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
  const { Redis } = await import('@upstash/redis');
  const redis = new Redis({ url: process.env.KV_REST_API_URL!, token: process.env.KV_REST_API_TOKEN! });
  await redis.set(`sdlc:${body.runId}:${body.phase}`, JSON.stringify(body.payload), { ex: 86400 });

  return Response.json({ received: true });
}
