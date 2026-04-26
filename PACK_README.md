# Project Chimera Roadmap Repo Pack

This pack is intended to be copied into the root of `asshat1981ar/project-chimera`.

## Contents

- `ROADMAP.md`
- `docs/roadmap/2026-04-26-ui-ux-expansion-roadmap.md`
- `docs/sprints/2026-04-26-ui-ux-expansion-sprint-pack.md`
- `docs/issues/2026-04-26-ui-ux-github-issues.md`
- `docs/issues/2026-04-26-ui-ux-github-issues.csv`
- `docs/issues/2026-04-26-ui-ux-github-issues.jsonl`
- `docs/prompts/2026-04-26-ui-ux-coding-agent-prompt.md`
- `.github/ISSUE_TEMPLATE/chimera_delivery_task.yml`
- `scripts/github/create-ui-ux-roadmap-issues.sh`

## Apply manually

```bash
unzip chimera-roadmap-repo-pack.zip -d /tmp/chimera-roadmap-pack
cp -R /tmp/chimera-roadmap-pack/* /path/to/project-chimera/
cd /path/to/project-chimera
git checkout -b docs/ui-ux-expansion-roadmap
git add ROADMAP.md docs/ .github/ISSUE_TEMPLATE/chimera_delivery_task.yml scripts/github/create-ui-ux-roadmap-issues.sh
git commit -m "docs: add UI/UX expansion roadmap and issue seed list"
git push -u origin docs/ui-ux-expansion-roadmap
```

## Create GitHub issues

After the docs are committed and `gh auth login` is complete:

```bash
bash scripts/github/create-ui-ux-roadmap-issues.sh asshat1981ar/project-chimera
```
