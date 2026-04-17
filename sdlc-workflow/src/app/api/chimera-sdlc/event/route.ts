import type { ValidatePayload } from '@/lib/types';

function verifySecret(req: Request): boolean {
  const secret = req.headers.get('x-sdlc-secret');
  return secret === process.env.SDLC_WEBHOOK_SECRET;
}

export async function POST(req: Request) {
  if (!verifySecret(req)) {
    return Response.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const body = await req.json() as {
    runId: string;
    phase: 'validate';
    payload: ValidatePayload;
  };

  // Validate phase results are stored in KV for the orchestrator to pick up
  const { kv } = await import('@vercel/kv');
  await kv.set(`sdlc:${body.runId}:${body.phase}`, JSON.stringify(body.payload), {
    ex: 86400, // 24h TTL
  });

  return Response.json({ received: true });
}
