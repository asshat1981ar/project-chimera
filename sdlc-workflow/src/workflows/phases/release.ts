import type { PhaseResult } from '@/lib/types';

export async function runReleasePhase(
  sprintVersion: string,
  releaseNotes: string,
): Promise<PhaseResult> {
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

  const response = await fetch(
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

  if (!response.ok) {
    const err = await response.text();
    return {
      phase: 'release',
      status: 'failed',
      output: `GitHub Release API error ${response.status}: ${err}`,
      timestamp,
    };
  }

  const release = await response.json();
  return {
    phase: 'release',
    status: 'passed',
    output: `Draft release created: ${release.html_url}`,
    timestamp,
  };
}
