# Security Policy

## API Key Management

**NEVER commit API keys, tokens, or secrets to this repository.**

### For Developers (Local Setup)

1. Copy `local.properties.example` to `local.properties`
2. Fill in your API keys in `local.properties`
3. `local.properties` is gitignored — it will never be committed

### For CI/CD

All secrets are injected via GitHub Actions secrets:
- `API_KEY_OPENAI` — OpenAI key for AI dialogue
- `API_KEY_GEMINI` — Google Gemini key

Add these in: **Settings → Secrets and variables → Actions → New repository secret**

### Reporting a Vulnerability

If you discover a security vulnerability, please open a **private security advisory** via GitHub's Security tab rather than a public issue.

If API keys have been accidentally committed:
1. Rotate the key immediately at the provider's dashboard
2. Remove the key from git history using `git filter-repo`
3. Force-push the cleaned history
4. Open a security issue

## Supported Versions

| Version | Supported |
|---------|-----------|
| v2.0.x  | ✅ Active |
| v1.x    | ❌ End of life |
