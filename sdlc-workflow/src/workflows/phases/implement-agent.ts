import { createHook, getWritable } from 'workflow';
import { DurableAgent } from '@workflow/ai/agent';
import { tool } from 'ai';
import { z } from 'zod';
import { readFile, writeFile, listDirectory, searchCode } from '@/lib/tools/github-file-tools';
import { buildImplementSystemPrompt } from '@/lib/prompts/implement-system-prompt';
import type { PhaseResult, ReviewPayload } from '@/lib/types';

async function dispatchCiWorkflow(runId: string, sprintVersion: string, branch: string): Promise<void> {
  'use step';
  const token = process.env.GH_DISPATCH_TOKEN;
  if (!token) throw new Error('GH_DISPATCH_TOKEN is not set');
  const resp = await fetch(
    'https://api.github.com/repos/asshat1981ar/project-chimera/actions/workflows/sdlc-validate.yml/dispatches',
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/vnd.github+json',
        'X-GitHub-Api-Version': '2022-11-28',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ref: branch,
        inputs: { run_id: runId, sprint_version: sprintVersion },
      }),
    },
  );
  if (!resp.ok) {
    const text = await resp.text();
    throw new Error(`GH CI dispatch failed: ${resp.status} — ${text}`);
  }
  console.log(`[AGENT] CI dispatched for ${runId} on branch ${branch}`);
}

async function runAgent(taskManifest: string, branch: string): Promise<string> {
  'use step';

  const agentTools = {
    readFile: tool({
      description: 'Read a file from the Chimera repository. Returns the full text content.',
      inputSchema: z.object({
        path: z.string().describe('Relative file path from repo root, e.g. domain/src/main/kotlin/com/chimera/domain/Foo.kt'),
      }),
      execute: async ({ path }) => readFile(path, branch),
    }),
    writeFile: tool({
      description: 'Write or update a file in the Chimera repository. Creates a commit on the sprint branch.',
      inputSchema: z.object({
        path: z.string().describe('Relative file path from repo root'),
        content: z.string().describe('Full file content to write'),
        commitMessage: z.string().describe('Git commit message, e.g. feat(domain): add FooUseCase'),
      }),
      execute: async ({ path, content, commitMessage }) =>
        writeFile(path, content, branch, commitMessage).then(() => `Written: ${path}`),
    }),
    listDirectory: tool({
      description: 'List files and subdirectories at a given path in the repository.',
      inputSchema: z.object({
        path: z.string().describe('Directory path, e.g. domain/src/main/kotlin/com/chimera/domain'),
      }),
      execute: async ({ path }) => listDirectory(path, branch).then(entries => entries.join('\n')),
    }),
    searchCode: tool({
      description: 'Search the repository for code matching a query. Returns matching file paths.',
      inputSchema: z.object({
        query: z.string().describe('Search query, e.g. "@HiltViewModel" or "class SaveRepository"'),
        extension: z.string().describe('File extension to search, e.g. "kt" or "kts"'),
      }),
      execute: async ({ query, extension }) => searchCode(query, extension),
    }),
  };

  const agent = new DurableAgent({
    model: 'anthropic/claude-sonnet-4-6',
    system: buildImplementSystemPrompt(branch),
    tools: agentTools,
  });

  const result = await agent.stream({
    messages: [{ role: 'user', content: taskManifest }],
    writable: getWritable(),
    maxSteps: 40,
  });

  const lastMessage = result.messages.at(-1);
  if (lastMessage?.role !== 'assistant') return 'Agent completed without a final message.';
  const content = lastMessage.content;
  if (typeof content === 'string') return content;
  return (content as Array<{ type: string; text?: string }>)
    .filter(p => p.type === 'text')
    .map(p => p.text ?? '')
    .join('');
}

export async function runImplementAgentPhase(
  runId: string,
  taskManifest: string,
  sprintVersion: string,
  branch: string,
): Promise<PhaseResult> {
  'use workflow';

  const timestamp = new Date().toISOString();
  console.log(`[AGENT] Starting autonomous implementation for run ${runId} on ${branch}`);

  const agentSummary = await runAgent(taskManifest, branch);

  console.log(`[AGENT] Implementation complete. Summary:\n${agentSummary}`);
  console.log(`[AGENT] Pausing for human review — use POST /api/chimera-sdlc/review to approve/reject`);

  const reviewHook = createHook<ReviewPayload>({ token: `${runId}-review` });

  for await (const event of reviewHook) {
    if (event.decision === 'approved') {
      await dispatchCiWorkflow(runId, sprintVersion, branch);
      return {
        phase: 'implement',
        status: 'passed',
        output: `Agent implementation approved. CI dispatched on ${branch}.\n\nAgent summary:\n${agentSummary}`,
        timestamp: new Date().toISOString(),
      };
    } else {
      return {
        phase: 'implement',
        status: 'failed',
        output: `Agent implementation rejected. Notes: ${event.notes ?? 'none'}\n\nAgent summary:\n${agentSummary}`,
        timestamp: new Date().toISOString(),
      };
    }
  }

  return {
    phase: 'implement',
    status: 'failed',
    output: 'Review hook closed without event',
    timestamp,
  };
}
