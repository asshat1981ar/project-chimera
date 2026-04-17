export async function GET(
  _req: Request,
  { params }: { params: Promise<{ runId: string }> },
) {
  const { runId } = await params;
  const { kv } = await import('@vercel/kv');
  const run = await kv.get(`sdlc:run:${runId}`);

  if (!run) {
    return Response.json({ error: 'Run not found' }, { status: 404 });
  }

  return Response.json(run);
}
