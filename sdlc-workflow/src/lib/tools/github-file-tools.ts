const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

function ghHeaders(): Record<string, string> {
  const token = process.env.GH_DISPATCH_TOKEN;
  if (!token) throw new Error('GH_DISPATCH_TOKEN environment variable is not set');
  return {
    Authorization: `Bearer ${token}`,
    Accept: 'application/vnd.github+json',
    'X-GitHub-Api-Version': '2022-11-28',
    'Content-Type': 'application/json',
  };
}

export async function readFile(path: string, branch: string): Promise<string> {
  'use step';
  const resp = await fetch(
    `${GH_API}/repos/${REPO}/contents/${path}?ref=${encodeURIComponent(branch)}`,
    { headers: ghHeaders() },
  );
  if (!resp.ok) {
    if (resp.status === 404) return `[FILE NOT FOUND: ${path}]`;
    throw new Error(`readFile(${path}) failed: ${resp.status}`);
  }
  const data = await resp.json() as { content: string; encoding: string };
  if (data.encoding !== 'base64') throw new Error(`Unexpected encoding: ${data.encoding}`);
  return Buffer.from(data.content.replace(/\n/g, ''), 'base64').toString('utf-8');
}

export async function listDirectory(path: string, branch: string): Promise<string[]> {
  'use step';
  const resp = await fetch(
    `${GH_API}/repos/${REPO}/contents/${path}?ref=${encodeURIComponent(branch)}`,
    { headers: ghHeaders() },
  );
  if (!resp.ok) {
    if (resp.status === 404) return [];
    throw new Error(`listDirectory(${path}) failed: ${resp.status}`);
  }
  const body = await resp.json();
  if (!Array.isArray(body)) {
    throw new Error(`listDirectory(${path}) returned a file, not a directory`);
  }
  const items = body as Array<{ name: string; type: string; path: string }>;
  return items.map(i => `${i.type === 'dir' ? '[dir]' : '[file]'} ${i.path}`);
}

export async function writeFile(
  path: string,
  content: string,
  branch: string,
  commitMessage: string,
): Promise<void> {
  'use step';
  let sha: string | undefined;
  const existing = await fetch(
    `${GH_API}/repos/${REPO}/contents/${path}?ref=${encodeURIComponent(branch)}`,
    { headers: ghHeaders() },
  );
  if (existing.ok) {
    const data = await existing.json() as { sha: string };
    sha = data.sha;
  } else if (existing.status !== 404) {
    throw new Error(`writeFile pre-flight GET(${path}) failed: ${existing.status}`);
  }

  const body: Record<string, unknown> = {
    message: commitMessage,
    content: Buffer.from(content, 'utf-8').toString('base64'),
    branch,
  };
  if (sha) body.sha = sha;

  const resp = await fetch(`${GH_API}/repos/${REPO}/contents/${path}`, {
    method: 'PUT',
    headers: ghHeaders(),
    body: JSON.stringify(body),
  });
  if (!resp.ok) {
    const text = await resp.text();
    throw new Error(`writeFile(${path}) failed: ${resp.status} — ${text}`);
  }
}

export async function searchCode(query: string, extension: string): Promise<string> {
  'use step';
  const q = encodeURIComponent(`${query} repo:${REPO} extension:${extension}`);
  const resp = await fetch(`${GH_API}/search/code?q=${q}&per_page=10`, {
    headers: ghHeaders(),
  });
  if (!resp.ok) throw new Error(`searchCode failed: ${resp.status}`);
  const data = await resp.json() as {
    total_count: number;
    items: Array<{ path: string; html_url: string }>;
  };
  if (data.total_count === 0) return '[No results]';
  return data.items.map(i => i.path).join('\n');
}
