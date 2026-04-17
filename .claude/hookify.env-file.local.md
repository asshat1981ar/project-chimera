---
name: env-file-guard
enabled: true
event: file
conditions:
  - field: file_path
    operator: regex_match
    pattern: \.env$|\.env\.
---

**You are editing a .env file.**

Verify that:
1. This file is listed in `.gitignore`
2. No real credentials are being committed
3. The file uses placeholder values for secrets (e.g. `API_KEY=REPLACE_ME`)
