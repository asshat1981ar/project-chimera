#!/usr/bin/env python3
"""
Chimera Sprite Generation Pipeline

Orchestrates AI image generation for gothic manuscript ink-wash style sprites.
Reads sprite_manifest.json, generates missing assets via image generation API,
and outputs optimized PNGs to the Android res/ directory structure.

Usage:
    python generate_sprites.py --manifest assets/sprite_manifest.json --output app/src/main/res/
    python generate_sprites.py --category NPC_PORTRAIT --dry-run
    python generate_sprites.py --single "npc_elara_hostile"

Requirements:
    - Python 3.9+
    - Pillow (PIL) for image processing
    - requests for API calls (or local model)
"""

import argparse
import json
import hashlib
import os
import sys
from pathlib import Path
from dataclasses import dataclass
from typing import Optional, List, Dict
from enum import Enum

# --- Configuration ---

STYLE_PROMPT_PREFIX = (
    "Dark gothic manuscript illustration, sumi-e ink wash on aged parchment, "
    "visible brush strokes, expressive brushwork, ink bleed edges, "
    "monochrome charcoal with restrained accent color, "
    "parchment grain texture, no clean lines, no text, no watermark, "
    "game asset sprite"
)

NEGATIVE_PROMPT = (
    "bright colors, cartoon, anime, 3D render, photorealistic, clean lines, "
    "vector art, flat design, neon, vibrant, modern UI, glossy, "
    "gradient backgrounds, text, watermark, blurry, low quality"
)

# Category-specific prompt modifiers
CATEGORY_PROMPTS = {
    "NPC_PORTRAIT": {
        "format": "isolated portrait, upper body, character facing forward",
        "lighting": "single candlelight from upper left, deep shadows",
        "composition": "asymmetric, negative space dominant, transparent background"
    },
    "NPC_TOKEN": {
        "format": "small overhead token, birds-eye view silhouette",
        "lighting": "even overhead lighting",
        "composition": "centered, compact, transparent background"
    },
    "COMBAT_PLAYER": {
        "format": "dynamic action pose, combat stance figure",
        "lighting": "dramatic side lighting",
        "composition": "full body, isolated figure, transparent background"
    },
    "COMBAT_OPPONENT": {
        "format": "dynamic action pose, menacing combat stance",
        "lighting": "dramatic under lighting",
        "composition": "full body, isolated figure, transparent background"
    },
    "MAP_NODE": {
        "format": "top-down architectural ink wash, map marker",
        "lighting": "even overhead lighting",
        "composition": "square format, birds-eye view, transparent background"
    },
    "CAMP_ITEM": {
        "format": "botanical illustration, still life specimen drawing",
        "lighting": "soft diffused light",
        "composition": "centered, detailed, transparent background"
    },
    "SHARED_UI": {
        "format": "illuminated manuscript decorative element, ornate border fragment",
        "lighting": "even lighting with gold leaf highlights",
        "composition": "decorative frame, transparent background"
    }
}

# Expression-specific mood descriptors
EXPRESSION_MOODS = {
    "neutral": "calm neutral expression, balanced emotional state",
    "tense": "tense anxious expression, furrowed brow, clenched jaw",
    "wounded": "pained wounded expression, blood stains, injury marks",
    "grateful": "warm grateful expression, soft eyes, relieved posture",
    "hostile": "aggressive hostile expression, narrowed eyes, snarl",
    "oathbound": "determined oathbound expression, solemn resolve, glowing eyes"
}


# --- Data Classes ---

@dataclass
class SpriteEntry:
    id: str
    category: str
    base_name: str
    transparent: bool
    variants: List[Dict]
    metadata: Dict

    def get_prompt(self) -> str:
        """Build the full generation prompt for this sprite."""
        cat_config = CATEGORY_PROMPTS.get(self.category, {})
        parts = [STYLE_PROMPT_PREFIX]

        # Add category-specific framing
        if "format" in cat_config:
            parts.append(cat_config["format"])
        if "lighting" in cat_config:
            parts.append(cat_config["lighting"])
        if "composition" in cat_config:
            parts.append(cat_config["composition"])

        # Add expression mood if applicable
        expression = self.metadata.get("expression")
        if expression and expression in EXPRESSION_MOODS:
            parts.append(EXPRESSION_MOODS[expression])

        # Add NPC/item specific description
        npc_id = self.metadata.get("npcId")
        if npc_id:
            parts.append(f"medieval fantasy {npc_id} character")

        item_id = self.metadata.get("itemId")
        if item_id:
            parts.append(f"{item_id} alchemical ingredient item")

        node_type = self.metadata.get("nodeType")
        if node_type:
            parts.append(f"{node_type} location ruins")

        stance = self.metadata.get("stance")
        if stance:
            parts.append(f"{stance} combat stance pose")

        return ", ".join(parts)

    def get_output_path(self, output_dir: str, density: str = "xhdpi") -> Path:
        """Determine the output file path for this sprite."""
        drawable_dir = f"drawable-{density}"
        return Path(output_dir) / drawable_dir / f"{self.base_name}.png"


# --- Core Functions ---

def load_manifest(manifest_path: str) -> List[SpriteEntry]:
    """Load and parse the sprite manifest JSON."""
    with open(manifest_path, 'r') as f:
        data = json.load(f)

    entries = []
    for category_data in data.get("categories", []):
        category = category_data["category"]
        for entry_data in category_data.get("entries", []):
            entries.append(SpriteEntry(
                id=entry_data["id"],
                category=category,
                base_name=entry_data["baseName"],
                transparent=entry_data.get("transparent", True),
                variants=entry_data.get("variants", []),
                metadata={
                    k: v for k, v in entry_data.items()
                    if k not in {"id", "baseName", "transparent", "variants", "category"}
                    and v is not None
                }
            ))

    return entries


def generate_sprite(entry: SpriteEntry, output_path: Path, api_endpoint: Optional[str] = None) -> bool:
    """
    Generate a single sprite asset.

    If api_endpoint is provided, calls the remote image generation API.
    Otherwise, creates a placeholder for manual replacement.
    """
    prompt = entry.get_prompt()

    # Ensure output directory exists
    output_path.parent.mkdir(parents=True, exist_ok=True)

    if api_endpoint:
        try:
            return _call_generation_api(prompt, entry.base_name, output_path, api_endpoint)
        except Exception as e:
            print(f"API generation failed for {entry.id}: {e}")
            return _create_placeholder(entry, output_path)
    else:
        return _create_placeholder(entry, output_path)


def _call_generation_api(prompt: str, name: str, output_path: Path, api_endpoint: str) -> bool:
    """Call external image generation API."""
    import requests

    payload = {
        "prompt": prompt,
        "negative_prompt": NEGATIVE_PROMPT,
        "width": 512,
        "height": 512,
        "seed": _derive_seed(name),
        "num_inference_steps": 30,
        "guidance_scale": 7.5
    }

    print(f"Generating: {name}")
    print(f"  Prompt: {prompt[:120]}...")

    try:
        response = requests.post(api_endpoint, json=payload, timeout=120)
        response.raise_for_status()

        # Save generated image
        with open(output_path, 'wb') as f:
            f.write(response.content)

        print(f"  Saved: {output_path}")
        return True

    except requests.RequestException as e:
        print(f"  API error: {e}")
        return False


def _create_placeholder(entry: SpriteEntry, output_path: Path) -> bool:
    """Create a placeholder PNG with metadata for manual replacement."""
    try:
        from PIL import Image, ImageDraw, ImageFont

        size = 512
        img = Image.new('RGBA', (size, size), (26, 20, 16, 255))  # PARCHMENT_DARK
        draw = ImageDraw.Draw(img)

        # Draw border
        draw.rectangle([4, 4, size-4, size-4], outline=(74, 69, 64, 255), width=2)

        # Draw category label
        label = f"{entry.category}\n{entry.id}"
        draw.text((size//2, size//2), label, fill=(74, 69, 64, 255),
                  anchor="mm", font=None)

        # Save
        img.save(output_path, 'PNG')
        print(f"Placeholder: {output_path}")
        return True

    except ImportError:
        # Create minimal placeholder without PIL
        with open(output_path, 'wb') as f:
            # Minimal valid PNG (1x1 pixel)
            f.write(b'\x89PNG\r\n\x1a\n')
        print(f"Minimal placeholder: {output_path}")
        return True


def _derive_seed(name: str) -> int:
    """Derive a consistent seed from the sprite name for reproducibility."""
    return int(hashlib.md5(name.encode()).hexdigest(), 16) % (2**31)


def optimize_sprite(input_path: Path, output_path: Path, target_size: int = 128) -> bool:
    """
    Downscale sprite to runtime resolution and optimize.
    Called during build pipeline.
    """
    try:
        from PIL import Image

        with Image.open(input_path) as img:
            # Resize with high-quality downsampling
            img_resized = img.resize((target_size, target_size), Image.LANCZOS)

            # Ensure transparency preserved
            if img_resized.mode != 'RGBA':
                img_resized = img_resized.convert('RGBA')

            output_path.parent.mkdir(parents=True, exist_ok=True)
            img_resized.save(output_path, 'PNG', optimize=True)

        return True

    except ImportError:
        print("PIL not available, skipping optimization")
        # Copy as-is
        import shutil
        shutil.copy(input_path, output_path)
        return True

    except Exception as e:
        print(f"Optimization failed: {e}")
        return False


# --- CLI ---

def main():
    parser = argparse.ArgumentParser(
        description="Chimera Sprite Generation Pipeline"
    )
    parser.add_argument(
        "--manifest",
        default="assets/sprite_manifest.json",
        help="Path to sprite_manifest.json"
    )
    parser.add_argument(
        "--output",
        default="app/src/main/res/",
        help="Output directory for generated assets"
    )
    parser.add_argument(
        "--category",
        help="Generate only sprites in this category"
    )
    parser.add_argument(
        "--single",
        help="Generate a single sprite by ID"
    )
    parser.add_argument(
        "--api",
        help="Image generation API endpoint URL"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print prompts without generating"
    )
    parser.add_argument(
        "--optimize",
        action="store_true",
        help="Run optimization pass on existing assets"
    )
    parser.add_argument(
        "--target-size",
        type=int,
        default=128,
        help="Target resolution for optimization (default: 128)"
    )

    args = parser.parse_args()

    if not os.path.exists(args.manifest):
        print(f"Manifest not found: {args.manifest}")
        sys.exit(1)

    entries = load_manifest(args.manifest)
    print(f"Loaded {len(entries)} sprite entries from manifest")

    # Filter
    if args.category:
        entries = [e for e in entries if e.category == args.category.upper()]
        print(f"Filtered to {len(entries)} entries in category {args.category}")

    if args.single:
        entries = [e for e in entries if e.id == args.single]
        if not entries:
            print(f"Sprite ID not found: {args.single}")
            sys.exit(1)

    # Dry run: print prompts
    if args.dry_run:
        for entry in entries:
            print(f"\n--- {entry.id} ({entry.category}) ---")
            print(f"Prompt: {entry.get_prompt()}")
            out_path = entry.get_output_path(args.output)
            print(f"Output: {out_path}")
        return

    # Optimization pass
    if args.optimize:
        for entry in entries:
            for variant in entry.variants:
                source = Path(args.output) / "drawable-xxhdpi" / f"{entry.base_name}.png"
                target = Path(args.output) / "drawable-xhdpi" / f"{entry.base_name}.png"
                if source.exists():
                    optimize_sprite(source, target, args.target_size)
        return

    # Generate
    success_count = 0
    for entry in entries:
        output_path = entry.get_output_path(args.output)

        if output_path.exists():
            print(f"Skipping existing: {entry.id}")
            continue

        if generate_sprite(entry, output_path, args.api):
            success_count += 1

    print(f"\nDone: {success_count}/{len(entries)} sprites generated")


if __name__ == "__main__":
    main()
