import { start } from 'workflow/api';
import { chimeraSprintWorkflow } from '@/workflows/orchestrator';
import type { GatePayload, SprintInput } from '@/lib/types';

function verifySecret(req: Request): boolean {
  const secret = req.headers.get('x-sdlc-secret');
  return secret === process.env.SDLC_WEBHOOK_SECRET;
}

type QueueItem = { sprintVersion: string; taskManifest: string; gatePayload: GatePayload; branch?: string };

export async function POST(req: Request) {
  if (!verifySecret(req)) {
    return Response.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const body = await req.json() as {
    sprintVersion: string;
    taskManifest: string;
    gatePayload: GatePayload;
    branch: string;
    sprintQueue?: QueueItem[];
  };

  const runId = `${body.sprintVersion}-${Date.now()}`;

  const sprintQueue: SprintInput[] | undefined = body.sprintQueue?.map((item, i) => ({
    runId: `${item.sprintVersion}-${Date.now()}-${i}`,
    sprintVersion: item.sprintVersion,
    taskManifest: item.taskManifest,
    gatePayload: item.gatePayload,
    branch: item.branch ?? 'main',
  }));

  await start(chimeraSprintWorkflow, [{
    runId,
    sprintVersion: body.sprintVersion,
    taskManifest: body.taskManifest,
    gatePayload: body.gatePayload,
    branch: body.branch ?? 'main',
    sprintQueue,
  }]);

  return Response.json({ runId, status: 'started' });
}
