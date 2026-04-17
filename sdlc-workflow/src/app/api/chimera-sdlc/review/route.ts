import { resumeHook } from 'workflow/api';
import type { ReviewPayload } from '@/lib/types';

function verifySecret(req: Request): boolean {
  return req.headers.get('x-sdlc-secret') === process.env.SDLC_WEBHOOK_SECRET;
}

export async function POST(req: Request) {
  if (!verifySecret(req)) {
    return Response.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const body = await req.json() as { runId: string } & ReviewPayload;

  if (body.decision !== 'approved' && body.decision !== 'rejected') {
    return Response.json({ error: 'decision must be "approved" or "rejected"' }, { status: 400 });
  }

  await resumeHook(`${body.runId}-review`, {
    decision: body.decision,
    notes: body.notes,
    agentSummary: body.agentSummary,
  });

  return Response.json({ resumed: true, runId: body.runId });
}
