import { start } from 'workflow/api';
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
    branch: string;
    sprintQueue?: Array<{ sprintVersion: string; taskManifest: string; gatePayload: GatePayload; branch: string }>;
  };

  const runId = `${body.sprintVersion}-${Date.now()}`;

  await start(chimeraSprintWorkflow, [{
    runId,
    sprintVersion: body.sprintVersion,
    taskManifest: body.taskManifest,
    gatePayload: body.gatePayload,
    branch: body.branch ?? 'main',
    sprintQueue: body.sprintQueue,
  }]);

  return Response.json({ runId, status: 'started' });
}
