import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { fetchCILogsStep } from './ci-logs-tool';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

function makeJob(id: number, name: string) {
  return { id, name };
}

beforeEach(() => {
  vi.stubEnv('GH_DISPATCH_TOKEN', 'test-token');
  vi.resetAllMocks();
});

afterEach(() => {
  vi.unstubAllEnvs();
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
    expect(lineCount).toBeLessThanOrEqual(52); // 50 log lines + header + blank separator
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
    vi.unstubAllEnvs();
    await expect(fetchCILogsStep('999')).rejects.toThrow('GH_DISPATCH_TOKEN');
  });

  it('returns partial results when one job log fetch fails', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ jobs: [makeJob(1, 'build'), makeJob(2, 'test')] }) })
      .mockResolvedValueOnce({ ok: false, status: 403, text: async () => 'Forbidden' })
      .mockResolvedValueOnce({ ok: true, text: async () => 'test log output' });

    const result = await fetchCILogsStep('800');
    expect(result).toContain('"build" logs unavailable: 403');
    expect(result).toContain('test log output');
  });
});
