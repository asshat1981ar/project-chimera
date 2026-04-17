import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { bumpAndroidVersionStep, parseVersion, bumpVersion } from './android-version-tool';

const REPO = 'asshat1981ar/project-chimera';
const GH_API = 'https://api.github.com';

const PROPS_WITH_VERSION = `org.gradle.jvmargs=-Xmx2048m\nVERSION_NAME=1.2.3\nVERSION_CODE=42\n`;
const PROPS_WITHOUT_VERSION = `org.gradle.jvmargs=-Xmx2048m\nkotlin.code.style=official\n`;

function encodeBase64(s: string) {
  return Buffer.from(s, 'utf-8').toString('base64');
}

beforeEach(() => {
  vi.stubEnv('GH_DISPATCH_TOKEN', 'test-token');
  vi.resetAllMocks();
});

afterEach(() => {
  vi.unstubAllEnvs();
});

describe('parseVersion', () => {
  it('parses VERSION_NAME and VERSION_CODE from properties', () => {
    const result = parseVersion(PROPS_WITH_VERSION);
    expect(result.versionName).toBe('1.2.3');
    expect(result.versionCode).toBe(42);
  });

  it('returns defaults when properties are absent', () => {
    const result = parseVersion(PROPS_WITHOUT_VERSION);
    expect(result.versionName).toBe('1.0.0');
    expect(result.versionCode).toBe(1);
  });
});

describe('bumpVersion', () => {
  it('increments patch version', () => {
    expect(bumpVersion('1.2.3', 'patch')).toBe('1.2.4');
  });

  it('increments minor version and resets patch', () => {
    expect(bumpVersion('1.2.3', 'minor')).toBe('1.3.0');
  });

  it('increments major version and resets minor and patch', () => {
    expect(bumpVersion('1.2.3', 'major')).toBe('2.0.0');
  });
});

describe('bumpAndroidVersionStep', () => {
  it('reads gradle.properties, bumps patch, and writes back', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITH_VERSION), sha: 'abc123', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    const result = await bumpAndroidVersionStep('feat/my-branch', 'patch');

    expect(result.newVersionName).toBe('1.2.4');
    expect(result.newVersionCode).toBe(43);
    expect(fetchMock.mock.calls[1][0]).toContain('/contents/gradle.properties');
    expect(fetchMock.mock.calls[1][1]?.method).toBe('PUT');
  });

  it('upserts VERSION_NAME and VERSION_CODE when absent', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITHOUT_VERSION), sha: 'def456', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    const result = await bumpAndroidVersionStep('main', 'patch');

    expect(result.newVersionName).toBe('1.0.1');
    expect(result.newVersionCode).toBe(2);
    const putBody = JSON.parse(fetchMock.mock.calls[1][1]?.body as string);
    const written = Buffer.from(putBody.content, 'base64').toString('utf-8');
    expect(written).toContain('VERSION_NAME=1.0.1');
    expect(written).toContain('VERSION_CODE=2');
  });

  it('throws if GH_DISPATCH_TOKEN is not set', async () => {
    vi.unstubAllEnvs();
    await expect(bumpAndroidVersionStep('main', 'patch')).rejects.toThrow('GH_DISPATCH_TOKEN');
  });

  it('throws if GET gradle.properties fails', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;
    fetchMock.mockResolvedValueOnce({ ok: false, status: 404, text: async () => 'Not Found' });

    await expect(bumpAndroidVersionStep('main', 'patch')).rejects.toThrow('404');
  });

  it('throws if PUT fails', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITH_VERSION), sha: 'abc123', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: false, status: 409, text: async () => 'Conflict' });

    await expect(bumpAndroidVersionStep('feat/my-branch', 'patch')).rejects.toThrow('409');
  });

  it('includes sha in PUT body to prevent conflicts', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITH_VERSION), sha: 'mysha123', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    await bumpAndroidVersionStep('feat/my-branch', 'patch');

    const putBody = JSON.parse(fetchMock.mock.calls[1][1]?.body as string);
    expect(putBody.sha).toBe('mysha123');
    expect(putBody.branch).toBe('feat/my-branch');
  });

  it('returns old and new version information', async () => {
    const fetchMock = vi.fn();
    global.fetch = fetchMock;

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ content: encodeBase64(PROPS_WITH_VERSION), sha: 'abc123', encoding: 'base64' }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    const result = await bumpAndroidVersionStep('feat/my-branch', 'minor');

    expect(result.oldVersionName).toBe('1.2.3');
    expect(result.oldVersionCode).toBe(42);
    expect(result.newVersionName).toBe('1.3.0');
    expect(result.newVersionCode).toBe(43);
  });
});
