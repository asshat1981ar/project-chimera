import { start } from 'workflow/api';
import { runGatePhase } from './phases/gate';
import { runImplementPhase } from './phases/implement';
import { runValidatePhase } from './phases/validate';
import { runReleasePhase } from './phases/release';
import { runReflectPhase } from './phases/reflect';
import type { SprintRun, OrchestratorInput, SprintInput } from '@/lib/types';

async function startNextSprint(next: SprintInput, rest: SprintInput[]): Promise<void> {
  'use step';
  await start(chimeraSprintWorkflow, [{ ...next, sprintQueue: rest }]);
  console.log(`[CHAIN] Queued sprint ${next.sprintVersion} (${rest.length} remaining in queue)`);
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

  // ── Phase 1: Gate ──────────────────────────────────────────────
  const gateResult = await runGatePhase(input.gatePayload);
  run.phases.gate = gateResult;

  if (gateResult.status === 'failed') {
    run.currentPhase = 'gate';
    await saveRun(run);
    return run;
  }

  // ── Phase 2: Implement (pauses until POST /approve) ────────────
  run.currentPhase = 'implement';
  await saveRun(run);

  const implementResult = await runImplementPhase(input.runId, input.taskManifest, input.sprintVersion, input.branch);
  run.phases.implement = implementResult;

  if (implementResult.status === 'failed') {
    run.currentPhase = 'implement';
    await saveRun(run);
    return run;
  }

  // ── Phase 3: Validate (pauses until POST /validate) ───────────
  run.currentPhase = 'validate';
  await saveRun(run);

  const validateResult = await runValidatePhase(input.runId);
  run.phases.validate = validateResult;

  if (validateResult.status === 'failed') {
    run.currentPhase = 'validate';
    await saveRun(run);
    return run;
  }

  // ── Phase 4: Release ──────────────────────────────────────────
  run.currentPhase = 'release';
  await saveRun(run);

  const releaseResult = await runReleasePhase(
    input.sprintVersion,
    `Sprint ${input.sprintVersion} — automated release from Chimera SDLC`,
  );
  run.phases.release = releaseResult;

  if (releaseResult.status === 'failed') {
    run.currentPhase = 'release';
    await saveRun(run);
    return run;
  }

  // ── Phase 5: Reflect ──────────────────────────────────────────
  run.currentPhase = 'reflect';
  await saveRun(run);

  const reflectResult = await runReflectPhase(run);
  run.phases.reflect = reflectResult;

  await saveRun(run);

  // ── Chain next sprint if queue is non-empty ───────────────────
  if (input.sprintQueue && input.sprintQueue.length > 0) {
    const [next, ...rest] = input.sprintQueue;
    await startNextSprint(next, rest);
  }

  return run;
}
