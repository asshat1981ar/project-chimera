import { defineHook } from 'workflow';
import type { ApprovePayload } from './types';

// Pause IMPLEMENT phase until a human/agent POSTs to /api/chimera-sdlc/approve
export const implementApprovalHook = defineHook<ApprovePayload>();
