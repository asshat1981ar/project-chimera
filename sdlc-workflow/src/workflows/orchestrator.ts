import { runGatePhase } from './phases/gate';
import { runImplementPhase } from './phases/implement';
import { runReleasePhase } from './phases/release';
import type { GatePayload, SprintRun } from '@/lib/types';

interface OrchestratorInput {
  runId: string;
  sprintVersion: string;
  taskManifest: string;
  gatePayload: GatePayload;
}

async function saveRun(run: SprintRun): Promise<void> {
  'use step';
  const { Redis } = await import('@upstash/redis');
  const redis = new Redis({ url: process.env.KV_REST_API_URL!, token: process.env.KV_REST_API_TOKEN! });
  await redis.set(`sdlc:run:${run.runId}`, JSON.stringify(run), { ex: 86400 });
}

export async function chimeraSprintWorkflow(input: OrchestratorInput): Promise<SprintRun> {
  'use workflow';

  const run: SprintRun = {
    runId: input.runId,
    sprintVersion: input.sprintVersion,
    startedAt: new Date().toISOString(),
    currentPhase: 'gate',
    phases: {},
  };

  await saveRun(run);

  // Phase 1: Gate
  const gateResult = await runGatePhase(input.gatePayload);
  run.phases.gate = gateResult;

  if (gateResult.status === 'failed') {
    run.currentPhase = 'gate';
    await saveRun(run);
    return run;
  }

  // Phase 2: Implement (pauses for human/agent approval)
  run.currentPhase = 'implement';
  await saveRun(run);
  const implementResult = await runImplementPhase(input.runId, input.taskManifest);
  run.phases.implement = implementResult;

  if (implementResult.status === 'failed') {
    run.currentPhase = 'implement';
    await saveRun(run);
    return run;
  }

  // Phase 3: Validate — payload arrives asynchronously via /api/chimera-sdlc/event
  run.currentPhase = 'validate';
  await saveRun(run);

  // Phase 4: Release
  run.currentPhase = 'release';
  await saveRun(run);
  const releaseResult = await runReleasePhase(
    input.sprintVersion,
    `Sprint ${input.sprintVersion} — automated release from Chimera SDLC`,
  );
  run.phases.release = releaseResult;

  run.currentPhase = releaseResult.status === 'passed' ? 'reflect' : 'release';
  await saveRun(run);
  return run;
}
