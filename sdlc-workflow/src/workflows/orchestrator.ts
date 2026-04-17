import { runGatePhase } from './phases/gate';
import { runImplementPhase } from './phases/implement';
import { runValidatePhase } from './phases/validate';
import { runReleasePhase } from './phases/release';
import type { GatePayload, ValidatePayload, SprintRun } from '@/lib/types';

interface OrchestratorInput {
  runId: string;
  sprintVersion: string;
  taskManifest: string;
  gatePayload: GatePayload;
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

  // Phase 1: Gate
  const gateResult = await runGatePhase(input.gatePayload);
  run.phases.gate = gateResult;

  if (gateResult.status === 'failed') {
    run.currentPhase = 'gate';
    return run;
  }

  // Phase 2: Implement (pauses for human/agent approval)
  run.currentPhase = 'implement';
  const implementResult = await runImplementPhase(input.runId, input.taskManifest);
  run.phases.implement = implementResult;

  if (implementResult.status === 'failed') {
    run.currentPhase = 'implement';
    return run;
  }

  // Phase 3: Validate — waits for GitHub Actions to POST validate results
  // (validate payload arrives via /api/chimera-sdlc/event after test re-run)
  run.currentPhase = 'validate';

  // Phase 4: Release
  run.currentPhase = 'release';
  const releaseResult = await runReleasePhase(
    input.sprintVersion,
    `Sprint ${input.sprintVersion} — automated release from Chimera SDLC`,
  );
  run.phases.release = releaseResult;

  run.currentPhase = releaseResult.status === 'passed' ? 'reflect' : 'release';
  return run;
}
