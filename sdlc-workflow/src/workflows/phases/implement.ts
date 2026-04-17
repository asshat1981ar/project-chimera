import { createHook } from 'workflow';
import type { PhaseResult, ApprovePayload } from '@/lib/types';

async function dispatchCiWorkflow(runId: string, sprintVersion: string, branch: string): Promise<void> {
  'use step';
  const token = process.env.GH_DISPATCH_TOKEN;
  if (!token) {
    console.warn('[IMPLEMENT] GH_DISPATCH_TOKEN not set — skipping CI dispatch, validate manually');
    return;
  }
  const resp = await fetch(
    'https://api.github.com/repos/asshat1981ar/project-chimera/actions/workflows/sdlc-validate.yml/dispatches',
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/vnd.github+json',
        'X-GitHub-Api-Version': '2022-11-28',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ref: branch,
        inputs: { run_id: runId, sprint_version: sprintVersion },
      }),
    },
  );
  if (!resp.ok) {
    const text = await resp.text();
    throw new Error(`GH workflow dispatch failed: ${resp.status} — ${text}`);
  }
  console.log(`[IMPLEMENT] CI dispatched for ${runId} on branch ${branch}`);
}

export async function runImplementPhase(
  runId: string,
  taskManifest: string,
  sprintVersion: string,
  branch: string,
): Promise<PhaseResult> {
  'use workflow';

  const timestamp = new Date().toISOString();

  console.log(`[IMPLEMENT] Task manifest for run ${runId} (branch: ${branch}):\n${taskManifest}`);

  const hook = createHook<ApprovePayload>({ token: runId });

  for await (const event of hook) {
    if (event.decision === 'approved') {
      await dispatchCiWorkflow(runId, sprintVersion, branch);
      return {
        phase: 'implement',
        status: 'passed',
        output: `Approved at ${event.agentDispatchedAt ?? timestamp}. CI dispatched on ${branch}. Notes: ${event.notes ?? 'none'}`,
        timestamp: new Date().toISOString(),
      };
    } else {
      return {
        phase: 'implement',
        status: 'failed',
        output: `Implement rejected. Notes: ${event.notes ?? 'none'}`,
        timestamp: new Date().toISOString(),
      };
    }
  }

  return {
    phase: 'implement',
    status: 'failed',
    output: 'Implement hook closed without event',
    timestamp: new Date().toISOString(),
  };
}
