#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  tools/huggingface/hf_portrait_probe.sh [options]

Build a Project Chimera-style NPC portrait prompt and optionally call the
Hugging Face Inference API for a candidate text-to-image model.

Options:
  --model MODEL_ID    Hugging Face model ID.
                      Default: black-forest-labs/FLUX.1-schnell
  --name NAME         NPC name. Default: The Warden
  --role ROLE         NPC role. Default: NPC_NEUTRAL
  --title TITLE       NPC title. Default: Keeper of the Gate
  --steps N           num_inference_steps. Default: 20
  --output-dir DIR    Directory for generated image. Default: /tmp/chimera-hf
  --dry-run           Print request JSON and do not call the API.
  --help              Show this help.

Environment:
  HF_TOKEN            Required unless --dry-run is used. Sent as a Bearer token.

Output:
  JSON summary containing the model, prompt, HTTP status, content type, and
  output image path when an image is generated.
USAGE
}

MODEL="black-forest-labs/FLUX.1-schnell"
NAME="The Warden"
ROLE="NPC_NEUTRAL"
TITLE="Keeper of the Gate"
STEPS=20
OUTPUT_DIR="/tmp/chimera-hf"
DRY_RUN=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --model)
      MODEL="${2:?missing value for --model}"
      shift 2
      ;;
    --name)
      NAME="${2:?missing value for --name}"
      shift 2
      ;;
    --role)
      ROLE="${2:?missing value for --role}"
      shift 2
      ;;
    --title)
      TITLE="${2:?missing value for --title}"
      shift 2
      ;;
    --steps)
      STEPS="${2:?missing value for --steps}"
      shift 2
      ;;
    --output-dir)
      OUTPUT_DIR="${2:?missing value for --output-dir}"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required." >&2
  exit 1
fi

if ! [[ "$STEPS" =~ ^[0-9]+$ ]] || [[ "$STEPS" -lt 1 ]]; then
  echo "--steps must be a positive integer" >&2
  exit 2
fi

role_upper="$(printf '%s' "$ROLE" | tr '[:lower:]' '[:upper:]')"
case "$role_upper" in
  COMPANION|NPC_ALLY)
    role_hint="trusted companion, loyal ally"
    ;;
  ANTAGONIST|NPC_HOSTILE|FACTION_LEADER)
    role_hint="menacing villain, threatening"
    ;;
  MENTOR)
    role_hint="wise elder, experienced guide"
    ;;
  MERCHANT)
    role_hint="traveling merchant, weathered"
    ;;
  GUARDIAN)
    role_hint="armored guardian, stoic warrior"
    ;;
  *)
    role_hint="fantasy NPC"
    ;;
esac

title_part=""
if [[ -n "$TITLE" ]]; then
  title_part="${TITLE}, "
fi

prompt="detailed fantasy RPG portrait of ${NAME}, ${title_part}${role_hint}, dark fantasy art, dramatic lighting, highly detailed face, character portrait, professional concept art"
endpoint="https://router.huggingface.co/hf-inference/models/${MODEL}"
payload="$(jq -cn --arg prompt "$prompt" --argjson steps "$STEPS" '{inputs:$prompt, parameters:{num_inference_steps:$steps}}')"

if [[ "$DRY_RUN" -eq 1 ]]; then
  jq -cn \
    --arg model "$MODEL" \
    --arg endpoint "$endpoint" \
    --arg prompt "$prompt" \
    --argjson request "$payload" \
    '{dryRun:true, model:$model, endpoint:$endpoint, prompt:$prompt, request:$request}'
  exit 0
fi

if [[ -z "${HF_TOKEN:-}" ]]; then
  echo "HF_TOKEN is required unless --dry-run is used." >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR"
safe_name="$(printf '%s' "$NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '_' | sed 's/^_//;s/_$//')"
output_path="${OUTPUT_DIR}/${safe_name:-npc}.jpg"
headers_file="$(mktemp)"
body_file="$(mktemp)"
trap 'rm -f "$headers_file" "$body_file"' EXIT

status="$(
  curl -sS \
    -w '%{http_code}' \
    -D "$headers_file" \
    -o "$body_file" \
    -H "Authorization: Bearer ${HF_TOKEN}" \
    -H "Content-Type: application/json" \
    --data "$payload" \
    "$endpoint"
)"

content_type="$(awk 'BEGIN{IGNORECASE=1} /^content-type:/ {print $2; exit}' "$headers_file" | tr -d '\r')"

if [[ "$status" =~ ^2 && "$content_type" == image/* ]]; then
  mv "$body_file" "$output_path"
  jq -cn \
    --arg model "$MODEL" \
    --arg prompt "$prompt" \
    --arg status "$status" \
    --arg contentType "$content_type" \
    --arg outputPath "$output_path" \
    '{ok:true, model:$model, prompt:$prompt, status:($status|tonumber), contentType:$contentType, outputPath:$outputPath}'
else
  error_body="$(tr -d '\000' < "$body_file" | head -c 2000)"
  jq -cn \
    --arg model "$MODEL" \
    --arg prompt "$prompt" \
    --arg status "$status" \
    --arg contentType "$content_type" \
    --arg error "$error_body" \
    '{ok:false, model:$model, prompt:$prompt, status:($status|tonumber), contentType:$contentType, error:$error}'
  exit 1
fi
