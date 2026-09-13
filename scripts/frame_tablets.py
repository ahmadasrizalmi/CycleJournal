# -*- coding: utf-8 -*-
"""Compose tablet store screenshots.

goldie ships a single Android device (the Play phone) and its capture step replays stale iOS
simulator frames on this machine, so the tablet tiles are composed here with the same design
language goldie uses: the brand gradient, the headline and subhead from goldie.config.ts, and the
emulator screenshot below them. Play's tablet sizes are 1200x1920 (7 inch) and 1600x2560 (10 inch).

Run: python scripts/frame_tablets.py
"""
import io, os, re
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = "D:/9 KANDA/APPS Android/CycleJournal"
RAW = os.path.join(ROOT, "out/raw")
OUT = os.path.join(ROOT, "out/screenshots")
FONTS = os.path.expandvars(r"%APPDATA%\npm\node_modules\goldie\assets\fonts")

DEVICES = {"tablet-7": (1200, 1920), "tablet-10": (1600, 2560)}
GRAD = ((0xFF, 0x8A, 0x71), (0xFF, 0x5E, 0x7D))
HEAD = (255, 255, 255)
SUB = (0xFF, 0xF1, 0xF2)
SHADOW = (140, 30, 60)

BLOCK = re.compile(r'id: "(?P<id>[a-z\-]+)"(?P<body>.*?)(?=\n    \{|\n  \],)', re.S)
HEAD_RE = re.compile(r'headline:\s*\{\s*"en-US":\s*"(?P<en>[^"]*)"\s*,\s*"id-ID":\s*"(?P<idn>[^"]*)"', re.S)
SUB_RE = re.compile(r'subhead:\s*\{\s*"en-US":\s*"(?P<en>[^"]*)"\s*,\s*"id-ID":\s*"(?P<idn>[^"]*)"', re.S)


def scene_copy():
    """Read the headline/subhead pairs straight out of goldie.config.ts, the single source."""
    src = io.open(os.path.join(ROOT, "goldie.config.ts"), encoding="utf-8").read()
    out = {}
    for m in BLOCK.finditer(src):
        head, sub = HEAD_RE.search(m.group("body")), SUB_RE.search(m.group("body"))
        if not (head and sub):
            continue
        out[m.group("id")] = {
            "id-ID": (head.group("idn"), sub.group("idn")),
            "en-US": (head.group("en"), sub.group("en")),
        }
    assert len(out) >= 8, "parsed only %d scenes from goldie.config.ts" % len(out)
    return out


def grad_bg(size):
    w, h = size
    img = Image.new("RGB", size)
    d = ImageDraw.Draw(img)
    for y in range(h):
        # 145deg: blend horizontally as well, cheap diagonal feel
        for_t = y / max(h - 1, 1)
        r = int(GRAD[0][0] + (GRAD[1][0] - GRAD[0][0]) * for_t)
        g = int(GRAD[0][1] + (GRAD[1][1] - GRAD[0][1]) * for_t)
        b = int(GRAD[0][2] + (GRAD[1][2] - GRAD[0][2]) * for_t)
        d.line([(0, y), (w, y)], fill=(r, g, b))
    return img


def wrap(draw, text, font, max_w):
    words, lines, cur = text.split(), [], ""
    for word in words:
        probe = (cur + " " + word).strip()
        if draw.textlength(probe, font=font) <= max_w or not cur:
            cur = probe
        else:
            lines.append(cur)
            cur = word
    if cur:
        lines.append(cur)
    return lines


def compose(device, locale, scene, shot_path, headline, subhead, out_path):
    W, H = DEVICES[device]
    tile = grad_bg((W, H))
    d = ImageDraw.Draw(tile)
    margin = int(W * 0.075)
    head_font = ImageFont.truetype(os.path.join(FONTS, "Montserrat-700.ttf"), int(W * 0.082))
    sub_font = ImageFont.truetype(os.path.join(FONTS, "Montserrat-400.ttf"), int(W * 0.038))

    y = int(H * 0.055)
    for line in wrap(d, headline, head_font, W - 2 * margin):
        d.text((margin, y), line, font=head_font, fill=HEAD)
        y += int(head_font.size * 1.16)
    y += int(W * 0.012)
    for line in wrap(d, subhead, sub_font, int((W - 2 * margin) * 0.92)):
        d.text((margin, y), line, font=sub_font, fill=SUB)
        y += int(sub_font.size * 1.42)

    shot = Image.open(shot_path).convert("RGB")
    target_w = int(W * 0.68)
    scale = target_w / shot.width
    shot = shot.resize((target_w, int(shot.height * scale)), Image.LANCZOS)
    radius = int(W * 0.028)
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, shot.width - 1, shot.height - 1], radius=radius, fill=255)

    # keep the whole device inside the tile, under the copy block
    room = H - y - int(H * 0.045)
    if shot.height > room:
        scale = room / shot.height
        shot = shot.resize((int(shot.width * scale), room), Image.LANCZOS)
        mask = Image.new("L", shot.size, 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, shot.width - 1, shot.height - 1], radius=radius, fill=255)

    x = (W - shot.width) // 2
    shadow = Image.new("RGBA", tile.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [x + 10, y + 26, x + shot.width - 10, y + shot.height + 26],
        radius=radius, fill=SHADOW + (115,),
    )
    tile = Image.alpha_composite(tile.convert("RGBA"), shadow.filter(ImageFilter.GaussianBlur(int(W * 0.02)))).convert("RGB")
    tile.paste(shot, (x, y), mask)

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    tile.save(out_path, "PNG", optimize=True)
    return os.path.getsize(out_path)


def main():
    copy = scene_copy()
    for device, (W, H) in DEVICES.items():
        for locale in ("id-ID", "en-US"):
            src_dir = os.path.join(RAW, device, locale)
            if not os.path.isdir(src_dir):
                print("no captures for", device, locale)
                continue
            dst_dir = os.path.join(OUT, device, locale)
            shots = sorted(f for f in os.listdir(src_dir) if f.endswith(".png"))
            for i, f in enumerate(shots, start=1):
                scene = f[:-4]
                if scene not in copy:
                    print("  no copy for", scene); continue
                headline, subhead = copy[scene][locale]
                out = os.path.join(dst_dir, "%02d-%s.png" % (i, scene))
                size = compose(device, locale, scene, os.path.join(src_dir, f), headline, subhead, out)
                print("  %-12s %-6s %-20s %dx%d %8d" % (device, locale, scene, W, H, size))


if __name__ == "__main__":
    main()
