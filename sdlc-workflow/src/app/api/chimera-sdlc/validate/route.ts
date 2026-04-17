import { resumeHook } from 'workflow/api';
import type { ValidatePayload } from '@/lib/types';

function verifySecret(req: Request): boolean {
  const secret = req.headers.get('x-sdlc-secret');
  return secret === process.env.SDLC_WEBHOOK_SECRET;
}

export async function POST(req: Request) {
  if (!verifySecret(req)) {
    return Response.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const body = await req.json() as { runId: string } & ValidatePayload;

  if (typeof body.testsPassed !== 'boolean') {
    return Response.json({ error: 'testsPassed (boolean) required' }, { status: 400 });
  }

  await resumeHook(`${body.runId}-validate`, {
    testsPassed:  body.testsPassed,
    testOutput:   body.testOutput ?? '',
    detektClean:  body.detektClean ?? true,
  });

  return Response.json({ resumed: true, runId: body.runId });
}
