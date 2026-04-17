import type { GatePayload, PhaseResult } from '@/lib/types';

export async function runGatePhase(payload: GatePayload): Promise<PhaseResult> {
  const timestamp = new Date().toISOString();

  if (payload.archViolations.length > 0) {
    return {
      phase: 'gate',
      status: 'failed',
      output: `Arch violations:\n${payload.archViolations.join('\n')}`,
      timestamp,
    };
  }

  if (!payload.buildSucceeded) {
    return {
      phase: 'gate',
      status: 'failed',
      output: 'assembleMockDebug failed — fix compile errors before proceeding',
      timestamp,
    };
  }

  const baselineOutput = [
    `core: ${payload.testBaselineCore}`,
    `domain: ${payload.testBaselineDomain}`,
  ].join('\n');

  if (baselineOutput.includes('BUILD FAILED')) {
    return {
      phase: 'gate',
      status: 'failed',
      output: `Test baseline failed:\n${baselineOutput}`,
      timestamp,
    };
  }

  return {
    phase: 'gate',
    status: 'passed',
    output: `Arch clean. Baseline recorded:\n${baselineOutput}`,
    timestamp,
  };
}
