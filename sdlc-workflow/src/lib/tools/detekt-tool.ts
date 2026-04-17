import { tool } from 'ai';
import { z } from 'zod';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

function ghHeaders(): Record<string, string> {
  const token = process.env.GH_DISPATCH_TOKEN;
  if (!token) throw new Error('GH_DISPATCH_TOKEN environment variable is not set');
  return {
    Authorization: `Bearer ${token}`,
    Accept: 'application/vnd.github+json',
    'X-GitHub-Api-Version': '2022-11-28',
    'Content-Type': 'application/json',
  };
}

interface DetektResult {
  passed: boolean;
  conclusion: string;
  runUrl: string;
}

interface PollOptions {
  maxPolls?: number;
  pollIntervalMs?: number;
}

export async function runDetektStep(branch: string, opts: PollOptions = {}): Promise<DetektResult> {
  'use step';
  const { maxPolls = 90, pollIntervalMs = 0 } = opts;
  const dispatchedAt = new Date().toISOString();

  const dispatchResp = await fetch(
    `${GH_API}/repos/${REPO}/actions/workflows/detekt-check.yml/dispatches`,
    {
      method: 'POST',
      headers: ghHeaders(),
      body: JSON.stringify({ ref: branch, inputs: { branch } }),
    },
  );
  if (!dispatchResp.ok) {
    const text = await dispatchResp.text();
    throw new Error(`Detekt dispatch failed: ${dispatchResp.status} — ${text}`);
  }

  let consecutiveErrors = 0;
  for (let attempt = 0; attempt < maxPolls; attempt++) {
    if (pollIntervalMs > 0) await new Promise(r => setTimeout(r, pollIntervalMs));

    const listResp = await fetch(
      `${GH_API}/repos/${REPO}/actions/workflows/detekt-check.yml/runs?branch=${encodeURIComponent(branch)}&per_page=10`,
      { headers: ghHeaders() },
    );
    if (!listResp.ok) {
      consecutiveErrors++;
      if (consecutiveErrors >= 3) {
        throw new Error(`Detekt poll failed repeatedly: ${listResp.status}`);
      }
      continue;
    }
    consecutiveErrors = 0;

    const data = await listResp.json() as {
      workflow_runs: Array<{ id: number; status: string; conclusion: string | null; head_branch: string; created_at: string; html_url: string }>;
    };
    if (!Array.isArray(data.workflow_runs)) continue;

    const run = data.workflow_runs.find(
      r => r.head_branch === branch && r.created_at >= dispatchedAt,
    );

    if (!run) continue;
    if (run.status !== 'completed') continue;

    return {
      passed: run.conclusion === 'success',
      conclusion: run.conclusion ?? 'unknown',
      runUrl: run.html_url,
    };
  }

  return { passed: false, conclusion: 'timed_out', runUrl: '' };
}

export const detektTool = tool({
  description:
    'Dispatch a GitHub Actions detekt workflow on the current sprint branch and wait for the result. ' +
    'Use after writing or modifying Kotlin files to verify code quality before requesting human review.',
  inputSchema: z.object({
    branch: z.string().describe('Sprint branch name, e.g. feat/chimera-v1.10.0-sprint5'),
  }),
  execute: async ({ branch }) => {
    const result = await runDetektStep(branch, { maxPolls: 90, pollIntervalMs: 4000 });
    if (result.passed) return `Detekt passed. Run: ${result.runUrl}`;
    if (result.conclusion === 'timed_out') return 'Detekt timed out waiting for run. Check GitHub Actions manually.';
    return `Detekt FAILED (${result.conclusion}). See: ${result.runUrl}`;
  },
});
