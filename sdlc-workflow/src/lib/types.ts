export type Phase =
  | 'sense'
  | 'plan'
  | 'gate'
  | 'implement'
  | 'validate'
  | 'sync'
  | 'release'
  | 'reflect';

export type PhaseStatus = 'pending' | 'running' | 'passed' | 'failed' | 'waiting_approval';

export interface PhaseResult {
  phase: Phase;
  status: PhaseStatus;
  output: string;
  timestamp: string;
}

export interface SprintRun {
  runId: string;
  sprintVersion: string;
  startedAt: string;
  currentPhase: Phase;
  phases: Partial<Record<Phase, PhaseResult>>;
}

export interface GatePayload {
  archViolations: string[];
  testBaselineCore: string;
  testBaselineDomain: string;
  buildSucceeded: boolean;
}

export interface ValidatePayload {
  testsPassed: boolean;
  testOutput: string;
  detektClean: boolean;
}

export interface ApprovePayload {
  decision: 'approved' | 'rejected';
  notes?: string;
  agentDispatchedAt?: string;
}
