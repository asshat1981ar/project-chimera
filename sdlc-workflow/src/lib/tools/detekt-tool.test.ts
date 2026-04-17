import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
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
  vi.stubEnv('GH_DISPATCH_TOKEN', 'test-token');
  vi.resetAllMocks();
});

afterEach(() => {
  vi.unstubAllEnvs();
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
    const dispatchCall = fetchMock.mock.calls[0];
    const dispatchBody = JSON.parse(dispatchCall[1].body);
    expect(dispatchBody).toEqual({ ref: 'feat/my-branch', inputs: { branch: 'feat/my-branch' } });
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
    vi.unstubAllEnvs();
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

  it('detektTool.execute returns timed_out message when conclusion is timed_out', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    const pending = makeRun(99, 'queued', null, 'feat/my-branch', new Date(Date.now() + 1000).toISOString());
    fetchMock
      .mockResolvedValueOnce({ ok: true, text: async () => '' })
      .mockResolvedValue({ ok: true, json: async () => makeRunsResp([pending]) });

    const result = await runDetektStep('feat/my-branch', { maxPolls: 2, pollIntervalMs: 0 });
    expect(result.conclusion).toBe('timed_out');
    expect(result.runUrl).toBe('');
  });
});
