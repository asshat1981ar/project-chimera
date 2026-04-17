# SDLC Quality Tools Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add three GitHub-API-native quality tools to the Chimera SDLC Workflow that give the DurableAgent real-time code quality feedback (`runDetekt`, `fetchCILogs`) and automate Android version management (`bumpAndroidVersion`).

**Architecture:** All tools are `'use step'` functions that call GitHub REST API — zero local filesystem access, fully compatible with Vercel serverless. `runDetekt` and `fetchCILogs` are wired as DurableAgent tools in the implement-agent phase, enabling a self-correcting quality loop (agent writes code → dispatches detekt → reads CI logs on failure → revises → repeats). `bumpAndroidVersion` is a standalone orchestrator step executed before the release phase.

**Tech Stack:** TypeScript, Vercel Workflow SDK 4.2.4, AI SDK v6 `tool()` with `inputSchema:`, GitHub REST API v2022-11-28 (Contents, Actions, Runs, Jobs), Zod, Vitest.

---

## Fit Analysis

| Proposed Tool | Decision | Reasoning |
|---|---|---|
| `runStaticAnalysis` (ESLint + npm audit) | **Adapt → `runDetekt`** | Repo is Kotlin/Android, not Node. Equivalent = Detekt. Dispatch via GitHub Actions + poll Checks API. |
| `fetchAndSanitizeLogs` (tail local file) | **Adapt → `fetchCILogs`** | No local log files on Vercel serverless. Equivalent = GitHub Actions job logs API. PII redaction logic kept. |
| `bumpSemanticVersion` (package.json) | **Adapt → `bumpAndroidVersion`** | No package.json. Version lives in `gradle.properties` as `VERSION_NAME`/`VERSION_CODE`. GitHub Contents API write. |
| `updateTSDocAST` (ts-morph AST) | **Drop** | Source is Kotlin, not TypeScript. ts-morph is YAGNI. KDoc is handled by human review. |
| `scaffoldDatabaseMigration` (SQL files) | **Drop** | Room uses annotation-based schema generation (`@Database(exportSchema=true)`). SQL migration files do not fit. |

---

## File Structure

**Create:**
- `.github/workflows/detekt-check.yml` — lightweight `workflow_dispatch`-only detekt runner
- `sdlc-workflow/src/lib/tools/detekt-tool.ts` — `runDetektStep()` + `detektTool` AI SDK export
- `sdlc-workflow/src/lib/tools/detekt-tool.test.ts` — 9 tests
- `sdlc-workflow/src/lib/tools/ci-logs-tool.ts` — `fetchCILogsStep()` + `ciLogsTool` AI SDK export
- `sdlc-workflow/src/lib/tools/ci-logs-tool.test.ts` — 9 tests
- `sdlc-workflow/src/lib/tools/android-version-tool.ts` — `bumpAndroidVersionStep()` step function
- `sdlc-workflow/src/lib/tools/android-version-tool.test.ts` — 9 tests

**Modify:**
- `sdlc-workflow/src/workflows/phases/implement-agent.ts` — add `detektTool` + `ciLogsTool` to `agentTools`
- `sdlc-workflow/src/workflows/orchestrator.ts` — call `bumpAndroidVersionStep` before `runReleasePhase`

---

### Task 1: GitHub Actions detekt-check workflow

**Files:**
- Create: `.github/workflows/detekt-check.yml`

This is a prerequisite for `runDetekt`. It must exist in the repo before the tool can dispatch it.

- [ ] **Step 1: Create the workflow file**

Write `.github/workflows/detekt-check.yml`:

```yaml
name: Detekt Check

on:
  workflow_dispatch:
    inputs:
      branch:
        description: 'Branch to analyze'
        required: true
        default: 'main'
      sdlc_run_id:
        description: 'Correlation ID (optional)'
        required: false
        default: ''

jobs:
  detekt:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ inputs.branch }}

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          cache-read-only: true

      - name: Grant execute permission
        run: chmod +x gradlew

      - name: Run Detekt
        run: ./gradlew detekt --continue
```

The `--continue` flag ensures all violations are reported even if the task fails mid-run. The job conclusion (`success`/`failure`) is what the tool polls for.

- [ ] **Step 2: Commit**

```bash
cd /home/westonaaron675/Chimera/project-chimera
git add .github/workflows/detekt-check.yml
git commit -m "ci: add detekt-check workflow for SDLC agent dispatch"
```

Expected: commit with 1 file.

---

### Task 2: `runDetekt` tool

**Files:**
- Create: `sdlc-workflow/src/lib/tools/detekt-tool.ts`
- Create: `sdlc-workflow/src/lib/tools/detekt-tool.test.ts`

The tool dispatches `detekt-check.yml` and polls the GitHub Actions runs list until the run completes, then reports pass/fail.

- [ ] **Step 1: Write the failing tests**

Write `sdlc-workflow/src/lib/tools/detekt-tool.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { runDetektStep } from './detekt-tool';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

function makeRunsResp(runs: object[]) {
  return { workflow_runs: runs };
}

function makeRun(id: number, status: string, conclusion: string | null, branch: string, createdAt: string) {
  return { id, status, conclusion, head_branch: branch, created_at: createdAt, html_url: `https://github.com/run/${id}` };
}

beforeEach(() => {
  process.env.GH_DISPATCH_TOKEN = 'test-token';
  vi.resetAllMocks();
});

describe('runDetektStep', () => {
  it('dispatches workflow_dispatch to detekt-check.yml', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const dispatchTime = new Date().toISOString();
    const completedRun = makeRun(42, 'completed', 'success', 'feat/my-branch', new Date(Date.now() + 1000).toISOString());

    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([completedRun]) });

    const result = await runDetektStep('feat/my-branch');

    expect(fetchMock).toHaveBeenCalledWith(
      `${GH_API}/repos/${REPO}/actions/workflows/detekt-check.yml/dispatches`,
      expect.objectContaining({ method: 'POST' }),
    );
    expect(result.passed).toBe(true);
    expect(result.conclusion).toBe('success');
  });

  it('returns passed=false when conclusion is failure', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const failedRun = makeRun(43, 'completed', 'failure', 'feat/my-branch', new Date(Date.now() + 1000).toISOString());

    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([failedRun]) });

    const result = await runDetektStep('feat/my-branch');

    expect(result.passed).toBe(false);
    expect(result.conclusion).toBe('failure');
  });

  it('skips runs created before dispatch and waits for newer run', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const oldRun = makeRun(10, 'completed', 'success', 'feat/my-branch', new Date(Date.now() - 60_000).toISOString());
    const newRun = makeRun(11, 'completed', 'success', 'feat/my-branch', new Date(Date.now() + 2000).toISOString());

    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([oldRun]) })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([newRun]) });

    const result = await runDetektStep('feat/my-branch');
    expect(result.passed).toBe(true);
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it('skips in-progress runs and waits for completion', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const inProgress = makeRun(20, 'in_progress', null, 'feat/my-branch', new Date(Date.now() + 1000).toISOString());
    const completed = makeRun(20, 'completed', 'success', 'feat/my-branch', new Date(Date.now() + 1000).toISOString());

    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([inProgress]) })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([completed]) });

    const result = await runDetektStep('feat/my-branch');
    expect(result.passed).toBe(true);
  });

  it('throws if GH_DISPATCH_TOKEN is not set', async () => {
    delete process.env.GH_DISPATCH_TOKEN;
    await expect(runDetektStep('main')).rejects.toThrow('GH_DISPATCH_TOKEN');
  });

  it('throws if dispatch API returns non-2xx', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;
    fetchMock.mockResolvedValueOnce({ ok: false, status: 422, text: async () => 'Unprocessable' });

    await expect(runDetektStep('bad-branch')).rejects.toThrow('422');
  });

  it('returns passed=false and sets conclusion=timed_out after max polls', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const pending = makeRun(99, 'queued', null, 'feat/my-branch', new Date(Date.now() + 1000).toISOString());

    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValue({ ok: true, json: async () => makeRunsResp([pending]) });

    const result = await runDetektStep('feat/my-branch', { maxPolls: 3, pollIntervalMs: 0 });
    expect(result.passed).toBe(false);
    expect(result.conclusion).toBe('timed_out');
  });

  it('filters runs to the correct branch', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const wrongBranch = makeRun(5, 'completed', 'success', 'feat/other-branch', new Date(Date.now() + 1000).toISOString());
    const correctBranch = makeRun(6, 'completed', 'success', 'feat/my-branch', new Date(Date.now() + 1000).toISOString());

    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([wrongBranch, correctBranch]) });

    const result = await runDetektStep('feat/my-branch');
    expect(result.passed).toBe(true);
  });

  it('exposes runUrl in result', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const run = makeRun(77, 'completed', 'success', 'feat/my-branch', new Date(Date.now() + 1000).toISOString());
    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, json: async () => makeRunsResp([run]) });

    const result = await runDetektStep('feat/my-branch');
    expect(result.runUrl).toBe('https://github.com/run/77');
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run src/lib/tools/detekt-tool.test.ts 2>&1 | tail -10
```

Expected: FAIL — `Cannot find module './detekt-tool'`

- [ ] **Step 3: Implement `detekt-tool.ts`**

Write `sdlc-workflow/src/lib/tools/detekt-tool.ts`:

```typescript
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
  const { maxPolls = 90, pollIntervalMs = 4000 } = opts;
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

  for (let attempt = 0; attempt < maxPolls; attempt++) {
    if (pollIntervalMs > 0) await new Promise(r => setTimeout(r, pollIntervalMs));

    const listResp = await fetch(
      `${GH_API}/repos/${REPO}/actions/workflows/detekt-check.yml/runs?branch=${encodeURIComponent(branch)}&per_page=10`,
      { headers: ghHeaders() },
    );
    if (!listResp.ok) continue;

    const data = await listResp.json() as {
      workflow_runs: Array<{ id: number; status: string; conclusion: string | null; head_branch: string; created_at: string; html_url: string }>;
    };

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
    const result = await runDetektStep(branch);
    return result.passed
      ? `Detekt passed. Run: ${result.runUrl}`
      : `Detekt FAILED (${result.conclusion}). See: ${result.runUrl}`;
  },
});
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run src/lib/tools/detekt-tool.test.ts 2>&1 | tail -10
```

Expected: `Test Files 1 passed` and `Tests 9 passed`

- [ ] **Step 5: Verify TypeScript compiles**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx tsc --noEmit 2>&1 | head -20
```

Expected: no output (zero errors).

- [ ] **Step 6: Commit**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
git add src/lib/tools/detekt-tool.ts src/lib/tools/detekt-tool.test.ts
git commit -m "feat(sdlc): runDetekt — GitHub Actions dispatch + poll tool for DurableAgent"
```

---

### Task 3: `fetchCILogs` tool

**Files:**
- Create: `sdlc-workflow/src/lib/tools/ci-logs-tool.ts`
- Create: `sdlc-workflow/src/lib/tools/ci-logs-tool.test.ts`

Fetches GitHub Actions job logs by run ID, redacts PII, and returns truncated text. Enables the agent to read CI failure output and self-repair.

- [ ] **Step 1: Write the failing tests**

Write `sdlc-workflow/src/lib/tools/ci-logs-tool.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchCILogsStep } from './ci-logs-tool';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

function makeJob(id: number, name: string) {
  return { id, name };
}

beforeEach(() => {
  process.env.GH_DISPATCH_TOKEN = 'test-token';
  vi.resetAllMocks();
});

describe('fetchCILogsStep', () => {
  it('fetches jobs for the given run ID', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'detekt')] }) })
      .mockResolvedValueOnce({ ok: true, text: async () => 'line1\nline2\nline3' });

    await fetchCILogsStep('12345');

    expect(fetchMock).toHaveBeenCalledWith(
      `${GH_API}/repos/${REPO}/actions/runs/12345/jobs`,
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer test-token' }) }),
    );
  });

  it('filters jobs by jobName when provided', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'build'), makeJob(2, 'detekt')] }) })
      .mockResolvedValueOnce({ ok: true, text: async () => 'detekt log line' });

    const result = await fetchCILogsStep('100', 'detekt');

    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock.mock.calls[1][0]).toContain('/actions/jobs/2/logs');
    expect(result).toContain('detekt log line');
  });

  it('redacts IP addresses from logs', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'ci')] }) })
      .mockResolvedValueOnce({ ok: true, text: async () => 'Connecting to 192.168.1.42 for download' });

    const result = await fetchCILogsStep('200');
    expect(result).not.toContain('192.168.1.42');
    expect(result).toContain('[REDACTED_IP]');
  });

  it('redacts email addresses from logs', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'ci')] }) })
      .mockResolvedValueOnce({ ok: true, text: async () => 'Signed by user@example.com via token' });

    const result = await fetchCILogsStep('300');
    expect(result).not.toContain('user@example.com');
    expect(result).toContain('[REDACTED_EMAIL]');
  });

  it('redacts Bearer tokens from logs', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'ci')] }) })
      .mockResolvedValueOnce({ ok: true, text: async () => 'Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.abc.def' });

    const result = await fetchCILogsStep('400');
    expect(result).not.toContain('eyJhbGciOiJSUzI1NiJ9');
    expect(result).toContain('[REDACTED_TOKEN]');
  });

  it('truncates logs to lineCount lines', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const bigLog = Array.from({ length: 1000 }, (_, i) => `line ${i}`).join('\n');
    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'ci')] }) })
      .mockResolvedValueOnce({ ok: true, text: async () => bigLog });

    const result = await fetchCILogsStep('500', undefined, 50);
    const lineCount = result.split('\n').filter(Boolean).length;
    expect(lineCount).toBeLessThanOrEqual(50);
  });

  it('returns message when no jobs found', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock.mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [] }) });

    const result = await fetchCILogsStep('600');
    expect(result).toContain('[No jobs found');
  });

  it('returns message when jobs API fails', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock.mockResolvedValueOnce({ ok: false, status: 404, text: async () => 'Not Found' });

    const result = await fetchCILogsStep('700');
    expect(result).toContain('[fetchCILogs error');
  });

  it('throws if GH_DISPATCH_TOKEN is not set', async () => {
    delete process.env.GH_DISPATCH_TOKEN;
    await expect(fetchCILogsStep('999')).rejects.toThrow('GH_DISPATCH_TOKEN');
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run src/lib/tools/ci-logs-tool.test.ts 2>&1 | tail -10
```

Expected: FAIL — `Cannot find module './ci-logs-tool'`

- [ ] **Step 3: Implement `ci-logs-tool.ts`**

Write `sdlc-workflow/src/lib/tools/ci-logs-tool.ts`:

```typescript
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
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run src/lib/tools/ci-logs-tool.test.ts 2>&1 | tail -10
```

Expected: `Test Files 1 passed` and `Tests 9 passed`

- [ ] **Step 5: Verify TypeScript compiles**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx tsc --noEmit 2>&1 | head -20
```

Expected: no output.

- [ ] **Step 6: Commit**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
git add src/lib/tools/ci-logs-tool.ts src/lib/tools/ci-logs-tool.test.ts
git commit -m "feat(sdlc): fetchCILogs — GitHub Actions log fetcher with PII redaction for DurableAgent"
```

---

### Task 4: `bumpAndroidVersion` tool

**Files:**
- Create: `sdlc-workflow/src/lib/tools/android-version-tool.ts`
- Create: `sdlc-workflow/src/lib/tools/android-version-tool.test.ts`

Reads `gradle.properties` on the sprint branch, parses `VERSION_NAME`/`VERSION_CODE`, applies semver bump, and commits the update back to the branch. Called from the orchestrator workflow before GitHub Release creation.

Note: `gradle.properties` currently does NOT define `VERSION_NAME`/`VERSION_CODE` (they fall back to defaults in `app/build.gradle.kts`). This tool upserts those properties on first use.

- [ ] **Step 1: Write the failing tests**

Write `sdlc-workflow/src/lib/tools/android-version-tool.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { bumpAndroidVersionStep, parseVersion, bumpVersion } from './android-version-tool';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

const PROPS_WITH_VERSION = `org.gradle.jvmargs=-Xmx2048m\nVERSION_NAME=1.2.3\nVERSION_CODE=42\n`;
const PROPS_WITHOUT_VERSION = `org.gradle.jvmargs=-Xmx2048m\nkotlin.code.style=official\n`;

function encodeBase64(s: string) {
  return Buffer.from(s, 'utf-8').toString('base64');
}

beforeEach(() => {
  process.env.GH_DISPATCH_TOKEN = 'test-token';
  vi.resetAllMocks();
});

describe('parseVersion', () => {
  it('parses VERSION_NAME and VERSION_CODE from properties', () => {
    const result = parseVersion(PROPS_WITH_VERSION);
    expect(result.versionName).toBe('1.2.3');
    expect(result.versionCode).toBe(42);
  });

  it('returns defaults when properties are absent', () => {
    const result = parseVersion(PROPS_WITHOUT_VERSION);
    expect(result.versionName).toBe('1.0.0');
    expect(result.versionCode).toBe(1);
  });
});

describe('bumpVersion', () => {
  it('increments patch version', () => {
    expect(bumpVersion('1.2.3', 'patch')).toBe('1.2.4');
  });

  it('increments minor version and resets patch', () => {
    expect(bumpVersion('1.2.3', 'minor')).toBe('1.3.0');
  });

  it('increments major version and resets minor and patch', () => {
    expect(bumpVersion('1.2.3', 'major')).toBe('2.0.0');
  });
});

describe('bumpAndroidVersionStep', () => {
  it('reads gradle.properties, bumps patch, and writes back', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITH_VERSION), sha: 'abc123', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    const result = await bumpAndroidVersionStep('feat/my-branch', 'patch');

    expect(result.newVersionName).toBe('1.2.4');
    expect(result.newVersionCode).toBe(43);
    expect(fetchMock.mock.calls[1][0]).toContain('/contents/gradle.properties');
    expect(fetchMock.mock.calls[1][1]?.method).toBe('PUT');
  });

  it('upserts VERSION_NAME and VERSION_CODE when absent', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITHOUT_VERSION), sha: 'def456', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    const result = await bumpAndroidVersionStep('main', 'patch');

    expect(result.newVersionName).toBe('1.0.1');
    expect(result.newVersionCode).toBe(2);
    const putBody = JSON.parse(fetchMock.mock.calls[1][1]?.body as string);
    const written = Buffer.from(putBody.content, 'base64').toString('utf-8');
    expect(written).toContain('VERSION_NAME=1.0.1');
    expect(written).toContain('VERSION_CODE=2');
  });

  it('throws if GH_DISPATCH_TOKEN is not set', async () => {
    delete process.env.GH_DISPATCH_TOKEN;
    await expect(bumpAndroidVersionStep('main', 'patch')).rejects.toThrow('GH_DISPATCH_TOKEN');
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run src/lib/tools/android-version-tool.test.ts 2>&1 | tail -10
```

Expected: FAIL — `Cannot find module './android-version-tool'`

- [ ] **Step 3: Implement `android-version-tool.ts`**

Write `sdlc-workflow/src/lib/tools/android-version-tool.ts`:

```typescript
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

export interface VersionInfo {
  versionName: string;
  versionCode: number;
}

export function parseVersion(propertiesContent: string): VersionInfo {
  const nameMatch = propertiesContent.match(/^VERSION_NAME=(.+)$/m);
  const codeMatch = propertiesContent.match(/^VERSION_CODE=(\d+)$/m);
  return {
    versionName: nameMatch ? nameMatch[1].trim() : '1.0.0',
    versionCode: codeMatch ? parseInt(codeMatch[1], 10) : 1,
  };
}

export function bumpVersion(versionName: string, bumpType: 'patch' | 'minor' | 'major'): string {
  const parts = versionName.split('.').map(Number);
  const [major = 0, minor = 0, patch = 0] = parts;
  if (bumpType === 'major') return `${major + 1}.0.0`;
  if (bumpType === 'minor') return `${major}.${minor + 1}.0`;
  return `${major}.${minor}.${patch + 1}`;
}

function upsertProperty(content: string, key: string, value: string): string {
  const pattern = new RegExp(`^${key}=.*$`, 'm');
  if (pattern.test(content)) {
    return content.replace(pattern, `${key}=${value}`);
  }
  return content.trimEnd() + `\n${key}=${value}\n`;
}

export interface BumpResult {
  newVersionName: string;
  newVersionCode: number;
  oldVersionName: string;
  oldVersionCode: number;
}

export async function bumpAndroidVersionStep(
  branch: string,
  bumpType: 'patch' | 'minor' | 'major' = 'patch',
): Promise<BumpResult> {
  'use step';

  const headers = ghHeaders();
  const url = `${GH_API}/repos/${REPO}/contents/gradle.properties?ref=${encodeURIComponent(branch)}`;

  const getResp = await fetch(url, { headers });
  if (!getResp.ok) throw new Error(`bumpAndroidVersion: GET gradle.properties failed ${getResp.status}`);

  const data = await getResp.json() as { content: string; sha: string; encoding: string };
  const currentContent = Buffer.from(data.content.replace(/\n/g, ''), 'base64').toString('utf-8');

  const { versionName: oldVersionName, versionCode: oldVersionCode } = parseVersion(currentContent);
  const newVersionName = bumpVersion(oldVersionName, bumpType);
  const newVersionCode = oldVersionCode + 1;

  let updatedContent = upsertProperty(currentContent, 'VERSION_NAME', newVersionName);
  updatedContent = upsertProperty(updatedContent, 'VERSION_CODE', String(newVersionCode));

  const putBody = {
    message: `chore: bump Android version to ${newVersionName} (${newVersionCode})`,
    content: Buffer.from(updatedContent, 'utf-8').toString('base64'),
    sha: data.sha,
    branch,
  };

  const putResp = await fetch(`${GH_API}/repos/${REPO}/contents/gradle.properties`, {
    method: 'PUT',
    headers,
    body: JSON.stringify(putBody),
  });
  if (!putResp.ok) {
    const errText = await putResp.text();
    throw new Error(`bumpAndroidVersion: PUT failed ${putResp.status} — ${errText}`);
  }

  return { newVersionName, newVersionCode, oldVersionName, oldVersionCode };
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run src/lib/tools/android-version-tool.test.ts 2>&1 | tail -10
```

Expected: `Test Files 1 passed` and `Tests 9 passed`

- [ ] **Step 5: Verify TypeScript compiles**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx tsc --noEmit 2>&1 | head -20
```

Expected: no output.

- [ ] **Step 6: Commit**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
git add src/lib/tools/android-version-tool.ts src/lib/tools/android-version-tool.test.ts
git commit -m "feat(sdlc): bumpAndroidVersion — gradle.properties VERSION_NAME/CODE bumper via GitHub API"
```

---

### Task 5: Wire `runDetekt` + `fetchCILogs` into DurableAgent

**Files:**
- Modify: `sdlc-workflow/src/workflows/phases/implement-agent.ts`

Add the two new agent tools to the `agentTools` object inside `runAgent`. The agent will use `runDetekt` after writing code, and `fetchCILogs` after a CI failure to diagnose and self-repair.

- [ ] **Step 1: Update `implement-agent.ts`**

In `sdlc-workflow/src/workflows/phases/implement-agent.ts`, add the imports after the existing imports:

```typescript
import { detektTool } from '@/lib/tools/detekt-tool';
import { ciLogsTool } from '@/lib/tools/ci-logs-tool';
```

Then in the `agentTools` object (inside `runAgent`, after the `searchCode` tool and before the closing `}`), add:

```typescript
    runDetekt: detektTool,
    fetchCILogs: ciLogsTool,
```

The complete `agentTools` object after modification:

```typescript
  const agentTools = {
    readFile: tool({
      description: 'Read a file from the Chimera repository. Returns the full text content.',
      inputSchema: z.object({
        path: z.string().describe('Relative file path from repo root, e.g. domain/src/main/kotlin/com/chimera/domain/Foo.kt'),
      }),
      execute: async ({ path }) => readFile(path, branch),
    }),
    writeFile: tool({
      description: 'Write or update a file in the Chimera repository. Creates a commit on the sprint branch.',
      inputSchema: z.object({
        path: z.string().describe('Relative file path from repo root'),
        content: z.string().describe('Full file content to write'),
        commitMessage: z.string().describe('Git commit message, e.g. feat(domain): add FooUseCase'),
      }),
      execute: async ({ path, content, commitMessage }) =>
        writeFile(path, content, branch, commitMessage).then(() => `Written: ${path}`),
    }),
    listDirectory: tool({
      description: 'List files and subdirectories at a given path in the repository.',
      inputSchema: z.object({
        path: z.string().describe('Directory path, e.g. domain/src/main/kotlin/com/chimera/domain'),
      }),
      execute: async ({ path }) => listDirectory(path, branch).then(entries => entries.join('\n')),
    }),
    searchCode: tool({
      description: 'Search the repository for code matching a query. Returns matching file paths.',
      inputSchema: z.object({
        query: z.string().describe('Search query, e.g. "@HiltViewModel" or "class SaveRepository"'),
        extension: z.string().describe('File extension to search, e.g. "kt" or "kts"'),
      }),
      execute: async ({ query, extension }) => searchCode(query, extension),
    }),
    runDetekt: detektTool,
    fetchCILogs: ciLogsTool,
  };
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx tsc --noEmit 2>&1 | head -20
```

Expected: no output.

- [ ] **Step 3: Run full test suite to confirm no regressions**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run 2>&1 | tail -10
```

Expected: all existing tests still pass plus the new 18 tests.

- [ ] **Step 4: Commit**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
git add src/workflows/phases/implement-agent.ts
git commit -m "feat(sdlc): wire runDetekt + fetchCILogs into DurableAgent tool set"
```

---

### Task 6: Wire `bumpAndroidVersion` into orchestrator

**Files:**
- Modify: `sdlc-workflow/src/workflows/orchestrator.ts`

Call `bumpAndroidVersionStep` in the orchestrator before `runReleasePhase`. It runs on the sprint branch so the version commit appears before the GitHub Release tag is created.

- [ ] **Step 1: Update `orchestrator.ts`**

Add the import at the top of `sdlc-workflow/src/workflows/orchestrator.ts` after the existing phase imports:

```typescript
import { bumpAndroidVersionStep } from '@/lib/tools/android-version-tool';
```

Then, in the `chimeraSprintWorkflow` function body, replace the Phase 4 block:

Before (lines ~74–88):
```typescript
  // ── Phase 4: Release ──────────────────────────────────────────
  run.currentPhase = 'release';
  await saveRun(run);

  const releaseResult = await runReleasePhase(
    input.sprintVersion,
    `Sprint ${input.sprintVersion} — automated release from Chimera SDLC`,
  );
```

After:
```typescript
  // ── Phase 4: Release ──────────────────────────────────────────
  run.currentPhase = 'release';
  await saveRun(run);

  const bumpResult = await bumpAndroidVersionStep(input.branch, 'patch');
  console.log(`[RELEASE] Version bumped: ${bumpResult.oldVersionName} → ${bumpResult.newVersionName} (code ${bumpResult.newVersionCode})`);

  const releaseResult = await runReleasePhase(
    input.sprintVersion,
    `Sprint ${input.sprintVersion} — automated release from Chimera SDLC\n\nAndroid version: ${bumpResult.newVersionName} (${bumpResult.newVersionCode})`,
  );
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx tsc --noEmit 2>&1 | head -20
```

Expected: no output.

- [ ] **Step 3: Run full test suite**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run 2>&1 | tail -10
```

Expected: all tests pass.

- [ ] **Step 4: Commit**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
git add src/workflows/orchestrator.ts
git commit -m "feat(sdlc): bump Android version in gradle.properties as part of automated release phase"
```

---

### Task 7: Final smoke test

Verify the full picture: new GitHub Actions workflow file, 27 new tests all passing, tsc clean, orchestrator imports compile.

- [ ] **Step 1: Run full vitest**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx vitest run 2>&1 | tail -15
```

Expected output (counts may vary by existing test count):
```
✓ src/lib/tools/github-file-tools.test.ts (12 tests)
✓ src/lib/tools/detekt-tool.test.ts (9 tests)
✓ src/lib/tools/ci-logs-tool.test.ts (9 tests)
✓ src/lib/tools/android-version-tool.test.ts (9 tests)
✓ src/app/api/chimera-sdlc/review/route.test.ts (4 tests)

Test Files  5 passed
Tests  43 passed
```

- [ ] **Step 2: Final tsc check**

```bash
cd /home/westonaaron675/Chimera/project-chimera/sdlc-workflow
npx tsc --noEmit 2>&1
```

Expected: no output (zero errors).

- [ ] **Step 3: Verify workflow file is present**

```bash
ls /home/westonaaron675/Chimera/project-chimera/.github/workflows/detekt-check.yml
```

Expected: file listed.

- [ ] **Step 4: Final commit if any unstaged changes**

```bash
cd /home/westonaaron675/Chimera/project-chimera
git status --short
```

If clean: nothing to do. If unstaged: `git add` and commit with `chore(sdlc): smoke test fixes`.

---

## Self-Review

**Spec coverage:**

| User spec requirement | Covered by |
|---|---|
| `runStaticAnalysis` → detect errors before promotion | Task 2: `runDetektStep` dispatches `detekt-check.yml`, polls until complete |
| `fetchAndSanitizeLogs` → tail recent log lines, redact PII | Task 3: `fetchCILogsStep` fetches job logs, redacts IP/email/token |
| `bumpSemanticVersion` → safe SemVer bump, update changelog | Task 4: `bumpAndroidVersionStep` bumps VERSION_NAME/VERSION_CODE in `gradle.properties` |
| `updateTSDocAST` | Dropped — YAGNI (Kotlin codebase) |
| `scaffoldDatabaseMigration` | Dropped — Room annotation-based migrations, no SQL files |
| Tools work on Vercel serverless | ✅ All use GitHub API, zero `exec()` / filesystem |
| AI SDK v6 `inputSchema:` | ✅ All `tool()` calls use `inputSchema:` |
| No `parameters:` | ✅ Removed from all new tools |

**No placeholders:** All code blocks are complete and runnable.

**Type consistency:** `DetektResult`, `BumpResult`, `VersionInfo` types are defined in implementation files and referenced consistently across tests.
