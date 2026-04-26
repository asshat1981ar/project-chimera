# Hugging Face Tooling Prompt

Project Chimera is an Android-first Kotlin RPG. Hugging Face usage should stay in support of the existing app, especially NPC portrait generation, and should not introduce a second product surface.

## Self-Prompt

Use this prompt when generating or extending repo-local Hugging Face tools:

```text
You are adding reusable Hugging Face API command-line tools to Project Chimera.

Repository context:
- Android-first Kotlin app with modules `app`, `chimera-core`, `core-model`, `core-ui`, `core-database`, `core-network`, `core-ai`, `core-data`, `domain`, and `feature-*`.
- AI is optional adapter behavior. The game must keep working offline through authored fallbacks.
- `core-ai/src/main/kotlin/com/chimera/ai/PortraitGenerationService.kt` currently calls the Hugging Face Inference API for NPC portraits.
- The app's checked-in portrait default is `stabilityai/stable-diffusion-xl-base-1.0`, but the repo-local probe should prefer a currently supported HF router model such as `black-forest-labs/FLUX.1-schnell`.
- App builds read `HUGGING_FACE_TOKEN` from Gradle properties; repo-local CLI tools must read `HF_TOKEN` from the environment.
- Existing NPC/content source data lives under `app/src/main/assets/`.

Build tools that:
- Help evaluate or smoke-test text-to-image models for dark fantasy NPC portrait generation.
- Use `HF_TOKEN` as a bearer token when calling Hugging Face APIs.
- Provide `--help`.
- Emit shell-friendly JSON or NDJSON by default.
- Are non-destructive unless explicitly given an output path.
- Do not modify Android source, Gradle files, model defaults, generated assets, or local.properties.
- Prefer composable scripts under `tools/huggingface/`.

Useful first tools:
1. Search and rank public text-to-image models for portrait suitability.
2. Probe the current or candidate portrait model with a Project Chimera-style prompt.
3. Enrich model IDs from stdin with license, gated status, pipeline, inference status, and endpoint compatibility.
4. Generate candidate portrait prompts from `app/src/main/assets/npcs.json` without making API calls by default.

Before changing app behavior, inspect code and verify root Gradle commands from the repository root.
```

## Tools

- `hf_portrait_model_audit.sh`: searches Hugging Face text-to-image models and emits enriched candidate metadata.
- `hf_portrait_probe.sh`: builds the same style of portrait prompt used by the app and can dry-run or call the Inference API.

These scripts are developer tooling only. They do not change app code, build configuration, generated assets, or the checked-in portrait manifest.
