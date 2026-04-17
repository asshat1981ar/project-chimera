export async function GET(
  _req: Request,
  { params }: { params: Promise<{ runId: string }> },
) {
  const { runId } = await params;
  const { Redis } = await import('@upstash/redis');
  const redis = new Redis({ url: process.env.KV_REST_API_URL!, token: process.env.KV_REST_API_TOKEN! });
  const raw = await redis.get(`sdlc:run:${runId}`);
  const run = typeof raw === 'string' ? JSON.parse(raw) : raw;

  if (!run) {
    return Response.json({ error: 'Run not found' }, { status: 404 });
  }

  return Response.json(run);
}
