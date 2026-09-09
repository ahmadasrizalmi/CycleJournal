import os
import json
import math
from PIL import Image, ImageDraw, ImageFont

def render_screens():
    raw_dir = os.path.abspath("out/raw/pixel-10-pro")
    os.makedirs(raw_dir, exist_ok=True)
    width, height = 1080, 1920

    # Typography Fonts (Segoe UI on Windows)
    font_bold_hero = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 44)
    font_bold_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 34)
    font_section = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 26)
    font_sub_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 22)
    font_body = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 20)
    font_caption = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 18)
    font_small = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 15)
    font_tiny = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 13)
    font_tab = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 16)

    # Logo Master: Transparent Background PNG
    logo_path = "design/logo_clean_master.png"
    if not os.path.exists(logo_path):
        logo_path = "design/logo cyclejournal no background.png"
    logo_master = Image.open(logo_path).convert("RGBA")

    # Colors
    c_white = (255, 255, 255, 255)
    c_light_bg = (251, 251, 252, 255)
    c_slate_900 = (15, 23, 42, 255)
    c_slate_800 = (30, 41, 59, 255)
    c_slate_700 = (51, 65, 85, 255)
    c_slate_600 = (71, 85, 105, 255)
    c_slate_500 = (100, 116, 139, 255)
    c_slate_400 = (148, 163, 184, 255)
    c_slate_200 = (226, 232, 240, 255)
    c_slate_100 = (241, 245, 249, 255)
    c_slate_50 = (248, 250, 252, 255)

    c_coral_400 = (255, 138, 113, 255)
    c_coral_500 = (255, 111, 97, 255)
    c_coral_600 = (255, 94, 125, 255)
    c_border = (226, 232, 240, 255)
    c_border_subtle = (241, 245, 249, 255)

    c_rose_alert = (244, 63, 94, 255)
    c_rose_dark = (159, 18, 57, 255)
    c_teal = (13, 148, 136, 255)
    c_cyan = (6, 182, 212, 255)
    c_amber = (245, 158, 11, 255)

    margin = 68

    def draw_alpha(base_img, draw_func):
        overlay = Image.new("RGBA", base_img.size, (0, 0, 0, 0))
        d_overlay = ImageDraw.Draw(overlay)
        draw_func(d_overlay)
        base_img.alpha_composite(overlay)

    def draw_status_bar(draw):
        draw.text((margin, 28), "09:41", fill=c_slate_900, font=font_caption)
        draw.text((width - margin - 80, 28), "100%", fill=c_slate_900, font=font_caption)
        # Battery icon
        bx = width - margin - 32
        by = 31
        draw.rounded_rectangle([bx, by, bx + 24, by + 13], radius=3, outline=c_slate_900, width=1)
        draw.rectangle([bx + 2, by + 2, bx + 19, by + 11], fill=c_slate_900)
        draw.rectangle([bx + 24, by + 4, bx + 26, by + 9], fill=c_slate_900)

    def draw_gradient_rect(draw, x1, y1, x2, y2, r1=255, g1=138, b1=113, r2=255, g2=94, b2=125, corner_radius=26):
        h = max(1, y2 - y1)
        w = max(1, x2 - x1)
        mask = Image.new("L", (w, h), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.rounded_rectangle([0, 0, w, h], radius=corner_radius, fill=255)
        
        grad = Image.new("RGBA", (w, h))
        grad_draw = ImageDraw.Draw(grad)
        for y in range(h):
            ratio = y / h
            r = int(r1 + (r2 - r1) * ratio)
            g = int(g1 + (g2 - g1) * ratio)
            b = int(b1 + (b2 - b1) * ratio)
            grad_draw.line([(0, y), (w, y)], fill=(r, g, b, 255))
        
        draw._image.paste(grad, (x1, y1), mask)

    # Vector Drawing Helpers (No raw ASCII)
    def draw_eye_icon(draw, cx, cy, color):
        # Outer eye shape
        draw.arc([cx - 10, cy - 6, cx + 10, cy + 6], start=0, end=360, fill=color, width=2)
        # Pupil
        draw.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], fill=color)

    def draw_moon_icon(draw, cx, cy, color, bg_color):
        draw.ellipse([cx - 7, cy - 7, cx + 7, cy + 7], fill=color)
        draw.ellipse([cx - 4, cy - 9, cx + 9, cy + 5], fill=bg_color)

    def draw_tune_icon(draw, cx, cy, color):
        # 3 sliders
        draw.line([(cx - 8, cy - 5), (cx + 8, cy - 5)], fill=color, width=2)
        draw.ellipse([cx - 4, cy - 7, cx - 1, cy - 3], fill=color)
        draw.line([(cx - 8, cy), (cx + 8, cy)], fill=color, width=2)
        draw.ellipse([cx + 1, cy - 2, cx + 4, cy + 2], fill=color)
        draw.line([(cx - 8, cy + 5), (cx + 8, cy + 5)], fill=color, width=2)
        draw.ellipse([cx - 3, cy + 3, cx, cy + 7], fill=color)

    def draw_arrow_back(draw, cx, cy, color):
        draw.line([(cx - 7, cy), (cx + 7, cy)], fill=color, width=2)
        draw.line([(cx - 7, cy), (cx - 1, cy - 6)], fill=color, width=2)
        draw.line([(cx - 7, cy), (cx - 1, cy + 6)], fill=color, width=2)

    def draw_arrow_forward(draw, cx, cy, color):
        draw.line([(cx - 7, cy), (cx + 7, cy)], fill=color, width=2)
        draw.line([(cx + 7, cy), (cx + 1, cy - 6)], fill=color, width=2)
        draw.line([(cx + 7, cy), (cx + 1, cy + 6)], fill=color, width=2)

    def draw_checkmark(draw, cx, cy, color, size=6):
        draw.line([(cx - size, cy), (cx - size // 3, cy + size)], fill=color, width=2)
        draw.line([(cx - size // 3, cy + size), (cx + size, cy - size)], fill=color, width=2)

    def draw_checkmark_circle(draw, cx, cy, bg_color, check_color, r=10):
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=bg_color)
        draw_checkmark(draw, cx, cy, check_color, size=int(r * 0.55))

    def draw_alert_triangle(draw, cx, cy, fill_color, icon_color, size=11):
        pts = [(cx, cy - size), (cx - size, cy + size), (cx + size, cy + size)]
        draw.polygon(pts, fill=fill_color)
        # Exclamation point
        draw.line([(cx, cy - size // 3), (cx, cy + size // 4)], fill=icon_color, width=2)
        draw.ellipse([cx - 1, cy + size // 2, cx + 1, cy + size // 2 + 2], fill=icon_color)

    def draw_info_circle(draw, cx, cy, fill_color, icon_color, r=10):
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=fill_color)
        draw.ellipse([cx - 1, cy - r // 2, cx + 1, cy - r // 2 + 2], fill=icon_color)
        draw.line([(cx, cy - r // 6), (cx, cy + r // 2)], fill=icon_color, width=2)

    def draw_star(draw, cx, cy, color, r=9):
        pts = []
        for i in range(10):
            rad = r if i % 2 == 0 else r * 0.45
            angle = i * math.pi / 5 - math.pi / 2
            pts.append((cx + rad * math.cos(angle), cy + rad * math.sin(angle)))
        draw.polygon(pts, fill=color)

    def draw_sparkle(draw, cx, cy, color, r=7):
        draw.line([(cx - r, cy), (cx + r, cy)], fill=color, width=2)
        draw.line([(cx, cy - r), (cx, cy + r)], fill=color, width=2)
        draw.line([(cx - r * 0.5, cy - r * 0.5), (cx + r * 0.5, cy + r * 0.5)], fill=color, width=1)
        draw.line([(cx - r * 0.5, cy + r * 0.5), (cx + r * 0.5, cy - r * 0.5)], fill=color, width=1)

    def draw_lock(draw, cx, cy, color, s=8):
        # Shackle
        draw.arc([cx - s * 0.7, cy - s * 1.3, cx + s * 0.7, cy], start=180, end=0, fill=color, width=2)
        # Body
        draw.rounded_rectangle([cx - s, cy - s * 0.3, cx + s, cy + s * 0.9], radius=3, fill=color)

    def draw_pdf_doc_icon(draw, cx, cy, color):
        w, h = 8, 11
        draw.rounded_rectangle([cx - w, cy - h, cx + w, cy + h], radius=2, outline=color, width=2)
        draw.line([(cx - w + 3, cy - 4), (cx + w - 3, cy - 4)], fill=color, width=1)
        draw.line([(cx - w + 3, cy), (cx + w - 3, cy)], fill=color, width=1)
        draw.line([(cx - w + 3, cy + 4), (cx + w - 5, cy + 4)], fill=color, width=1)

    def draw_sheets_grid_icon(draw, cx, cy, color):
        s = 9
        draw.rounded_rectangle([cx - s, cy - s, cx + s, cy + s], radius=2, outline=color, width=2)
        draw.line([(cx - s, cy), (cx + s, cy)], fill=color, width=1)
        draw.line([(cx, cy - s), (cx, cy + s)], fill=color, width=1)

    def draw_cloud_icon(draw, cx, cy, color, is_upload=True):
        # Base cloud
        draw.ellipse([cx - 10, cy - 4, cx - 1, cy + 5], fill=color)
        draw.ellipse([cx - 4, cy - 9, cx + 6, cy + 1], fill=color)
        draw.ellipse([cx + 1, cy - 5, cx + 10, cy + 5], fill=color)
        draw.rectangle([cx - 8, cy + 1, cx + 8, cy + 6], fill=color)
        # Arrow inside
        arr_col = c_white
        if is_upload:
            draw.line([(cx, cy + 4), (cx, cy - 3)], fill=arr_col, width=2)
            draw.line([(cx, cy - 3), (cx - 3, cy)], fill=arr_col, width=2)
            draw.line([(cx, cy - 3), (cx + 3, cy)], fill=arr_col, width=2)
        else:
            draw.line([(cx, cy - 3), (cx, cy + 4)], fill=arr_col, width=2)
            draw.line([(cx, cy + 4), (cx - 3, cy + 1)], fill=arr_col, width=2)
            draw.line([(cx, cy + 4), (cx + 3, cy + 1)], fill=arr_col, width=2)

    def draw_switch(draw, x, y, is_on, on_color=c_coral_500, off_color=c_slate_200):
        w, h = 50, 28
        track_color = on_color if is_on else off_color
        draw.rounded_rectangle([x, y, x + w, y + h], radius=h // 2, fill=track_color)
        thumb_x = x + w - h + 2 if is_on else x + 2
        draw.ellipse([thumb_x, y + 2, thumb_x + h - 4, y + h - 2], fill=c_white)

    def draw_bottom_nav(draw, active_tab=0):
        bar_w = width - margin * 2
        bar_h = 86
        bar_y = height - 120
        bar_x = margin

        # Background rounded surface
        draw.rounded_rectangle(
            [bar_x, bar_y, bar_x + bar_w, bar_y + bar_h],
            radius=43,
            fill=c_white,
            outline=c_border,
            width=1
        )

        tab_cx = [
            bar_x + int(bar_w * 0.12),
            bar_x + int(bar_w * 0.32),
            bar_x + int(bar_w * 0.50), # Central FAB
            bar_x + int(bar_w * 0.68),
            bar_x + int(bar_w * 0.88)
        ]

        # Tab 1: Beranda
        t1_col = c_coral_600 if active_tab == 0 else c_slate_400
        hx, hy = tab_cx[0], bar_y + 26
        draw.polygon([(hx, hy - 9), (hx - 10, hy), (hx + 10, hy)], fill=t1_col)
        draw.rectangle([hx - 7, hy, hx + 7, hy + 9], fill=t1_col)
        draw.text((hx - 24, bar_y + 48), "Beranda", fill=t1_col, font=font_small)

        # Tab 2: Kalender
        t2_col = c_coral_600 if active_tab == 1 else c_slate_400
        cx, cy = tab_cx[1], bar_y + 26
        draw.rounded_rectangle([cx - 9, cy - 9, cx + 9, cy + 9], radius=3, outline=t2_col, width=2)
        draw.line([(cx - 9, cy - 3), (cx + 9, cy - 3)], fill=t2_col, width=2)
        draw.text((cx - 24, bar_y + 48), "Kalender", fill=t2_col, font=font_small)

        # Tab 3: Center Elevated Floating Action Button (offset up -16px)
        fab_size = 64
        fcx, fcy = tab_cx[2], bar_y + bar_h // 2 - 12
        draw_gradient_rect(draw, fcx - fab_size // 2, fcy - fab_size // 2, fcx + fab_size // 2, fcy + fab_size // 2, corner_radius=fab_size // 2)
        draw.line([(fcx - 11, fcy), (fcx + 11, fcy)], fill=c_white, width=3)
        draw.line([(fcx, fcy - 11), (fcx, fcy + 11)], fill=c_white, width=3)

        # Tab 4: Laporan
        t4_col = c_coral_600 if active_tab == 3 else c_slate_400
        rx, ry = tab_cx[3], bar_y + 26
        draw.rounded_rectangle([rx - 7, ry - 9, rx + 7, ry + 9], radius=2, outline=t4_col, width=2)
        draw.line([(rx - 4, ry - 3), (rx + 4, ry - 3)], fill=t4_col, width=2)
        draw.line([(rx - 4, ry + 2), (rx + 4, ry + 2)], fill=t4_col, width=2)
        draw.text((rx - 22, bar_y + 48), "Laporan", fill=t4_col, font=font_small)

        # Tab 5: Pengaturan
        t5_col = c_coral_600 if active_tab == 4 else c_slate_400
        sx, sy = tab_cx[4], bar_y + 26
        draw_tune_icon(draw, sx, sy, t5_col)
        draw.text((sx - 30, bar_y + 48), "Pengaturan", fill=t5_col, font=font_small)

    # =========================================================================
    # 1. SCREEN 1: DASHBOARD (Clean, strictly matching design file & prototype)
    # =========================================================================
    im1 = Image.new("RGBA", (width, height), c_light_bg)
    d1 = ImageDraw.Draw(im1)
    draw_status_bar(d1)

    # Top App Header
    h_top = 70
    draw_gradient_rect(d1, margin, h_top, margin + 44, h_top + 44, corner_radius=14)
    # Pearl core in icon
    d1.ellipse([margin + 14, h_top + 14, margin + 30, h_top + 30], fill=c_white)
    d1.text((margin + 56, h_top + 2), "MODE PRIVAT OFFLINE", fill=c_coral_600, font=font_tiny)
    d1.text((margin + 56, h_top + 18), "CycleJournal", fill=c_slate_900, font=font_section)

    # 3 Square Buttons on Header Right (Eye, Moon, Tune)
    btn_size = 38
    b3_x = width - margin - btn_size
    b2_x = b3_x - btn_size - 10
    b1_x = b2_x - btn_size - 10

    for bx in [b1_x, b2_x, b3_x]:
        d1.rounded_rectangle([bx, h_top + 3, bx + btn_size, h_top + 3 + btn_size], radius=12, fill=c_white, outline=c_border, width=1)
    draw_eye_icon(d1, b1_x + btn_size // 2, h_top + 3 + btn_size // 2, c_slate_600)
    draw_moon_icon(d1, b2_x + btn_size // 2, h_top + 3 + btn_size // 2, c_slate_600, c_white)
    draw_tune_icon(d1, b3_x + btn_size // 2, h_top + 3 + btn_size // 2, c_slate_600)

    # 1. HERO CARD: DYNAMIC HORMONAL PHASE (At the very top!)
    hero_y1 = 138
    hero_y2 = 418
    draw_gradient_rect(d1, margin, hero_y1, width - margin, hero_y2, corner_radius=26)


    # Phase Capsule
    d1.rounded_rectangle([margin + 24, hero_y1 + 22, margin + 148, hero_y1 + 54], radius=16, fill=(255, 255, 255, 55))
    d1.text((margin + 36, hero_y1 + 29), "JENDELA SUBUR", fill=c_white, font=font_small)

    # Title & Subtitle
    d1.text((margin + 24, hero_y1 + 68), "Fase Folikuler", fill=c_white, font=font_bold_hero)
    draw_sparkle(d1, margin + 30, hero_y1 + 134, (255, 209, 216, 255), r=6)
    d1.text((margin + 44, hero_y1 + 124), "Ovulasi dalam 2 hari ke depan", fill=(255, 228, 230, 255), font=font_caption)

    # 2 Metric Pills Row (Clearly separated, no overlapping)
    p_w = 175
    p_h = 70
    p1_x = margin + 24
    p2_x = p1_x + p_w + 14
    p_y = hero_y1 + 172

    d1.rounded_rectangle([p1_x, p_y, p1_x + p_w, p_y + p_h], radius=14, fill=(0, 0, 0, 42))
    d1.text((p1_x + 14, p_y + 10), "RATA-RATA SIKLUS", fill=(255, 209, 216, 255), font=font_tiny)
    d1.text((p1_x + 14, p_y + 32), "28 Hari (±1.5)", fill=c_white, font=font_sub_bold)

    d1.rounded_rectangle([p2_x, p_y, p2_x + p_w, p_y + p_h], radius=14, fill=(0, 0, 0, 42))
    d1.text((p2_x + 14, p_y + 10), "PELUANG KONSEPSI", fill=(255, 209, 216, 255), font=font_tiny)
    d1.text((p2_x + 14, p_y + 32), "Tinggi (85%)", fill=(254, 240, 138, 255), font=font_sub_bold)

    # Circular Progress Ring: Day 14 of 28 (right column)
    rcx = width - margin - 100
    rcy = hero_y1 + 140
    rr = 72
    # 25% white track
    d1.ellipse([rcx - rr, rcy - rr, rcx + rr, rcy + rr], outline=(255, 255, 255, 60), width=10)
    # 50% arc from -90 to 90
    d1.arc([rcx - rr, rcy - rr, rcx + rr, rcy + rr], start=-90, end=90, fill=c_white, width=10)
    d1.text((rcx - 16, rcy - 44), "Hari", fill=(255, 228, 230, 255), font=font_small)
    d1.text((rcx - 25, rcy - 20), "14", fill=c_white, font=font_bold_hero)
    d1.text((rcx - 24, rcy + 30), "dari 28", fill=(255, 228, 230, 255), font=font_tiny)

    # 2. 7-DAY HORIZONTAL WEEK STRIP (Directly beneath Hero Card)
    strip_y1 = hero_y2 + 18
    strip_y2 = strip_y1 + 170
    d1.rounded_rectangle([margin, strip_y1, width - margin, strip_y2], radius=22, fill=c_white, outline=c_border, width=1)

    # Header Row
    d1.ellipse([margin + 20, strip_y1 + 22, margin + 28, strip_y1 + 30], fill=c_coral_500)
    d1.text((margin + 34, strip_y1 + 16), "Minggu Ini • Sep 2026", fill=c_slate_900, font=font_sub_bold)
    d1.text((width - margin - 170, strip_y1 + 16), "Buka Kalender Penuh >", fill=c_coral_600, font=font_small)

    # 7 Columns
    col_w = (width - margin * 2 - 48) // 7
    week_items = [
        ("Kam", "11", (251, 113, 133, 255), False, False),
        ("Jum", "12", c_slate_400, False, False),
        ("Sab", "13", c_cyan, False, False),
        ("Min", "14", c_coral_500, True, False),  # Selected
        ("Sen", "15", c_cyan, False, False),
        ("Sel", "16", c_cyan, False, False),
        ("Rab", "17", c_teal, False, True)       # Peak Ovulation
    ]
    for idx, (day_name, day_num, dot_col, is_sel, is_peak) in enumerate(week_items):
        cx1 = margin + 14 + idx * (col_w + 5)
        cx2 = cx1 + col_w
        cy1 = strip_y1 + 54
        cy2 = strip_y2 - 14

        if is_sel:
            d1.rounded_rectangle([cx1, cy1, cx2, cy2], radius=14, fill=c_slate_900, outline=c_coral_400, width=2)
            d1.text((cx1 + (col_w - 28) // 2, cy1 + 10), day_name, fill=(252, 165, 165, 255), font=font_small)
            d1.text((cx1 + (col_w - 20) // 2, cy1 + 36), day_num, fill=c_white, font=font_sub_bold)
            d1.ellipse([cx1 + col_w // 2 - 4, cy2 - 16, cx1 + col_w // 2 + 4, cy2 - 8], fill=c_coral_400)
        elif is_peak:
            d1.rounded_rectangle([cx1, cy1, cx2, cy2], radius=14, fill=(236, 254, 255, 255), outline=(165, 243, 252, 255), width=1)
            d1.text((cx1 + (col_w - 28) // 2, cy1 + 10), day_name, fill=c_teal, font=font_small)
            d1.text((cx1 + (col_w - 20) // 2, cy1 + 36), day_num, fill=c_teal, font=font_sub_bold)
            d1.ellipse([cx1 + col_w // 2 - 4, cy2 - 16, cx1 + col_w // 2 + 4, cy2 - 8], fill=c_teal)
        else:
            d1.rounded_rectangle([cx1, cy1, cx2, cy2], radius=14, fill=c_slate_50, outline=c_border_subtle, width=1)
            d1.text((cx1 + (col_w - 28) // 2, cy1 + 10), day_name, fill=c_slate_400, font=font_small)
            d1.text((cx1 + (col_w - 20) // 2, cy1 + 36), day_num, fill=c_slate_800, font=font_sub_bold)
            d1.ellipse([cx1 + col_w // 2 - 4, cy2 - 16, cx1 + col_w // 2 + 4, cy2 - 8], fill=dot_col)

    # 3. BBT BIPHASIC SPARKLINE TREND CARD
    bbt_y1 = strip_y2 + 18
    bbt_y2 = bbt_y1 + 250
    d1.rounded_rectangle([margin, bbt_y1, width - margin, bbt_y2], radius=22, fill=c_white, outline=c_border, width=1)

    # Header
    d1.rounded_rectangle([margin + 20, bbt_y1 + 16, margin + 56, bbt_y1 + 52], radius=10, fill=(255, 241, 242, 255))
    d1.line([(margin + 26, bbt_y1 + 40), (margin + 36, bbt_y1 + 30), (margin + 46, bbt_y1 + 24)], fill=c_coral_600, width=2)
    d1.text((margin + 68, bbt_y1 + 16), "Tren Kurva Suhu Basal (BBT)", fill=c_slate_900, font=font_sub_bold)
    d1.text((margin + 68, bbt_y1 + 42), "Pantauan Pergeseran Biphasik (3-over-6)", fill=c_slate_400, font=font_small)

    # Normal Badge
    d1.rounded_rectangle([width - margin - 106, bbt_y1 + 16, width - margin - 20, bbt_y1 + 46], radius=10, fill=(236, 253, 245, 255), outline=(167, 243, 208, 255), width=1)
    d1.text((width - margin - 90, bbt_y1 + 22), "Normal", fill=(6, 95, 70, 255), font=font_small)

    # Coverline label with safe margin
    d1.text((width - margin - 220, bbt_y1 + 58), "Coverline 36.40°C", fill=c_slate_400, font=font_tiny)

    # Dotted Coverline at y = bbt_y1 + 130
    cly = bbt_y1 + 130
    for dx in range(margin + 20, width - margin - 20, 16):
        d1.line([(dx, cly), (dx + 8, cly)], fill=(203, 213, 225, 255), width=2)

    # Bezier Points
    bbt_pts = [
        (margin + 40, bbt_y1 + 165),
        (margin + 170, bbt_y1 + 160),
        (margin + 310, bbt_y1 + 150),
        (margin + 450, bbt_y1 + 140), # Today (36.50)
        (margin + 590, bbt_y1 + 110), # Shift
        (margin + 730, bbt_y1 + 88),
        (width - margin - 40, bbt_y1 + 75)
    ]
    # Smooth line
    for i in range(len(bbt_pts) - 1):
        d1.line([bbt_pts[i], bbt_pts[i+1]], fill=c_coral_600, width=4)
    # Dots
    for i, pt in enumerate(bbt_pts):
        dfill = c_slate_900 if i == 3 else (c_coral_600 if i < 3 else c_cyan)
        d1.ellipse([pt[0] - 7, pt[1] - 7, pt[0] + 7, pt[1] + 7], fill=c_white)
        d1.ellipse([pt[0] - 4, pt[1] - 4, pt[0] + 4, pt[1] + 4], fill=dfill)

    # Axis Labels
    d1.text((margin + 24, bbt_y2 - 30), "Fase Folikuler (Rendah)", fill=c_slate_400, font=font_tiny)
    d1.text((margin + 260, bbt_y2 - 30), "Prediksi Kenaikan Progesteron (+0.28°C)", fill=c_coral_600, font=font_tiny)
    d1.text((width - margin - 120, bbt_y2 - 30), "Fase Luteal", fill=c_slate_400, font=font_tiny)

    # 4. QUICK DAILY LOG SUMMARY CARD
    sum_y1 = bbt_y2 + 18
    sum_y2 = sum_y1 + 225
    d1.rounded_rectangle([margin, sum_y1, width - margin, sum_y2], radius=22, fill=c_white, outline=c_border, width=1)

    d1.ellipse([margin + 20, sum_y1 + 22, margin + 28, sum_y1 + 30], fill=c_coral_500)
    d1.text((margin + 34, sum_y1 + 16), "Catatan Hari Ini (14 Sep)", fill=c_slate_900, font=font_sub_bold)
    d1.text((width - margin - 140, sum_y1 + 16), "Ubah Catatan >", fill=c_coral_600, font=font_small)

    # 2 Parameter Boxes: BBT & Cervical Mucus
    box_w = (width - margin * 2 - 48) // 2
    box_y = sum_y1 + 54
    box_h = 68
    # BBT Box
    d1.rounded_rectangle([margin + 18, box_y, margin + 18 + box_w, box_y + box_h], radius=12, fill=c_slate_50, outline=c_border, width=1)
    d1.text((margin + 30, box_y + 10), "Suhu Basal (BBT)", fill=c_slate_400, font=font_tiny)
    d1.text((margin + 30, box_y + 32), "36.50 °C", fill=c_slate_900, font=font_sub_bold)

    # Mucus Box
    d1.rounded_rectangle([margin + 30 + box_w, box_y, width - margin - 18, box_y + box_h], radius=12, fill=c_slate_50, outline=c_border, width=1)
    d1.text((margin + 42 + box_w, box_y + 10), "Lendir Serviks", fill=c_slate_400, font=font_tiny)
    d1.text((margin + 42 + box_w, box_y + 32), "Putih Telur", fill=c_slate_900, font=font_sub_bold)

    # Dynamic VAS Red Alert Box (VAS 7/10 SpOG Alert)
    vas_y = sum_y1 + 134
    vas_h = 75
    d1.rounded_rectangle([margin + 18, vas_y, width - margin - 18, vas_y + vas_h], radius=12, fill=(255, 241, 242, 255), outline=(255, 228, 230, 255), width=1)
    d1.text((margin + 30, vas_y + 12), "Skala Nyeri (VAS)", fill=c_rose_alert, font=font_tiny)
    d1.text((margin + 30, vas_y + 34), "7 / 10 • Nyeri Pelvis & Pinggang", fill=c_rose_dark, font=font_sub_bold)
    # SpOG Alert badge
    d1.rounded_rectangle([width - margin - 125, vas_y + 20, width - margin - 32, vas_y + 54], radius=8, fill=c_rose_alert)
    d1.text((width - margin - 116, vas_y + 26), "SpOG Alert", fill=c_white, font=font_small)

    # Bottom Nav
    draw_bottom_nav(d1, active_tab=0)

    im1.save(os.path.join(raw_dir, "dashboard.png"), "PNG")
    print("✓ Saved exact cyclejournal_modern_compose_app dashboard.png")

    # =========================================================================
    # 2. SCREEN 2: DAILY LOG BOTTOM SHEET (Modal Bottom Sheet)
    # =========================================================================
    im2 = Image.new("RGBA", (width, height), c_light_bg)
    d2 = ImageDraw.Draw(im2)
    draw_status_bar(d2)

    # Top dimmer background overlay
    def draw_dimmer(d):
        d.rectangle([0, 0, width, 180], fill=(15, 23, 42, 110))
    draw_alpha(im2, draw_dimmer)

    # Modal Sheet with 36px rounded top corners
    sheet_y = 180
    d2.rounded_rectangle([0, sheet_y, width, height], radius=36, fill=c_white)
    # Sheet Handle Bar
    d2.rounded_rectangle([width // 2 - 36, sheet_y + 14, width // 2 + 36, sheet_y + 20], radius=4, fill=c_slate_200)

    # Sheet Header
    h_y = sheet_y + 38
    d2.text((margin + 20, h_y), "Jurnal Kondisi Hari Ini", fill=c_slate_900, font=font_bold_title)
    d2.text((margin + 20, h_y + 44), "Senin, 14 September 2026", fill=c_slate_400, font=font_body)
    # Close button circle
    cx_btn = width - margin - 40
    d2.ellipse([cx_btn - 16, h_y + 10, cx_btn + 16, h_y + 42], fill=c_slate_100)
    d2.line([(cx_btn - 6, h_y + 20), (cx_btn + 6, h_y + 32)], fill=c_slate_600, width=2)
    d2.line([(cx_btn + 6, h_y + 20), (cx_btn - 6, h_y + 32)], fill=c_slate_600, width=2)

    # Section 1: Menstrual Flow Pills
    sec1_y = h_y + 90
    d2.text((margin + 20, sec1_y), "Pendarahan Menstruasi (Flow)", fill=c_slate_900, font=font_sub_bold)
    f_chips = ["Tidak", "Bercak", "Ringan", "Sedang", "Deras"]
    f_w = (width - margin * 2 - 40 - 24) // 5
    for i, flow in enumerate(f_chips):
        fx1 = margin + 20 + i * (f_w + 6)
        fx2 = fx1 + f_w
        if flow == "Sedang":
            d2.rounded_rectangle([fx1, sec1_y + 36, fx2, sec1_y + 82], radius=10, fill=c_coral_500)
            d2.text((fx1 + (f_w - 48) // 2, sec1_y + 48), flow, fill=c_white, font=font_small)
        else:
            d2.rounded_rectangle([fx1, sec1_y + 36, fx2, sec1_y + 82], radius=10, fill=c_slate_100)
            d2.text((fx1 + (f_w - 48) // 2, sec1_y + 48), flow, fill=c_slate_600, font=font_small)

    # Section 2: Clinical Pain VAS Card (Refined functional rating)
    sec2_y = sec1_y + 106
    d2.rounded_rectangle([margin + 20, sec2_y, width - margin - 20, sec2_y + 175], radius=18, fill=(255, 241, 242, 255), outline=(255, 228, 230, 255), width=1)
    d2.text((margin + 36, sec2_y + 16), "Tingkat Nyeri (Skala 0–10)", fill=c_rose_dark, font=font_sub_bold)
    # Severity Badge
    d2.rounded_rectangle([width - margin - 150, sec2_y + 14, width - margin - 36, sec2_y + 42], radius=8, fill=(190, 18, 60, 35))
    d2.text((width - margin - 140, sec2_y + 19), "Perhatian SpOG", fill=c_rose_dark, font=font_tiny)

    d2.text((margin + 36, sec2_y + 54), "7 / 10", fill=c_rose_dark, font=font_bold_title)
    d2.text((margin + 140, sec2_y + 64), "Nyeri Berat • Membatasi gerak, butuh pereda nyeri", fill=c_rose_dark, font=font_caption)

    # Slider Track
    sl_w = width - margin * 2 - 72
    sl_y = sec2_y + 124
    d2.rounded_rectangle([margin + 36, sl_y, margin + 36 + sl_w, sl_y + 10], radius=5, fill=(254, 205, 211, 255))
    # Active 70%
    d2.rounded_rectangle([margin + 36, sl_y, margin + 36 + int(sl_w * 0.7), sl_y + 10], radius=5, fill=c_rose_dark)
    # Slider Thumb
    th_x = margin + 36 + int(sl_w * 0.7)
    d2.ellipse([th_x - 14, sl_y + 5 - 14, th_x + 14, sl_y + 5 + 14], fill=c_rose_dark)
    d2.ellipse([th_x - 6, sl_y + 5 - 6, th_x + 6, sl_y + 5 + 6], fill=c_white)

    # Section 3: Quick 1-Tap Symptom Chips
    sec3_y = sec2_y + 195
    d2.text((margin + 20, sec3_y), "Gejala Tubuh Hari Ini (Pilih Cepat)", fill=c_slate_900, font=font_sub_bold)
    sym_chips = [("Kram Pelvis", True), ("Sakit Pinggang", True), ("Payudara Sensitif", False)]
    sym_w = (width - margin * 2 - 40 - 16) // 3
    for i, (sym, is_sel) in enumerate(sym_chips):
        sx1 = margin + 20 + i * (sym_w + 8)
        sx2 = sx1 + sym_w
        if is_sel:
            d2.rounded_rectangle([sx1, sec3_y + 36, sx2, sec3_y + 84], radius=12, fill=(255, 241, 242, 255), outline=c_coral_400, width=1)
            d2.text((sx1 + (sym_w - 100) // 2, sec3_y + 50), sym, fill=c_coral_600, font=font_small)
        else:
            d2.rounded_rectangle([sx1, sec3_y + 36, sx2, sec3_y + 84], radius=12, fill=c_slate_100)
            d2.text((sx1 + (sym_w - 120) // 2, sec3_y + 50), sym, fill=c_slate_600, font=font_small)

    # Section 4: BBT & Analgesic Checkbox
    sec4_y = sec3_y + 106
    r_w = (width - margin * 2 - 40 - 12) // 2
    # BBT Box
    d2.rounded_rectangle([margin + 20, sec4_y, margin + 20 + r_w, sec4_y + 78], radius=14, fill=c_slate_50, outline=c_border, width=1)
    d2.text((margin + 34, sec4_y + 12), "Suhu Basal Tubuh (°C)", fill=c_slate_400, font=font_tiny)
    d2.text((margin + 34, sec4_y + 36), "36.50 °C", fill=c_slate_900, font=font_section)

    # Analgesic Box with Checkbox
    d2.rounded_rectangle([margin + 20 + r_w + 12, sec4_y, width - margin - 20, sec4_y + 78], radius=14, fill=c_slate_50, outline=c_border, width=1)
    d2.text((margin + 34 + r_w + 12, sec4_y + 12), "Analgesik", fill=c_slate_400, font=font_tiny)
    d2.text((margin + 34 + r_w + 12, sec4_y + 36), "Minum Obat", fill=c_slate_900, font=font_sub_bold)
    # Checkbox
    cbx = width - margin - 60
    cby = sec4_y + 38
    d2.rounded_rectangle([cbx, cby, cbx + 22, cby + 22], radius=6, fill=c_coral_500)
    draw_checkmark(d2, cbx + 11, cby + 11, c_white, size=5)

    # Section 5: Cervical Mucus Selector
    sec5_y = sec4_y + 98
    d2.text((margin + 20, sec5_y), "Lendir Serviks (Sintotermal)", fill=c_slate_900, font=font_sub_bold)
    m_chips = ["Kering", "Krim", "Cair", "Putih Telur"]
    m_w = (width - margin * 2 - 40 - 18) // 4
    for i, mucus in enumerate(m_chips):
        mx1 = margin + 20 + i * (m_w + 6)
        mx2 = mx1 + m_w
        if mucus == "Putih Telur":
            d2.rounded_rectangle([mx1, sec5_y + 36, mx2, sec5_y + 84], radius=10, fill=(207, 250, 254, 255), outline=c_cyan, width=1)
            d2.text((mx1 + (m_w - 74) // 2, sec5_y + 50), mucus, fill=(14, 116, 144, 255), font=font_small)
        else:
            d2.rounded_rectangle([mx1, sec5_y + 36, mx2, sec5_y + 84], radius=10, fill=c_slate_100)
            d2.text((mx1 + (m_w - 50) // 2, sec5_y + 50), mucus, fill=c_slate_600, font=font_small)

    # Save Action Button
    btn_y = sec5_y + 116
    draw_gradient_rect(d2, margin + 20, btn_y, width - margin - 20, btn_y + 64, corner_radius=16)
    d2.text((width // 2 - 120, btn_y + 18), "Simpan Catatan Hari Ini", fill=c_white, font=font_sub_bold)

    im2.save(os.path.join(raw_dir, "daily-log.png"), "PNG")
    print("✓ Saved exact cyclejournal_modern_compose_app daily-log.png")

    # =========================================================================
    # 3. SCREEN 3: SPOG MEDICAL REPORT (Modern Glassmorphism Card Style)
    # =========================================================================
    im3 = Image.new("RGBA", (width, height), c_light_bg)
    d3 = ImageDraw.Draw(im3)
    draw_status_bar(d3)

    # Header
    r_top = 70
    d3.text((margin, r_top), "Laporan Medis SpOG", fill=c_slate_900, font=font_bold_title)
    # FIGO Compliant Badge
    d3.rounded_rectangle([width - margin - 130, r_top + 6, width - margin, r_top + 38], radius=8, fill=c_slate_100)
    d3.text((width - margin - 120, r_top + 12), "FIGO Compliant", fill=c_slate_600, font=font_tiny)

    # Clinical Document Card
    doc_y1 = r_top + 60
    doc_y2 = doc_y1 + 830
    d3.rounded_rectangle([margin, doc_y1, width - margin, doc_y2], radius=22, fill=c_white, outline=c_border, width=1)

    # Document Title & Anon ID
    d3.text((margin + 24, doc_y1 + 20), "REKAPITULASI SIKLUS KLINIS", fill=c_slate_900, font=font_sub_bold)
    d3.text((margin + 24, doc_y1 + 50), "ID Anonim: px-7f9a2b1c4e0d", fill=c_slate_400, font=font_tiny)
    d3.text((width - margin - 130, doc_y1 + 24), "14 Sep 2026", fill=c_slate_900, font=font_small)

    # 3 Parameter Boxes: Rata-rata, Variasi, Lama Haid
    p3_w = (width - margin * 2 - 48 - 16) // 3
    p3_y = doc_y1 + 84
    p3_h = 65
    p3_items = [("Rata-rata", "28.0 Hari"), ("Variasi Siklus", "±1.5 Hari"), ("Lama Haid", "5.0 Hari")]
    for i, (l, v) in enumerate(p3_items):
        px1 = margin + 24 + i * (p3_w + 8)
        px2 = px1 + p3_w
        d3.rounded_rectangle([px1, p3_y, px2, p3_y + p3_h], radius=12, fill=c_slate_50, outline=c_border, width=1)
        d3.text((px1 + 12, p3_y + 8), l, fill=c_slate_400, font=font_tiny)
        d3.text((px1 + 12, p3_y + 30), v, fill=c_slate_900, font=font_small)

    # Mini BBT Curve Container
    m_bbt_y = p3_y + p3_h + 16
    m_bbt_h = 115
    d3.rounded_rectangle([margin + 24, m_bbt_y, width - margin - 24, m_bbt_y + m_bbt_h], radius=14, fill=c_slate_50, outline=c_border, width=1)
    d3.text((margin + 36, m_bbt_y + 10), "Pola Temperatur Biphasik (Ovulasi Terkonfirmasi)", fill=c_slate_900, font=font_small)
    # Dashed baseline
    bly = m_bbt_y + 60
    for dx in range(margin + 36, width - margin - 36, 14):
        d3.line([(dx, bly), (dx + 7, bly)], fill=(203, 213, 225, 255), width=1)
    # Curve
    d3.line([(margin + 36, bly + 10), (margin + 180, bly + 10), (margin + 320, bly - 15), (width - margin - 36, bly - 20)], fill=c_coral_600, width=3)
    # Labels
    d3.text((margin + 36, m_bbt_y + 86), "Baseline: 36.32°C", fill=c_slate_400, font=font_tiny)
    d3.text((margin + 260, m_bbt_y + 86), "Shift +0.25°C Pasca-Ovulasi", fill=c_teal, font=font_tiny)
    d3.text((width - margin - 150, m_bbt_y + 86), "Sustained High", fill=c_slate_400, font=font_tiny)

    # Anomaly Alert Box
    ano_y = m_bbt_y + m_bbt_h + 16
    ano_h = 105
    d3.rounded_rectangle([margin + 24, ano_y, width - margin - 24, ano_y + ano_h], radius=12, fill=(255, 241, 242, 255), outline=(255, 228, 230, 255), width=1)
    draw_info_circle(d3, margin + 42, ano_y + 24, c_rose_alert, c_white, r=9)
    d3.text((margin + 58, ano_y + 14), "Perhatian: Nyeri Haid Cukup Intens", fill=c_rose_alert, font=font_sub_bold)
    d3.text((margin + 40, ano_y + 44), "Tercatat skala nyeri 7/10 disertai konsumsi obat pereda nyeri. Riwayat ini siap", fill=c_rose_dark, font=font_tiny)
    d3.text((margin + 40, ano_y + 68), "dibahas saat konsultasi dengan dokter kandungan Anda.", fill=c_rose_dark, font=font_tiny)

    # Clinical History Table (FIGO 3-Cycle Log)
    tbl_y = ano_y + ano_h + 16
    tbl_h = 180
    d3.rounded_rectangle([margin + 24, tbl_y, width - margin - 24, tbl_y + tbl_h], radius=12, fill=c_white, outline=c_border, width=1)
    # Header Row
    d3.rounded_rectangle([margin + 24, tbl_y, width - margin - 24, tbl_y + 36], radius=12, fill=c_slate_50)
    d3.text((margin + 44, tbl_y + 8), "Mulai", fill=c_slate_500, font=font_tiny)
    d3.text((margin + 240, tbl_y + 8), "Panjang", fill=c_slate_500, font=font_tiny)
    d3.text((margin + 440, tbl_y + 8), "Durasi", fill=c_slate_500, font=font_tiny)
    d3.text((width - margin - 120, tbl_y + 8), "Ovulasi", fill=c_slate_500, font=font_tiny)
    d3.line([(margin + 24, tbl_y + 36), (width - margin - 24, tbl_y + 36)], fill=c_border, width=1)

    trows = [
        ("01 Jan 26", "26 Hari", "5 Hari", "15 Jan"),
        ("27 Jan 26", "28 Hari", "5 Hari", "11 Feb"),
        ("24 Feb 26", "30 Hari", "5 Hari", "13 Mar")
    ]
    for idx, (m, p, d, o) in enumerate(trows):
        ry = tbl_y + 48 + idx * 42
        d3.text((margin + 44, ry), m, fill=c_slate_900, font=font_small)
        d3.text((margin + 240, ry), p, fill=c_slate_900, font=font_sub_bold)
        d3.text((margin + 440, ry), d, fill=c_slate_900, font=font_small)
        d3.text((width - margin - 120, ry), o, fill=c_cyan, font=font_sub_bold)

    # Doctor Signature Area Preview
    sig_y = tbl_y + tbl_h + 16
    d3.rounded_rectangle([margin + 24, sig_y, width - margin - 24, sig_y + 70], radius=12, fill=c_slate_50, outline=c_border, width=1)
    d3.text((width // 2 - 110, sig_y + 12), "Kolom Catatan & Paraf Dokter SpOG", fill=c_slate_400, font=font_tiny)
    d3.line([(width - margin - 250, sig_y + 54), (width - margin - 50, sig_y + 54)], fill=c_slate_400, width=1)
    d3.text((width - margin - 240, sig_y + 40), "Tanda Tangan & Cap Dokter", fill=c_slate_500, font=font_tiny)

    # Action Buttons
    act_y = doc_y2 + 20
    # 1. Download PDF (Coral500)
    draw_gradient_rect(d3, margin, act_y, width - margin, act_y + 56, corner_radius=16)
    draw_pdf_doc_icon(d3, margin + 40, act_y + 28, c_white)
    d3.text((margin + 60, act_y + 16), "Unduh PDF Medis (Pro)", fill=c_white, font=font_sub_bold)

    # 2. Export CSV (Outlined)
    act2_y = act_y + 70
    d3.rounded_rectangle([margin, act2_y, width - margin, act2_y + 54], radius=16, fill=c_white, outline=c_border, width=1)
    draw_sheets_grid_icon(d3, margin + 40, act2_y + 27, (5, 150, 105, 255))
    d3.text((margin + 60, act2_y + 15), "Ekspor CSV Mentah (Excel / Sheets)", fill=c_slate_900, font=font_sub_bold)

    # Lifetime Pro Upgrade Card
    pro_y = act2_y + 68
    d3.rounded_rectangle([margin, pro_y, width - margin, pro_y + 92], radius=18, fill=(255, 251, 235, 255), outline=(253, 230, 138, 255), width=1)
    draw_star(d3, margin + 36, pro_y + 36, c_amber, r=14)
    d3.text((margin + 64, pro_y + 18), "Lisensi Pro Seumur Hidup", fill=c_slate_900, font=font_sub_bold)
    d3.text((margin + 64, pro_y + 48), "Unduh instan tanpa iklan selamanya", fill=c_slate_600, font=font_caption)
    # Buy Button
    d3.rounded_rectangle([width - margin - 150, pro_y + 22, width - margin - 20, pro_y + 68], radius=10, fill=c_amber)
    d3.text((width - margin - 138, pro_y + 34), "Beli Rp 49k", fill=c_white, font=font_sub_bold)

    # Bottom Nav
    draw_bottom_nav(d3, active_tab=3)

    im3.save(os.path.join(raw_dir, "medical-report.png"), "PNG")
    print("✓ Saved exact cyclejournal_modern_compose_app medical-report.png")

    # =========================================================================
    # 4. SCREEN 4: PRIVACY & SETTINGS (Modern Glassmorphism Cards)
    # =========================================================================
    im4 = Image.new("RGBA", (width, height), c_light_bg)
    d4 = ImageDraw.Draw(im4)
    draw_status_bar(d4)

    # Header
    s_top = 70
    d4.text((margin, s_top), "PREFERENSI & KONTROL", fill=c_coral_600, font=font_tiny)
    d4.text((margin, s_top + 18), "Pengaturan", fill=c_slate_900, font=font_bold_title)
    # Settings Gear Icon in Box
    d4.rounded_rectangle([width - margin - 44, s_top + 6, width - margin, s_top + 50], radius=10, fill=c_slate_100)
    draw_tune_icon(d4, width - margin - 22, s_top + 28, c_slate_500)

    # GROUP 0: MONETIZATION / PRO LICENSE STATUS CARD
    g0_y = s_top + 70
    g0_h = 145
    d4.rounded_rectangle([margin, g0_y, width - margin, g0_y + g0_h], radius=22, fill=c_amber)
    d4.text((margin + 24, g0_y + 14), "VERSI GRATIS (DIDUKUNG IKLAN)", fill=(255, 255, 255, 220), font=font_tiny)
    d4.text((margin + 24, g0_y + 34), "Upgrade ke Lifetime Pro", fill=c_white, font=font_section)
    d4.text((margin + 24, g0_y + 68), "Beli putus sekali seumur hidup: 100% bebas iklan,", fill=(255, 255, 255, 235), font=font_tiny)
    d4.text((margin + 24, g0_y + 88), "ekspor PDF tanpa batas & sinkronisasi cloud.", fill=(255, 255, 255, 235), font=font_tiny)
    # Button inside
    d4.rounded_rectangle([margin + 24, g0_y + 108, margin + 220, g0_y + 138], radius=8, fill=c_white)
    draw_sparkle(d4, margin + 40, g0_y + 123, (180, 83, 9, 255), r=5)
    d4.text((margin + 52, g0_y + 115), "Beli Putus Rp 49.000", fill=(180, 83, 9, 255), font=font_small)

    # GROUP 1: KEAMANAN & AKSES APLIKASI
    g1_y = g0_y + g0_h + 16
    g1_h = 240
    d4.rounded_rectangle([margin, g1_y, width - margin, g1_y + g1_h], radius=22, fill=c_white, outline=c_border, width=1)
    d4.text((margin + 24, g1_y + 16), "KEAMANAN & KUNCI APLIKASI", fill=c_coral_600, font=font_tiny)

    # Row 1: PIN
    r1_y = g1_y + 44
    d4.text((margin + 24, r1_y), "Kunci PIN 4-Digit", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, r1_y + 26), "Aktif (PIN 4-Digit)", fill=c_slate_400, font=font_caption)
    d4.rounded_rectangle([width - margin - 110, r1_y + 4, width - margin - 24, r1_y + 42], radius=10, fill=c_coral_500)
    d4.text((width - margin - 96, r1_y + 14), "Atur PIN", fill=c_white, font=font_small)

    d4.line([(margin + 24, r1_y + 56), (width - margin - 24, r1_y + 56)], fill=c_border_subtle, width=1)

    # Row 2: Biometric Switch (ON)
    r2_y = r1_y + 70
    d4.text((margin + 24, r2_y), "Kunci Sidik Jari / Wajah", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, r2_y + 26), "Buka cepat saat ponsel dipinjam", fill=c_slate_400, font=font_caption)
    draw_switch(d4, width - margin - 74, r2_y + 6, is_on=True)

    d4.line([(margin + 24, r2_y + 56), (width - margin - 24, r2_y + 56)], fill=c_border_subtle, width=1)

    # Row 3: Auto-Lock
    r3_y = r2_y + 70
    d4.text((margin + 24, r3_y), "Kunci Otomatis", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, r3_y + 26), "Saat aplikasi di latar belakang", fill=c_slate_400, font=font_caption)
    d4.rounded_rectangle([width - margin - 100, r3_y + 6, width - margin - 24, r3_y + 42], radius=8, fill=c_slate_100)
    d4.text((width - margin - 90, r3_y + 14), "30 Detik", fill=c_slate_900, font=font_small)

    # GROUP 2: PRIVASI & CADANGAN DATA
    g2_y = g1_y + g1_h + 16
    g2_h = 240
    d4.rounded_rectangle([margin, g2_y, width - margin, g2_y + g2_h], radius=22, fill=c_white, outline=c_border, width=1)
    d4.text((margin + 24, g2_y + 16), "PRIVASI & CADANGAN", fill=c_coral_600, font=font_tiny)

    d4.text((margin + 24, g2_y + 44), "Kunci Pemulihan Cadangan", fill=c_slate_900, font=font_sub_bold)
    d4.text((width - margin - 70, g2_y + 44), "Salin", fill=c_coral_600, font=font_small)

    # Recovery key box
    rk_y = g2_y + 74
    d4.rounded_rectangle([margin + 24, rk_y, width - margin - 24, rk_y + 42], radius=10, fill=c_slate_50, outline=c_border, width=1)
    d4.text((margin + 36, rk_y + 12), "px-7f9a2b1c4e0d", fill=c_slate_600, font=font_caption)

    d4.line([(margin + 24, rk_y + 54), (width - margin - 24, rk_y + 54)], fill=c_border_subtle, width=1)

    # Cloud Backup Row
    cb_y = rk_y + 66
    d4.text((margin + 24, cb_y), "Cadangan Cloud Terkunci", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, cb_y + 24), "Hanya tersimpan dalam bentuk terenkripsi", fill=c_slate_400, font=font_tiny)
    # Aktif Badge
    d4.rounded_rectangle([width - margin - 80, cb_y + 4, width - margin - 24, cb_y + 34], radius=8, fill=(236, 253, 245, 255))
    d4.text((width - margin - 70, cb_y + 10), "Aktif", fill=(6, 95, 70, 255), font=font_small)

    # 2 Outlined Buttons: Cadangkan & Pulihkan
    btn_w = (width - margin * 2 - 48 - 12) // 2
    by1 = cb_y + 54
    # Cadangkan
    d4.rounded_rectangle([margin + 24, by1, margin + 24 + btn_w, by1 + 44], radius=12, outline=c_border, width=1)
    draw_cloud_icon(d4, margin + 44, by1 + 22, c_coral_600, is_upload=True)
    d4.text((margin + 62, by1 + 12), "Cadangkan", fill=c_slate_900, font=font_small)
    # Pulihkan
    d4.rounded_rectangle([margin + 24 + btn_w + 12, by1, width - margin - 24, by1 + 44], radius=12, outline=c_border, width=1)
    draw_cloud_icon(d4, margin + 44 + btn_w + 12, by1 + 22, c_teal, is_upload=False)
    d4.text((margin + 62 + btn_w + 12, by1 + 12), "Pulihkan", fill=c_slate_900, font=font_small)

    # GROUP 3: TAMPILAN & NOTIFIKASI
    g3_y = g2_y + g2_h + 16
    g3_h = 220
    d4.rounded_rectangle([margin, g3_y, width - margin, g3_y + g3_h], radius=22, fill=c_white, outline=c_border, width=1)
    d4.text((margin + 24, g3_y + 16), "TAMPILAN & NOTIFIKASI", fill=c_coral_600, font=font_tiny)

    # Row 1: Mode Samaran (ON)
    t1_y = g3_y + 44
    d4.text((margin + 24, t1_y), "Mode Samaran (Anti-Intip)", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, t1_y + 24), "Samarkan istilah sensitif di publik", fill=c_slate_400, font=font_tiny)
    draw_switch(d4, width - margin - 74, t1_y + 6, is_on=True)

    d4.line([(margin + 24, t1_y + 54), (width - margin - 24, t1_y + 54)], fill=c_border_subtle, width=1)

    # Row 2: Mode Gelap Subuh (OFF)
    t2_y = t1_y + 68
    d4.text((margin + 24, t2_y), "Mode Gelap Subuh (OLED)", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, t2_y + 24), "Ramah mata saat bangun ukur suhu", fill=c_slate_400, font=font_tiny)
    draw_switch(d4, width - margin - 74, t2_y + 6, is_on=False)

    d4.line([(margin + 24, t2_y + 54), (width - margin - 24, t2_y + 54)], fill=c_border_subtle, width=1)

    # Row 3: BBT Reminder Time
    t3_y = t2_y + 68
    d4.text((margin + 24, t3_y), "Pengingat Suhu Basal (BBT)", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 24, t3_y + 24), "Alarm lembut pukul 05:30 pagi", fill=c_slate_400, font=font_tiny)
    d4.rounded_rectangle([width - margin - 80, t3_y + 6, width - margin - 24, t3_y + 38], radius=8, fill=(255, 241, 242, 255))
    d4.text((width - margin - 72, t3_y + 12), "05:30", fill=c_rose_dark, font=font_small)

    # DANGER ZONE: HAPUS DATA & RESET
    dz_y = g3_y + g3_h + 16
    dz_h = 130
    d4.rounded_rectangle([margin, dz_y, width - margin, dz_y + dz_h], radius=22, fill=(255, 241, 242, 255), outline=(255, 228, 230, 255), width=1)
    d4.text((margin + 24, dz_y + 16), "HAPUS DATA & RESET", fill=c_rose_alert, font=font_tiny)
    d4.text((margin + 24, dz_y + 36), "Menghapus seluruh catatan siklus lokal di ponsel dan cadangan cloud.", fill=c_rose_dark, font=font_tiny)
    # Red Button
    d4.rounded_rectangle([margin + 24, dz_y + 64, width - margin - 24, dz_y + 114], radius=12, fill=c_rose_alert)
    d4.text((width // 2 - 140, dz_y + 78), "Hapus Seluruh Data Permanen", fill=c_white, font=font_sub_bold)

    # Bottom Nav
    draw_bottom_nav(d4, active_tab=4)

    im4.save(os.path.join(raw_dir, "privacy-security.png"), "PNG")
    print("✓ Saved exact cyclejournal_modern_compose_app privacy-security.png")

    # =========================================================================
    # 5. SCREEN 5: DATA PORTABILITY & PRIVACY SOVEREIGNTY (Splash / Trust View)
    # =========================================================================
    im5 = Image.new("RGBA", (width, height), (255, 255, 255, 255))
    d5 = ImageDraw.Draw(im5)
    # Soft vertical gradient background (clean, no circle blobs)
    draw_gradient_rect(d5, 0, 0, width, height, r1=255, g1=245, b1=243, r2=255, g2=255, b2=255, corner_radius=0)

    draw_status_bar(d5)

    # Top Pill: FIGO STANDARD
    d5.rounded_rectangle([margin, 70, margin + 140, 106], radius=18, fill=(255, 241, 242, 220), outline=(255, 228, 230, 255), width=1)
    d5.text((margin + 16, 78), "FIGO STANDARD", fill=c_coral_600, font=font_tiny)
    d5.text((width - margin - 40, 78), "v1.0", fill=c_slate_400, font=font_small)

    # Center Logo Emblem: Glowing Shell with Pearl Core (No white box)
    scx = width // 2
    scy = 440
    # Outer ambient glow
    d5.ellipse([scx - 130, scy - 130, scx + 130, scy + 130], outline=(255, 138, 113, 50), width=8)
    d5.ellipse([scx - 110, scy - 110, scx + 110, scy + 110], outline=(255, 94, 125, 90), width=10)

    # Coral Shell Circle
    draw_gradient_rect(d5, scx - 85, scy - 85, scx + 85, scy + 85, corner_radius=85)

    # Transparent PNG Logo or Iridescent Pearl Core placed precisely inside shell
    logo_pearl = logo_master.resize((120, 120), Image.Resampling.LANCZOS)
    im5.paste(logo_pearl, (scx - 60, scy - 60), logo_pearl)

    # Brand Title
    d5.text((scx - 135, scy + 120), "Cycle", fill=c_slate_900, font=font_bold_hero)
    d5.text((scx - 135 + 115, scy + 120), "Journal", fill=c_coral_600, font=font_bold_hero)

    d5.text((scx - 195, scy + 185), "PRIVASI PENUH • STANDAR DOKTER KANDUNGAN", fill=c_slate_400, font=font_small)

    # Frosted Trust Capsule
    cap_w = 400
    d5.rounded_rectangle([scx - cap_w // 2, scy + 225, scx + cap_w // 2, scy + 275], radius=20, fill=c_white, outline=(255, 255, 255, 255), width=1)
    draw_checkmark_circle(d5, scx - cap_w // 2 + 24, scy + 250, (236, 253, 245, 255), (5, 150, 105, 255), r=10)
    d5.text((scx - cap_w // 2 + 44, scy + 238), "Enkripsi Mandiri • 100% Offline di Ponsel", fill=c_slate_700, font=font_caption)

    # Clinical Trust Card (Total Data Sovereignty Features)
    tc_y = scy + 310
    tc_h = 420
    d5.rounded_rectangle([margin, tc_y, width - margin, tc_y + tc_h], radius=24, fill=c_white, outline=c_border, width=1)

    d5.text((margin + 28, tc_y + 24), "Kedaulatan Penuh Atas Data Anda", fill=c_slate_900, font=font_section)
    disclaimer = (
        "CycleJournal adalah instrumen rekam siklus klinis mandiri.\n"
        "Seluruh catatan biomarker, suhu basal BBT, dan rekam nyeri\n"
        "tersimpan 100% lokal terenkripsi SQLCipher di ponsel Anda."
    )
    d5.text((margin + 28, tc_y + 68), disclaimer, fill=c_slate_600, font=font_caption)

    # 3 Trust Feature Rows with real vector icons
    tf_y1 = tc_y + 175
    # Feature 1: CSV Export
    draw_sheets_grid_icon(d5, margin + 46, tf_y1 + 10, (5, 150, 105, 255))
    d5.text((margin + 70, tf_y1), "Ekspor Data Mentah CSV & Excel Kapan Saja", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 70, tf_y1 + 28), "Portabilitas data tanpa batas kepemilikan pihak ketiga.", fill=c_slate_400, font=font_tiny)

    tf_y2 = tf_y1 + 75
    # Feature 2: Hardware Encryption
    draw_lock(d5, margin + 46, tf_y2 + 10, c_coral_600, s=9)
    d5.text((margin + 70, tf_y2), "Enkripsi Hardware SQLCipher Tanpa Akun", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 70, tf_y2 + 28), "Bebas dari pelacak analitik, email, dan server luar.", fill=c_slate_400, font=font_tiny)

    tf_y3 = tf_y2 + 75
    # Feature 3: Permanent Destruction
    draw_alert_triangle(d5, margin + 46, tf_y3 + 10, (254, 205, 211, 255), c_rose_dark, size=10)
    d5.text((margin + 70, tf_y3), "Pemusnahan Data Permanen Sekali Sentuh", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 70, tf_y3 + 28), "Hak mutlak GDPR & UU PDP untuk menghapus seluruh data.", fill=c_slate_400, font=font_tiny)

    # Action Buttons
    cta_y = tc_y + tc_h + 36
    draw_gradient_rect(d5, margin, cta_y, width - margin, cta_y + 64, corner_radius=18)
    d5.text((width // 2 - 80, cta_y + 18), "Masuk Aplikasi", fill=c_white, font=font_sub_bold)
    draw_arrow_forward(d5, width // 2 + 75, cta_y + 32, c_white)

    d5.text((width // 2 - 130, cta_y + 80), "Mulai Gratis • Tanpa Pendaftaran Akun", fill=c_slate_400, font=font_small)

    im5.save(os.path.join(raw_dir, "data-portability.png"), "PNG")
    print("✓ Saved exact cyclejournal_modern_compose_app data-portability.png")

    # Manifest for Goldie
    manifest = {
        "device": "pixel-10-pro",
        "screenshots": [
            {
                "sceneId": "dashboard",
                "file": os.path.abspath(os.path.join(raw_dir, "dashboard.png")).replace("\\", "/")
            },
            {
                "sceneId": "daily-log",
                "file": os.path.abspath(os.path.join(raw_dir, "daily-log.png")).replace("\\", "/")
            },
            {
                "sceneId": "medical-report",
                "file": os.path.abspath(os.path.join(raw_dir, "medical-report.png")).replace("\\", "/")
            },
            {
                "sceneId": "privacy-security",
                "file": os.path.abspath(os.path.join(raw_dir, "privacy-security.png")).replace("\\", "/")
            },
            {
                "sceneId": "data-portability",
                "file": os.path.abspath(os.path.join(raw_dir, "data-portability.png")).replace("\\", "/")
            }
        ]
    }
    with open(os.path.join(raw_dir, "manifest.json"), "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)
    print("✓ Saved manifest.json for goldie at", os.path.join(raw_dir, "manifest.json"))

if __name__ == "__main__":
    render_screens()
