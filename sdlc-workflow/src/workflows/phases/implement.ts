import { implementApprovalHook } from '@/lib/hooks';
import type { PhaseResult } from '@/lib/types';

export async function runImplementPhase(
  runId: string,
  taskManifest: string,
): Promise<PhaseResult> {
  'use workflow';

  const timestamp = new Date().toISOString();

  // Emit task manifest to stdout/logs — the operator reads this and dispatches agents
  console.log(`[IMPLEMENT] Task manifest for run ${runId}:\n${taskManifest}`);

  // Pause here until POST /api/chimera-sdlc/approve sends the hook event
  const events = implementApprovalHook.create({ token: runId });

  for await (const event of events) {
    if (event.decision === 'approved') {
      return {
        phase: 'implement',
        status: 'passed',
        output: `Agent dispatch approved at ${event.agentDispatchedAt ?? timestamp}. Notes: ${event.notes ?? 'none'}`,
        timestamp: new Date().toISOString(),
      };
    } else {
      return {
        phase: 'implement',
        status: 'failed',
        output: `Implement rejected. Notes: ${event.notes ?? 'none'}`,
        timestamp: new Date().toISOString(),
      };
    }
  }

  return {
    phase: 'implement',
    status: 'failed',
    output: 'Implement hook closed without event',
    timestamp: new Date().toISOString(),
  };
}
