import type { SprintRun, PhaseResult } from '@/lib/types';

export async function runReflectPhase(run: SprintRun): Promise<PhaseResult> {
  'use step';

  const timestamp = new Date().toISOString();
  const lines: string[] = [`Sprint ${run.sprintVersion} — SDLC Reflect`];
  lines.push(`Run ID : ${run.runId}`);
  lines.push(`Started: ${run.startedAt}`);
  lines.push('');

  const phaseOrder: Array<keyof typeof run.phases> = ['gate', 'implement', 'validate', 'release'];
  for (const phase of phaseOrder) {
    const result = run.phases[phase];
    if (result) {
      const icon = result.status === 'passed' ? '✅' : result.status === 'failed' ? '❌' : '⏳';
      lines.push(`${icon} ${phase.toUpperCase()}: ${result.status}`);
      if (result.status === 'failed') {
        lines.push(`   ${result.output.split('\n')[0]}`);
      }
    }
  }

  const releaseUrl = (run.phases.release as { output?: string } | undefined)?.output?.match(/https?:\/\/\S+/)?.[0];
  if (releaseUrl) {
    lines.push('');
    lines.push(`Release: ${releaseUrl}`);
  }

  const allPassed = phaseOrder.every(p => !run.phases[p] || run.phases[p]?.status === 'passed');
  lines.push('');
  lines.push(allPassed ? '🚀 Sprint complete — all phases passed.' : '⚠️  Sprint ended with failures.');

  return {
    phase: 'reflect',
    status: 'passed',
    output: lines.join('\n'),
    timestamp,
  };
}
