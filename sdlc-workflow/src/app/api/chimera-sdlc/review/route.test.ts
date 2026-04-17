import { describe, it, expect, vi, beforeAll } from 'vitest';

const mockResumeHook = vi.fn().mockResolvedValue(undefined);
vi.mock('workflow/api', () => ({ resumeHook: mockResumeHook }));

process.env.SDLC_WEBHOOK_SECRET = 'test-secret';

const { POST } = await import('./route');

describe('POST /api/chimera-sdlc/review', () => {
  it('resumes hook with approved decision', async () => {
    mockResumeHook.mockClear();
    const req = new Request('http://localhost/api/chimera-sdlc/review', {
      method: 'POST',
      headers: { 'x-sdlc-secret': 'test-secret', 'Content-Type': 'application/json' },
      body: JSON.stringify({ runId: 'run-123', decision: 'approved', agentSummary: 'Implemented Foo' }),
    });

    const resp = await POST(req);
    const data = await resp.json();

    expect(resp.status).toBe(200);
    expect(data.resumed).toBe(true);
    expect(mockResumeHook).toHaveBeenCalledWith('run-123-review', {
      decision: 'approved',
      notes: undefined,
      agentSummary: 'Implemented Foo',
    });
  });

  it('returns 401 for wrong secret', async () => {
    const req = new Request('http://localhost/api/chimera-sdlc/review', {
      method: 'POST',
      headers: { 'x-sdlc-secret': 'wrong', 'Content-Type': 'application/json' },
      body: JSON.stringify({ runId: 'run-123', decision: 'approved' }),
    });

    const resp = await POST(req);
    expect(resp.status).toBe(401);
  });

  it('returns 400 for missing decision', async () => {
    const req = new Request('http://localhost/api/chimera-sdlc/review', {
      method: 'POST',
      headers: { 'x-sdlc-secret': 'test-secret', 'Content-Type': 'application/json' },
      body: JSON.stringify({ runId: 'run-123' }),
    });

    const resp = await POST(req);
    expect(resp.status).toBe(400);
  });

  it('resumes hook with rejected decision and notes', async () => {
    mockResumeHook.mockClear();
    const req = new Request('http://localhost/api/chimera-sdlc/review', {
      method: 'POST',
      headers: { 'x-sdlc-secret': 'test-secret', 'Content-Type': 'application/json' },
      body: JSON.stringify({ runId: 'run-456', decision: 'rejected', notes: 'Missing tests' }),
    });

    const resp = await POST(req);
    const data = await resp.json();

    expect(resp.status).toBe(200);
    expect(data.resumed).toBe(true);
    expect(mockResumeHook).toHaveBeenCalledWith('run-456-review', {
      decision: 'rejected',
      notes: 'Missing tests',
      agentSummary: undefined,
    });
  });
});
