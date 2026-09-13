"""Adds the two new Home hero slides to the pen.dev document.

The Home hero became a three-slide carousel (period -> fertile window -> ovulation peak) but the
document only described the first slide. This script appends the missing variants, keeping the
document the single source of truth: it copies the existing hero card, swaps the copy, adds the
promil badge to the ovulation variant and brings the pagination dots up to three everywhere the
Home frames use them.

Run:  python scripts/pen_add_hero_slides.py [--dry-run]
"""

import argparse
import json
import shutil
import sys
from datetime import datetime
from pathlib import Path

DOC = Path.home() / ".pencil/documents/1d35c22e-7ab9-4e4e-bdec-d54e8db37d9a/pencil-new.pen"

ID_HOME = "hnYda"
EN_HOME = "f5aMuZ"
ID_HERO = "JRr0B"
EN_HERO = "k1YZBZ"

# id -> (owning home frame, hero copy)
VARIANTS = {
    "Masa Subur": {
        "eyebrow": "MASA SUBUR",
        "digits": ("0", "6"),
        "big": "6 hari",
        "sub": "jendela 22 Sep \u2013 28 Sep",
        "cta": "Lihat kalender",
        "icon": "sprout",
        "badge": None,
    },
    "Puncak Ovulasi": {
        "eyebrow": "PUNCAK OVULASI",
        "digits": ("0", "2"),
        "big": "2 hari lagi",
        "sub": "perkiraan 27 Sep \u00b7 peluang tinggi",
        "cta": "Catat suhu basal",
        "icon": "egg",
        "badge": "MODE PROMIL",
    },
}

VARIANTS_EN = {
    "Fertile Window": {
        "eyebrow": "FERTILE WINDOW",
        "digits": ("0", "6"),
        "big": "6 days",
        "sub": "window 22 Sep \u2013 28 Sep",
        "cta": "Open calendar",
        "icon": "sprout",
        "badge": None,
    },
    "Ovulation Peak": {
        "eyebrow": "OVULATION PEAK",
        "digits": ("0", "2"),
        "big": "2 days to go",
        "sub": "expected 27 Sep \u00b7 high chance",
        "cta": "Log basal temperature",
        "icon": "egg",
        "badge": "TTC MODE",
    },
}

COUNTER = [0]


def fresh_id() -> str:
    COUNTER[0] += 1
    return f"v4hero{COUNTER[0]:03d}"


def reid(node, taken):
    """Assigns fresh unique ids to a copied subtree."""
    if isinstance(node, dict):
        new_id = fresh_id()
        while new_id in taken:
            new_id = fresh_id()
        taken.add(new_id)
        node["id"] = new_id
        for child in node.get("children", []) or []:
            reid(child, taken)
    return node


def find(node, target_id):
    if isinstance(node, dict):
        if node.get("id") == target_id:
            return node
        for child in node.get("children", []) or []:
            found = find(child, target_id)
            if found:
                return found
    elif isinstance(node, list):
        for child in node:
            found = find(child, target_id)
            if found:
                return found
    return None


def all_ids(node, out):
    if isinstance(node, dict):
        if node.get("id"):
            out.add(node["id"])
        for child in node.get("children", []) or []:
            all_ids(child, out)
    elif isinstance(node, list):
        for child in node:
            all_ids(child, out)
    return out


def set_text(node, name, value):
    """Replaces the content of the first text/icon node with the given name."""
    if isinstance(node, dict):
        if node.get("name") == name and node.get("type") in ("text", "icon"):
            if node.get("type") == "text":
                node["content"] = value
            else:
                node["icon"] = value
            return True
        for child in node.get("children", []) or []:
            if set_text(child, name, value):
                return True
    return False


def texts_of(node, name):
    return [n for n in walk(node) if n.get("name") == name and n.get("type") == "text"]


def walk(node):
    if isinstance(node, dict):
        yield node
        for child in node.get("children", []) or []:
            yield from walk(child)
    elif isinstance(node, list):
        for child in node:
            yield from walk(child)


def set_nth_text(node, name, index, value):
    matches = texts_of(node, name)
    if index >= len(matches):
        raise SystemExit(f"expected at least {index + 1} text nodes named {name!r}")
    matches[index]["content"] = value


def add_third_dot(dots_frame, taken):
    """The hero carousel has three slides now, so the dot row needs a third dot."""
    children = dots_frame.get("children") or []
    if len(children) >= 3:
        return False
    base = json.loads(json.dumps(children[-1]))
    base["name"] = f"D{len(children) + 1}"
    base["id"] = fresh_id()
    while base["id"] in taken:
        base["id"] = fresh_id()
    taken.add(base["id"])
    children.append(base)
    dots_frame["children"] = children
    return True


def build_variant_frame(hero_source, dots_source, title, spec, x, y, taken):
    hero = reid(json.loads(json.dumps(hero_source)), taken)
    hero["x"] = 0
    hero["y"] = 0
    hero["name"] = f"Hero {title}"

    set_text(hero, "Eyebrow", spec["eyebrow"])
    set_nth_text(hero, "Digit", 0, spec["digits"][0])
    set_nth_text(hero, "Digit", 1, spec["digits"][1])
    set_text(hero, "Big", spec["big"])
    set_text(hero, "Sub", spec["sub"])
    set_text(hero, "T", spec["cta"])
    set_text(hero, "i", spec["icon"])

    if spec["badge"]:
        top = next(n for n in walk(hero) if n.get("name") == "Top")
        badge = {
            "type": "frame",
            "id": fresh_id(),
            "name": "Badge Promil",
            "height": 24,
            "fill": "$brand-tint",
            "cornerRadius": 999,
            "padding": [0, 10],
            "justifyContent": "center",
            "alignItems": "center",
            "children": [
                {
                    "type": "text",
                    "id": fresh_id(),
                    "name": "B",
                    "fill": "$brand-end",
                    "content": spec["badge"],
                    "fontFamily": "$font-ui",
                    "fontSize": 11,
                    "fontWeight": "700",
                    "letterSpacing": 0.6,
                }
            ],
        }
        children = top["children"]
        icon_index = next(i for i, c in enumerate(children) if c.get("name") == "Icon")
        children.insert(icon_index, badge)
        taken.update({badge["id"], badge["children"][0]["id"]})

    dots = reid(json.loads(json.dumps(dots_source)), taken)
    dots["name"] = "Dots"
    while len(dots.get("children") or []) < 3:
        add_third_dot(dots, taken)

    return {
        "type": "frame",
        "id": fresh_id(),
        "name": title,
        "clip": True,
        "width": 390,
        "height": 300,
        "x": x,
        "y": y,
        "fill": "$canvas-soft",
        "layout": "vertical",
        "gap": 10,
        "padding": 20,
        "children": [hero, dots],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--doc", default=str(DOC))
    args = parser.parse_args()

    doc_path = Path(args.doc)
    document = json.loads(doc_path.read_text(encoding="utf-8"))
    taken = all_ids(document, set())

    id_home = find(document, ID_HOME)
    en_home = find(document, EN_HOME)
    id_hero = find(document, ID_HERO)
    en_hero = find(document, EN_HERO)
    if not all([id_home, en_home, id_hero, en_hero]):
        raise SystemExit("the expected Home frames are missing from the document")

    # 1. three pagination dots on both existing Home frames
    id_dots = next(n for n in walk(id_home) if n.get("name") == "Dots")
    en_dots = next(n for n in walk(en_home) if n.get("name") == "Dots")
    added_dots = add_third_dot(id_dots, taken), add_third_dot(en_dots, taken)

    # 2. the two missing slides, next to the Home frames they belong to
    new_frames = []
    for offset, (title, spec) in enumerate(VARIANTS.items(), start=1):
        new_frames.append(
            build_variant_frame(
                id_hero, id_dots, f"Beranda \u00b7 ID v4 \u00b7 {title}", spec,
                x=id_home["x"] + 420 * offset, y=id_home["y"], taken=taken,
            )
        )
    for offset, (title, spec) in enumerate(VARIANTS_EN.items(), start=1):
        new_frames.append(
            build_variant_frame(
                en_hero, en_dots, f"Home \u00b7 EN v4 \u00b7 {title}", spec,
                x=en_home["x"] + 420 * offset, y=en_home["y"], taken=taken,
            )
        )

    document["children"].extend(new_frames)

    if args.dry_run:
        print(f"dry run: would add {len(new_frames)} frames, dots updated: {added_dots}")
        return

    backup = doc_path.with_suffix(f".pen.bak-{datetime.now():%Y%m%d-%H%M%S}")
    shutil.copy2(doc_path, backup)
    doc_path.write_text(json.dumps(document, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")

    # re-read to prove the document is still valid json and the ids stayed unique
    check = json.loads(doc_path.read_text(encoding="utf-8"))
    ids = all_ids(check, set())
    ids_count = sum(1 for _ in walk(check) if isinstance(_, dict) and _.get("id"))
    print(f"backup: {backup.name}")
    print(f"frames now: {len(check['children'])}, ids: {len(ids)} unique of {ids_count}")
    for frame in new_frames:
        print(f"  added: {frame['name']} ({frame['id']})")


if __name__ == "__main__":
    sys.exit(main())
