import { describe, it, expect, vi, beforeEach } from 'vitest';

const mockFetch = vi.fn();
global.fetch = mockFetch as unknown as typeof fetch;
process.env.GH_DISPATCH_TOKEN = 'test-token';

import { readFile, listDirectory, writeFile, searchCode } from './github-file-tools';

describe('readFile', () => {
  beforeEach(() => mockFetch.mockReset());

  it('decodes base64 file content', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        content: Buffer.from('class Foo {}', 'utf-8').toString('base64'),
        encoding: 'base64',
      }),
    });

    const result = await readFile('src/Foo.kt', 'main');
    expect(result).toBe('class Foo {}');
  });

  it('returns placeholder for 404', async () => {
    mockFetch.mockResolvedValueOnce({ ok: false, status: 404 });
    const result = await readFile('missing.kt', 'main');
    expect(result).toBe('[FILE NOT FOUND: missing.kt]');
  });

  it('throws if GH_DISPATCH_TOKEN is missing', async () => {
    const saved = process.env.GH_DISPATCH_TOKEN;
    delete process.env.GH_DISPATCH_TOKEN;
    await expect(readFile('any.kt', 'main')).rejects.toThrow('GH_DISPATCH_TOKEN');
    process.env.GH_DISPATCH_TOKEN = saved;
  });
});

describe('writeFile', () => {
  beforeEach(() => mockFetch.mockReset());

  it('creates new file when none exists (no sha)', async () => {
    mockFetch.mockResolvedValueOnce({ ok: false, status: 404 });
    mockFetch.mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    await expect(writeFile('new.kt', 'content', 'main', 'feat: add file')).resolves.toBeUndefined();

    const putCall = mockFetch.mock.calls[1];
    const body = JSON.parse(putCall[1].body as string);
    expect(body.sha).toBeUndefined();
    expect(body.branch).toBe('main');
  });

  it('includes sha when updating existing file', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ sha: 'abc123' }),
    });
    mockFetch.mockResolvedValueOnce({ ok: true, json: async () => ({}) });

    await writeFile('existing.kt', 'updated', 'feat-branch', 'chore: update');

    const putCall = mockFetch.mock.calls[1];
    const body = JSON.parse(putCall[1].body as string);
    expect(body.sha).toBe('abc123');
  });

  it('throws on non-404 pre-flight error', async () => {
    mockFetch.mockResolvedValueOnce({ ok: false, status: 403 });
    await expect(writeFile('file.kt', 'content', 'main', 'msg')).rejects.toThrow('pre-flight GET');
  });
});

describe('listDirectory', () => {
  beforeEach(() => mockFetch.mockReset());

  it('returns formatted paths', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        { name: 'Foo.kt', type: 'file', path: 'src/main/Foo.kt' },
        { name: 'sub', type: 'dir', path: 'src/main/sub' },
      ],
    });

    const result = await listDirectory('src/main', 'main');
    expect(result).toContain('[file] src/main/Foo.kt');
    expect(result).toContain('[dir] src/main/sub');
  });

  it('returns empty array for 404', async () => {
    mockFetch.mockResolvedValueOnce({ ok: false, status: 404 });
    const result = await listDirectory('missing/', 'main');
    expect(result).toEqual([]);
  });

  it('throws when path points to a file, not directory', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ name: 'Foo.kt', type: 'file', path: 'src/Foo.kt' }),
    });
    await expect(listDirectory('src/Foo.kt', 'main')).rejects.toThrow('returned a file');
  });
});

describe('searchCode', () => {
  beforeEach(() => mockFetch.mockReset());

  it('returns file paths for search results', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        total_count: 2,
        items: [
          { path: 'domain/src/Foo.kt', html_url: 'https://github.com/...' },
          { path: 'feature-home/src/Bar.kt', html_url: 'https://github.com/...' },
        ],
      }),
    });

    const result = await searchCode('@HiltViewModel', 'kt');
    expect(result).toContain('domain/src/Foo.kt');
    expect(result).toContain('feature-home/src/Bar.kt');
  });

  it('returns no-results placeholder for zero count', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ total_count: 0, items: [] }),
    });

    const result = await searchCode('NonExistentClass', 'kt');
    expect(result).toBe('[No results]');
  });

  it('throws on HTTP error', async () => {
    mockFetch.mockResolvedValueOnce({ ok: false, status: 429 });
    await expect(searchCode('query', 'kt')).rejects.toThrow('searchCode failed: 429');
  });
});
