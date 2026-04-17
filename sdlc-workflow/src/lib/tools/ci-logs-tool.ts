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
  };
}

function redactPii(text: string): string {
  return text
    .replace(/\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b/g, '[REDACTED_IP]')
    .replace(/[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g, '[REDACTED_EMAIL]')
    .replace(
      /Bearer\s+[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*/g,
      'Bearer [REDACTED_TOKEN]',
    )
    .replace(
      /(?:api[_-]?key|secret|token)["'\s:=]+[A-Za-z0-9_.-]{30,}/gi,
      'SECRET=[REDACTED]',
    );
}

export async function fetchCILogsStep(
  runId: string,
  jobNameFilter?: string,
  lineCount = 200,
): Promise<string> {
  'use step';

  const headers = ghHeaders();

  const jobsResp = await fetch(`${GH_API}/repos/${REPO}/actions/runs/${runId}/jobs`, { headers });
  if (!jobsResp.ok) {
    const errText = await jobsResp.text();
    return `[fetchCILogs error: jobs API ${jobsResp.status} — ${errText}]`;
  }

  const jobsData = await jobsResp.json() as { jobs: Array<{ id: number; name: string }> };
  let jobs = jobsData.jobs;
  if (jobNameFilter) {
    jobs = jobs.filter(j => j.name.includes(jobNameFilter));
  }
  if (jobs.length === 0) {
    return `[No jobs found for run ${runId}${jobNameFilter ? ` matching "${jobNameFilter}"` : ''}]`;
  }

  const logParts: string[] = [];
  for (const job of jobs) {
    const logResp = await fetch(
      `${GH_API}/repos/${REPO}/actions/jobs/${job.id}/logs`,
      { headers },
    );
    if (!logResp.ok) {
      logParts.push(`[Job "${job.name}" logs unavailable: ${logResp.status}]`);
      continue;
    }
    const rawLog = await logResp.text();
    const sanitized = redactPii(rawLog);
    const lines = sanitized.split('\n');
    const truncated = lines.slice(-lineCount).join('\n');
    logParts.push(`=== Job: ${job.name} ===\n${truncated}`);
  }

  return logParts.join('\n\n');
}

export const ciLogsTool = tool({
  description:
    'Fetch logs from a GitHub Actions workflow run. Use when a CI run (dispatched by the SDLC) fails ' +
    'and you need to read the error output to diagnose and fix the issue.',
  inputSchema: z.object({
    runId: z.string().describe('GitHub Actions run ID (numeric string), e.g. "12345678"'),
    jobName: z
      .string()
      .optional()
      .describe('Optional filter: only return logs from jobs whose name includes this string'),
    lineCount: z
      .number()
      .optional()
      .default(200)
      .describe('Max lines to return per job (default 200, max 1000)'),
  }),
  execute: async ({ runId, jobName, lineCount }) =>
    fetchCILogsStep(runId, jobName, Math.min(lineCount ?? 200, 1000)),
});
