#!/usr/bin/env bash
# Pre-commit: block commits violating chimera-core Android-free or cross-feature rules
STAGED=$(git diff --cached --name-only | grep "\.kt$" || true)
[ -z "$STAGED" ] && exit 0

CHIMERA_STAGED=$(echo "$STAGED" | grep "^chimera-core/" || true)
if [ -n "$CHIMERA_STAGED" ]; then
  VIOLATIONS=$(git diff --cached -- $CHIMERA_STAGED | grep "^+" | \
    grep -E "^\+import android\.|^\+import androidx\." || true)
  if [ -n "$VIOLATIONS" ]; then
    echo "PRE-COMMIT BLOCKED: chimera-core Android import:"
    echo "$VIOLATIONS"
    echo "Fix: move to core-* or inject via interface."
    exit 1
  fi
fi

FEATURE_STAGED=$(echo "$STAGED" | grep "^feature-" || true)
if [ -n "$FEATURE_STAGED" ]; then
  CROSS=$(git diff --cached -- $FEATURE_STAGED | grep "^+" | \
    grep "import com\.chimera\.feature\." || true)
  if [ -n "$CROSS" ]; then
    echo "PRE-COMMIT BLOCKED: cross-feature import:"
    echo "$CROSS"
    echo "Fix: move shared type to core-model."
    exit 1
  fi
fi
exit 0
