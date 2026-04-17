import { implementApprovalHook } from '@/lib/hooks';
import type { ApprovePayload } from '@/lib/types';

function verifySecret(req: Request): boolean {
  const secret = req.headers.get('x-sdlc-secret');
  return secret === process.env.SDLC_WEBHOOK_SECRET;
}

export async function POST(req: Request) {
  if (!verifySecret(req)) {
    return Response.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const body = await req.json() as { runId: string } & ApprovePayload;

  await implementApprovalHook.resume(body.runId, {
    decision: body.decision,
    notes: body.notes,
    agentDispatchedAt: body.agentDispatchedAt ?? new Date().toISOString(),
  });

  return Response.json({ resumed: true, runId: body.runId });
}
