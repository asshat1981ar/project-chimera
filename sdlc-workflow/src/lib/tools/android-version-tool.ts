import { ghHeaders } from './gh-headers';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

export interface VersionInfo {
  versionName: string;
  versionCode: number;
}

export function parseVersion(propertiesContent: string): VersionInfo {
  const nameMatch = propertiesContent.match(/^VERSION_NAME=(.+)$/m);
  const codeMatch = propertiesContent.match(/^VERSION_CODE=(\d+)$/m);
  return {
    versionName: nameMatch ? nameMatch[1].trim() : '1.0.0',
    versionCode: codeMatch ? parseInt(codeMatch[1], 10) : 1,
  };
}

export function bumpVersion(versionName: string, bumpType: 'patch' | 'minor' | 'major'): string {
  const parts = versionName.split('.').map(Number);
  const [major = 0, minor = 0, patch = 0] = parts;
  if (bumpType === 'major') return `${major + 1}.0.0`;
  if (bumpType === 'minor') return `${major}.${minor + 1}.0`;
  return `${major}.${minor}.${patch + 1}`;
}

function upsertProperty(content: string, key: string, value: string): string {
  const pattern = new RegExp(`^${key}=.*$`, 'm');
  if (pattern.test(content)) {
    return content.replace(pattern, `${key}=${value}`);
  }
  return content.trimEnd() + `\n${key}=${value}\n`;
}

export interface BumpResult {
  newVersionName: string;
  newVersionCode: number;
  oldVersionName: string;
  oldVersionCode: number;
}

export async function bumpAndroidVersionStep(
  branch: string,
  bumpType: 'patch' | 'minor' | 'major' = 'patch',
): Promise<BumpResult> {
  'use step';

  const headers = ghHeaders();
  const url = `${GH_API}/repos/${REPO}/contents/gradle.properties?ref=${encodeURIComponent(branch)}`;

  const getResp = await fetch(url, { headers });
  if (!getResp.ok) {
    throw new Error(`bumpAndroidVersion: GET gradle.properties failed ${getResp.status}`);
  }

  const data = await getResp.json() as { content: string; sha: string; encoding: string };
  const currentContent = Buffer.from(data.content.replace(/\n/g, ''), 'base64').toString('utf-8');

  const { versionName: oldVersionName, versionCode: oldVersionCode } = parseVersion(currentContent);
  const newVersionName = bumpVersion(oldVersionName, bumpType);
  const newVersionCode = oldVersionCode + 1;

  let updatedContent = upsertProperty(currentContent, 'VERSION_NAME', newVersionName);
  updatedContent = upsertProperty(updatedContent, 'VERSION_CODE', String(newVersionCode));

  const putBody = {
    message: `chore: bump Android version to ${newVersionName} (${newVersionCode})`,
    content: Buffer.from(updatedContent, 'utf-8').toString('base64'),
    sha: data.sha,
    branch,
  };

  const putResp = await fetch(`${GH_API}/repos/${REPO}/contents/gradle.properties`, {
    method: 'PUT',
    headers,
    body: JSON.stringify(putBody),
  });
  if (!putResp.ok) {
    const errText = await putResp.text();
    throw new Error(`bumpAndroidVersion: PUT failed ${putResp.status} — ${errText}`);
  }

  return { newVersionName, newVersionCode, oldVersionName, oldVersionCode };
}
