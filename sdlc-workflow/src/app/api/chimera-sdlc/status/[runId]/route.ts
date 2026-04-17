export async function GET(
  _req: Request,
  { params }: { params: { runId: string } },
) {
  const { kv } = await import('@vercel/kv');
  const run = await kv.get(`sdlc:run:${params.runId}`);

  if (!run) {
    return Response.json({ error: 'Run not found' }, { status: 404 });
  }

  return Response.json(run);
}
