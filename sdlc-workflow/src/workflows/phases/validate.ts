import { createHook } from 'workflow';
import type { ValidatePayload, PhaseResult } from '@/lib/types';

export function evaluateValidate(payload: ValidatePayload): PhaseResult {
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
      output: 'Detekt violations introduced — fix before release',
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

export async function runValidatePhase(runId: string): Promise<PhaseResult> {
  'use workflow';

  const hook = createHook<ValidatePayload>({ token: `${runId}-validate` });

  for await (const payload of hook) {
    return evaluateValidate(payload);
  }

  return {
    phase: 'validate',
    status: 'failed',
    output: 'Validate hook closed without receiving CI results',
    timestamp: new Date().toISOString(),
  };
}
