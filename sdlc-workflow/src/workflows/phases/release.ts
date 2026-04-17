import type { PhaseResult } from '@/lib/types';

export async function runReleasePhase(
  sprintVersion: string,
  releaseNotes: string,
): Promise<PhaseResult> {
  'use step';
  const timestamp = new Date().toISOString();

  const githubPat = process.env.GITHUB_PAT;
  if (!githubPat) {
    return {
      phase: 'release',
      status: 'failed',
      output: 'GITHUB_PAT env var missing — cannot create GitHub Release',
      timestamp,
    };
  }

  let response: Response;
  try {
    response = await fetch(
      'https://api.github.com/repos/asshat1981ar/project-chimera/releases',
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${githubPat}`,
          'Content-Type': 'application/json',
          'X-GitHub-Api-Version': '2022-11-28',
        },
        body: JSON.stringify({
          tag_name: `v${sprintVersion}`,
          name: `Chimera v${sprintVersion}`,
          body: releaseNotes,
          draft: true,
          prerelease: true,
        }),
      },
    );
  } catch (err: unknown) {
    return {
      phase: 'release',
      status: 'failed',
      output: `GitHub Release fetch failed: ${err instanceof Error ? err.message : String(err)}`,
      timestamp,
    };
  }

  if (!response.ok) {
    const errText = await response.text();
    return {
      phase: 'release',
      status: 'failed',
      output: `GitHub Release API error ${response.status}: ${errText}`,
      timestamp,
    };
  }

  const release = await response.json() as { html_url: string };
  return {
    phase: 'release',
    status: 'passed',
    output: `Draft release created: ${release.html_url}`,
    timestamp,
  };
}
