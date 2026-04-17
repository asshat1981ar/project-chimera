import type { ValidatePayload, PhaseResult } from '@/lib/types';

export async function runValidatePhase(payload: ValidatePayload): Promise<PhaseResult> {
  const timestamp = new Date().toISOString();

  if (!payload.testsPassed) {
    return {
      phase: 'validate',
      status: 'failed',
      output: `Tests failed after implementation:\n${payload.testOutput}`,
      timestamp,
    };
  }

  if (!payload.detektClean) {
    return {
      phase: 'validate',
      status: 'failed',
      output: `Detekt violations introduced — fix before release`,
      timestamp,
    };
  }

  return {
    phase: 'validate',
    status: 'passed',
    output: `All tests pass. Detekt clean.\n${payload.testOutput}`,
    timestamp,
  };
}
