import { chimeraSprintWorkflow } from '@/workflows/orchestrator';
import type { GatePayload } from '@/lib/types';

function verifySecret(req: Request): boolean {
  const secret = req.headers.get('x-sdlc-secret');
  return secret === process.env.SDLC_WEBHOOK_SECRET;
}

export async function POST(req: Request) {
  if (!verifySecret(req)) {
    return Response.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const body = await req.json() as {
    sprintVersion: string;
    taskManifest: string;
    gatePayload: GatePayload;
  };

  const runId = `${body.sprintVersion}-${Date.now()}`;

  // Fire and forget — workflow runs asynchronously on Vercel
  void chimeraSprintWorkflow({
    runId,
    sprintVersion: body.sprintVersion,
    taskManifest: body.taskManifest,
    gatePayload: body.gatePayload,
  });

  return Response.json({ runId, status: 'started' });
}
