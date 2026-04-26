#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  tools/huggingface/hf_portrait_model_audit.sh [options]

Search Hugging Face text-to-image models and emit enriched candidate metadata for
Project Chimera NPC portrait generation.

Options:
  --search TEXT       Search query. Default: "portrait fantasy character"
  --limit N           Number of search results to enrich. Default: 10
  --model MODEL_ID    Also enrich this known model. May be repeated.
                      Default: black-forest-labs/FLUX.1-schnell
  --no-default-model  Do not append the current app default model.
  --format FORMAT     Output format: ndjson or table. Default: ndjson
  --help              Show this help.

Environment:
  HF_TOKEN            Optional Hugging Face token. When set, it is sent as a
                      Bearer token for higher rate limits and private/gated access.

Output:
  ndjson: one JSON object per model, sorted by fit score descending.
  table:  tabular summary for quick terminal inspection.
USAGE
}

SEARCH="portrait fantasy character"
LIMIT=10
FORMAT="ndjson"
INCLUDE_DEFAULT=1
MODELS=()
DEFAULT_MODEL="black-forest-labs/FLUX.1-schnell"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --search)
      SEARCH="${2:?missing value for --search}"
      shift 2
      ;;
    --limit)
      LIMIT="${2:?missing value for --limit}"
      shift 2
      ;;
    --model)
      MODELS+=("${2:?missing value for --model}")
      shift 2
      ;;
    --no-default-model)
      INCLUDE_DEFAULT=0
      shift
      ;;
    --format)
      FORMAT="${2:?missing value for --format}"
      shift 2
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

if [[ "$FORMAT" != "ndjson" && "$FORMAT" != "table" ]]; then
  echo "--format must be ndjson or table" >&2
  exit 2
fi

if ! [[ "$LIMIT" =~ ^[0-9]+$ ]] || [[ "$LIMIT" -lt 1 ]]; then
  echo "--limit must be a positive integer" >&2
  exit 2
fi

auth_args=()
if [[ -n "${HF_TOKEN:-}" ]]; then
  auth_args=(-H "Authorization: Bearer ${HF_TOKEN}")
fi

api_get() {
  curl -fsS "${auth_args[@]}" "$1"
}

urlencode() {
  jq -rn --arg v "$1" '$v | @uri'
}

encoded_search="$(urlencode "$SEARCH")"
search_url="https://huggingface.co/api/models?pipeline_tag=text-to-image&search=${encoded_search}&limit=${LIMIT}"

tmp_ids="$(mktemp)"
tmp_rows="$(mktemp)"
trap 'rm -f "$tmp_ids" "$tmp_rows"' EXIT

api_get "$search_url" | jq -r '.[]?.id // .[]?.modelId' > "$tmp_ids"

if [[ "$INCLUDE_DEFAULT" -eq 1 ]]; then
  printf '%s\n' "$DEFAULT_MODEL" >> "$tmp_ids"
fi

for model in "${MODELS[@]}"; do
  printf '%s\n' "$model" >> "$tmp_ids"
done

sort -u "$tmp_ids" | while IFS= read -r model_id; do
  [[ -n "$model_id" ]] || continue
  api_get "https://huggingface.co/api/models/${model_id}" | jq -c '
    def license:
      (.cardData.license // ([.tags[]? | select(startswith("license:")) | sub("^license:";"")] | first) // null);
    def has_tag($tag): any(.tags[]?; . == $tag);
    def fit_score:
      (if (.pipeline_tag == "text-to-image") then 20 else 0 end) +
      (if has_tag("endpoints_compatible") then 20 else 0 end) +
      (if (.gated == false or .gated == null) then 20 else 0 end) +
      (if (.disabled == false or .disabled == null) then 10 else 0 end) +
      (if (.inference == "warm") then 10 elif (.inference == "cold") then 5 else 0 end) +
      (((.downloads // 0) | tonumber) as $d | if $d > 100000 then 10 elif $d > 10000 then 7 elif $d > 1000 then 4 elif $d > 0 then 1 else 0 end) +
      (((.likes // 0) | tonumber) as $l | if $l > 1000 then 10 elif $l > 100 then 6 elif $l > 10 then 3 else 0 end);
    {
      id: (.id // .modelId),
      fitScore: fit_score,
      pipeline: .pipeline_tag,
      library: .library_name,
      diffusersClass: .config.diffusers._class_name,
      license: license,
      gated: (.gated // false),
      disabled: (.disabled // false),
      inference: (.inference // null),
      endpointCompatible: has_tag("endpoints_compatible"),
      downloads: (.downloads // 0),
      likes: (.likes // 0),
      lastModified: .lastModified,
      tags: [.tags[]?],
      fitNotes: [
        (if has_tag("endpoints_compatible") then "endpoint-compatible" else "check endpoint support" end),
        (if (.gated == false or .gated == null) then "not gated" else "gated:" + (.gated | tostring) end),
        (if (license != null) then "license:" + license else "license missing" end),
        (if (.inference == "warm") then "inference warm" elif (.inference == "cold") then "inference cold" else "inference status unknown" end)
      ]
    }' >> "$tmp_rows"
done

if [[ "$FORMAT" == "ndjson" ]]; then
  jq -s -c 'sort_by(.fitScore, .downloads, .likes) | reverse | .[]' "$tmp_rows"
else
  jq -s -r '
    sort_by(.fitScore, .downloads, .likes) | reverse |
    (["score","downloads","likes","gated","endpoint","license","inference","id"] | @tsv),
    (.[] | [.fitScore, .downloads, .likes, .gated, .endpointCompatible, (.license // ""), (.inference // ""), .id] | @tsv)
  ' "$tmp_rows" | column -t -s $'\t'
fi
