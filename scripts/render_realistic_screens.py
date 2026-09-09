import os
import json
from PIL import Image, ImageDraw, ImageFont

def render_screens():
    raw_dir = os.path.abspath("out/raw/pixel-10-pro")
    os.makedirs(raw_dir, exist_ok=True)
    width, height = 1080, 1920

    # Fonts
    font_bold_hero = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 46)
    font_bold_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 36)
    font_section = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 28)
    font_sub_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 23)
    font_body = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 22)
    font_caption = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 19)
    font_small = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 17)
    font_tab = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 17)

    logo_master = Image.open("logo cyclejournal.jpg").convert("RGBA")

    # Colors
    c_white = (255, 255, 255, 255)
    c_slate_900 = (15, 23, 42, 255)
    c_slate_800 = (30, 41, 59, 255)
    c_slate_600 = (71, 85, 105, 255)
    c_slate_400 = (148, 163, 184, 255)
    c_border = (241, 245, 249, 255)
    c_coral = (255, 138, 113, 255)
    c_pink = (255, 94, 125, 255)
    c_amber_start = (245, 158, 11, 255)
    c_amber_end = (234, 88, 12, 255)
    margin = 92

    def draw_status_bar(draw):
        draw.text((margin, 28), "09:41", fill=c_slate_900, font=font_caption)
        draw.text((width - margin - 100, 28), "100% 5G", fill=c_slate_900, font=font_caption)

    import time
    def save_image_safe(image, filepath):
        tmp = filepath + ".tmp.png"
        image.save(tmp, "PNG")
        for _ in range(10):
            try:
                if os.path.exists(filepath):
                    os.remove(filepath)
                os.replace(tmp, filepath)
                return
            except Exception:
                time.sleep(0.1)
        image.save(filepath, "PNG")

    def draw_gradient_rect(draw, x1, y1, x2, y2, r1=255, g1=138, b1=113, r2=255, g2=94, b2=125, corner_radius=28):
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

    def draw_bottom_nav(draw, active_tab=0):
        bar_w = width - margin * 2
        bar_h = 100
        bar_y = height - 135
        bar_x = margin

        draw.rounded_rectangle(
            [bar_x, bar_y, bar_x + bar_w, bar_y + bar_h],
            radius=50,
            fill=(255, 255, 255, 255),
            outline=c_border,
            width=2
        )

        # Tab 1: Beranda (House Icon)
        t1_col = c_pink if active_tab == 0 else c_slate_400
        hx = bar_x + 85
        hy = bar_y + 24
        draw.polygon([(hx, hy), (hx - 14, hy + 12), (hx + 14, hy + 12)], fill=t1_col)
        draw.rectangle([hx - 10, hy + 12, hx + 10, hy + 26], fill=t1_col)
        draw.text((bar_x + 58, bar_y + 58), "Beranda", fill=t1_col, font=font_tab)

        # Tab 2: Kalender (Calendar Icon)
        t2_col = c_pink if active_tab == 1 else c_slate_400
        cx = bar_x + 250
        cy = bar_y + 24
        draw.rectangle([cx - 12, cy, cx + 12, cy + 24], outline=t2_col, width=2)
        draw.line([(cx - 12, cy + 8), (cx + 12, cy + 8)], fill=t2_col, width=2)
        draw.text((bar_x + 220, bar_y + 58), "Kalender", fill=t2_col, font=font_tab)

        # Center Elevated Action Button (wf-fab: 84px circle in gradient with + icon)
        fab_size = 84
        fab_cx = width // 2
        fab_cy = bar_y + bar_h // 2 - 16
        draw_gradient_rect(draw, fab_cx - fab_size // 2, fab_cy - fab_size // 2, fab_cx + fab_size // 2, fab_cy + fab_size // 2, corner_radius=fab_size // 2)
        draw.line([(fab_cx - 14, fab_cy), (fab_cx + 14, fab_cy)], fill=c_white, width=4)
        draw.line([(fab_cx, fab_cy - 14), (fab_cx, fab_cy + 14)], fill=c_white, width=4)

        # Tab 3: Laporan SpOG (Document Icon)
        t3_col = c_pink if active_tab == 2 else c_slate_400
        rx = bar_x + 630
        ry = bar_y + 24
        draw.rectangle([rx - 10, ry, rx + 10, ry + 24], outline=t3_col, width=2)
        draw.line([(rx - 6, ry + 6), (rx + 6, ry + 6)], fill=t3_col, width=2)
        draw.line([(rx - 6, ry + 12), (rx + 6, ry + 12)], fill=t3_col, width=2)
        draw.line([(rx - 6, ry + 18), (rx + 2, ry + 18)], fill=t3_col, width=2)
        draw.text((bar_x + 602, bar_y + 58), "Laporan", fill=t3_col, font=font_tab)

        # Tab 4: Pengaturan (Sliders / Settings Icon)
        t4_col = c_pink if active_tab == 3 else c_slate_400
        sx = bar_x + 800
        sy = bar_y + 24
        draw.line([(sx - 12, sy + 4), (sx + 12, sy + 4)], fill=t4_col, width=2)
        draw.ellipse([sx - 4, sy + 1, sx + 2, sy + 7], fill=t4_col)
        draw.line([(sx - 12, sy + 14), (sx + 12, sy + 14)], fill=t4_col, width=2)
        draw.ellipse([sx + 2, sy + 11, sx + 8, sy + 17], fill=t4_col)
        draw.text((bar_x + 760, bar_y + 58), "Pengaturan", fill=t4_col, font=font_tab)

    # =========================================================================
    # 1. SCREEN 1: MODERN DASHBOARD (from cyclejournal_modern_compose_app (1).kt)
    # =========================================================================
    im1 = Image.new("RGBA", (width, height), (251, 251, 252, 255))
    d1 = ImageDraw.Draw(im1)
    draw_status_bar(d1)

    # TopAppBar (with Logo + Title + Discreet Shield icon + DarkMode icon + Settings)
    logo_small = logo_master.resize((50, 50))
    im1.paste(logo_small, (margin, 76), logo_small)
    d1.text((margin + 62, 80), "CycleJournal", fill=c_slate_900, font=font_bold_title)

    # Discreet & Dark Mode action icons
    d1.rounded_rectangle([width - margin - 170, 80, width - margin - 120, 126], radius=12, fill=c_white, outline=c_border, width=1)
    d1.text((width - margin - 156, 88), "[S]", fill=c_pink, font=font_caption) # Shield

    d1.rounded_rectangle([width - margin - 110, 80, width - margin - 60, 126], radius=12, fill=c_white, outline=c_border, width=1)
    d1.text((width - margin - 96, 88), "[M]", fill=c_slate_600, font=font_caption) # Moon

    d1.rounded_rectangle([width - margin - 50, 80, width - margin, 126], radius=12, fill=c_white, outline=c_border, width=1)
    d1.text((width - margin - 36, 88), "[=]", fill=c_slate_600, font=font_caption)

    # 1. 7-Day Horizontal Weekly Strip Navigator (Monday to Sunday)
    week_strip_y = 150
    week_items = [
        ("Kam", "11", "Haid", False),
        ("Jum", "12", "Foli", False),
        ("Sab", "13", "Pra", False),
        ("Min", "14", "Subur", True), # Active / Selected
        ("Sen", "15", "Subur", False),
        ("Sel", "16", "Subur", False),
        ("Rab", "17", "Ovulasi", False)
    ]
    w_card_w = (width - margin * 2 - 60) // 7
    for idx, (w_day, w_num, w_phase, is_sel) in enumerate(week_items):
        wx1 = margin + idx * (w_card_w + 10)
        wx2 = wx1 + w_card_w
        if is_sel:
            draw_gradient_rect(d1, wx1, week_strip_y, wx2, week_strip_y + 115, corner_radius=20)
            d1.text((wx1 + (w_card_w - 30) // 2, week_strip_y + 12), w_day, fill=c_white, font=font_caption)
            d1.text((wx1 + (w_card_w - 24) // 2, week_strip_y + 42), w_num, fill=c_white, font=font_sub_bold)
            d1.text((wx1 + (w_card_w - 40) // 2, week_strip_y + 78), w_phase, fill=(255, 241, 242, 230), font=font_small)
        else:
            d1.rounded_rectangle([wx1, week_strip_y, wx2, week_strip_y + 115], radius=20, fill=c_white, outline=c_border, width=1)
            d1.text((wx1 + (w_card_w - 30) // 2, week_strip_y + 12), w_day, fill=c_slate_400, font=font_caption)
            d1.text((wx1 + (w_card_w - 24) // 2, week_strip_y + 42), w_num, fill=c_slate_800, font=font_sub_bold)
            d1.text((wx1 + (w_card_w - 40) // 2, week_strip_y + 78), w_phase, fill=c_slate_400, font=font_small)

    # 2. Hero Circular Cycle Ring Gauge (220dp circular ring)
    gauge_box_top = 285
    gauge_box_bottom = 730
    draw_gradient_rect(d1, margin, gauge_box_top, width - margin, gauge_box_bottom, corner_radius=32)

    # Center Circular Progress Meter
    gcx = width // 2
    # 3. Clinical Metric Bento Grid (4 Cards: BBT, VAS Pain, Cervical Mucus, FIGO Regularity)
    bento_y = 750
    b_card_w = (width - margin * 2 - 20) // 2
    b_card_h = 135

    # Card A: BBT
    d1.rounded_rectangle([margin, bento_y, margin + b_card_w, bento_y + b_card_h], radius=24, fill=c_white, outline=c_border, width=1)
    d1.text((margin + 28, bento_y + 16), "Suhu Basal (BBT)", fill=c_slate_400, font=font_caption)
    d1.text((margin + 28, bento_y + 48), "36.50 °C", fill=c_slate_900, font=font_section)
    d1.text((margin + 28, bento_y + 92), "▲ +0.20°C (Biphasic Shift)", fill=(13, 148, 136, 255), font=font_caption)

    # Card B: VAS Pain
    d1.rounded_rectangle([margin + b_card_w + 20, bento_y, width - margin, bento_y + b_card_h], radius=24, fill=c_white, outline=c_border, width=1)
    d1.text((margin + b_card_w + 48, bento_y + 16), "Skala Nyeri (VAS)", fill=c_slate_400, font=font_caption)
    d1.text((margin + b_card_w + 48, bento_y + 48), "7 / 10", fill=(220, 38, 38, 255), font=font_section)
    d1.text((margin + b_card_w + 48, bento_y + 92), "Sedang (Aktivitas Terganggu)", fill=(153, 27, 27, 255), font=font_caption)

    # Card C: Cervical Mucus
    bento_y2 = bento_y + b_card_h + 16
    d1.rounded_rectangle([margin, bento_y2, margin + b_card_w, bento_y2 + b_card_h], radius=24, fill=c_white, outline=c_border, width=1)
    d1.text((margin + 28, bento_y2 + 16), "Lendir Serviks", fill=c_slate_400, font=font_caption)
    d1.text((margin + 28, bento_y2 + 48), "Putih Telur", fill=(14, 116, 144, 255), font=font_section)
    d1.text((margin + 28, bento_y2 + 92), "Kesuburan Puncak (Peak)", fill=c_slate_600, font=font_caption)

    # Card D: FIGO Regularity
    d1.rounded_rectangle([margin + b_card_w + 20, bento_y2, width - margin, bento_y2 + b_card_h], radius=24, fill=c_white, outline=c_border, width=1)
    d1.text((margin + b_card_w + 48, bento_y2 + 16), "Keteraturan FIGO", fill=c_slate_400, font=font_caption)
    d1.text((margin + b_card_w + 48, bento_y2 + 48), "Sangat Teratur", fill=c_slate_900, font=font_section)
    d1.text((margin + b_card_w + 48, bento_y2 + 92), "Variabilitas SD σ: ±1.5 Hari", fill=c_slate_600, font=font_caption)
    # 4. Medical Red Flag Alert Banner (if VAS >= 7)
    alert_y = bento_y2 + b_card_h + 18
    d1.rounded_rectangle([margin, alert_y, width - margin, alert_y + 115], radius=24, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d1.text((margin + 24, alert_y + 16), "[!] Indikasi Anomali Medis (FIGO Red Flag):", fill=(153, 27, 27, 255), font=font_sub_bold)
    d1.text((margin + 24, alert_y + 52), "Nyeri level tinggi terdeteksi (VAS 7/10). Ditandai pada", fill=(153, 27, 27, 255), font=font_caption)
    d1.text((margin + 24, alert_y + 78), "laporan SpOG sebagai indikasi dismenore berat.", fill=(153, 27, 27, 255), font=font_caption)

    # 5. Daily Journal Preview Card
    prev_y = alert_y + 135
    d1.rounded_rectangle([margin, prev_y, width - margin, prev_y + 195], radius=28, fill=c_white, outline=c_border, width=1)
    d1.text((margin + 28, prev_y + 20), "Jurnal Harian Aktif (14 September 2026)", fill=c_slate_900, font=font_section)
    d1.text((margin + 28, prev_y + 64), "• Pendarahan: Tidak ada perdarahan aktif", fill=c_slate_600, font=font_body)
    d1.text((margin + 28, prev_y + 104), "• Gejala & Analgesik: Kram pelvis ringan, paracetamol 500mg diminum", fill=c_slate_600, font=font_body)
    d1.text((margin + 28, prev_y + 144), "• Metode Sintotermal: Suhu 36.50°C terkonfirmasi di atas coverline 36.40°C", fill=(13, 148, 136, 255), font=font_body)

    # Floating Bottom Nav Bar
    draw_bottom_nav(d1, active_tab=0)

    save_image_safe(im1, os.path.join(raw_dir, "dashboard.png"))
    print("✓ Saved exact cyclejournal_modern_compose_app dashboard.png")

    # =========================================================================
    # 2. SCREEN 2: DAILY LOG MODAL (DailyJournalLogModal from user file)
    # =========================================================================
    im2 = Image.new("RGBA", (width, height), (248, 250, 252, 255))
    d2 = ImageDraw.Draw(im2)
    draw_status_bar(d2)

    # Top dimmer area
    d2.rectangle([0, 0, width, 160], fill=(0, 0, 0, 45))

    # Bottom Sheet Surface with 40px rounded top corners
    d2.rounded_rectangle([0, 160, width, height], radius=40, fill=c_white)
    # Sheet handle
    d2.rounded_rectangle([width // 2 - 40, 185, width // 2 + 40, 195], radius=10, fill=(226, 232, 240, 255))

    d2.text((margin, 225), "Catat Biomarker Harian", fill=c_slate_900, font=font_bold_title)
    d2.text((margin, 280), "Senin, 14 September 2026 • Fase Subur", fill=c_slate_600, font=font_body)
    d2.text((width - margin - 40, 235), "[X]", fill=c_slate_400, font=font_section)

    d2.line([(margin, 335), (width - margin, 335)], fill=c_border, width=2)

    # Section 1: Flow (Pill chips with 100px radius)
    d2.text((margin, 365), "Intensitas Aliran Haid (Flow)", fill=c_slate_900, font=font_section)
    flow_chips = [("Tidak", False), ("Bercak", False), ("Ringan", False), ("Sedang", True), ("Deras", False)]
    chip_w = (width - margin * 2 - 40) // 5
    for i, (label, active) in enumerate(flow_chips):
        cx1 = margin + i * (chip_w + 10)
        cx2 = cx1 + chip_w
        if active:
            draw_gradient_rect(d2, cx1, 415, cx2, 485, corner_radius=100)
            d2.text((cx1 + (chip_w - 60) // 2, 438), label, fill=c_white, font=font_sub_bold)
        else:
            d2.rounded_rectangle([cx1, 415, cx2, 485], radius=100, fill=(248, 250, 252, 255), outline=c_border, width=2)
            d2.text((cx1 + (chip_w - 60) // 2, 438), label, fill=c_slate_900, font=font_sub_bold)

    # Section 2: Pain VAS Slider with Functional Severity (None, Mild, Moderate, Severe, Extreme)
    d2.text((margin, 525), "Skala Nyeri Klinis (VAS 0–10)", fill=c_slate_900, font=font_section)
    d2.text((width - margin - 90, 525), "7 / 10", fill=(220, 38, 38, 255), font=font_section)

    # Slider Track
    d2.rounded_rectangle([margin, 585, width - margin, 601], radius=10, fill=(241, 245, 249, 255))
    active_slider_w = int((width - margin * 2) * 0.7)
    d2.rounded_rectangle([margin, 585, margin + active_slider_w, 601], radius=10, fill=(220, 38, 38, 255))
    d2.ellipse([margin + active_slider_w - 18, 593 - 18, margin + active_slider_w + 18, 593 + 18], fill=(220, 38, 38, 255))

    # VAS Functional Category Pill & Text
    d2.rounded_rectangle([margin, 625, margin + 260, 665], radius=100, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=1)
    d2.text((margin + 20, 633), "Kategori: Nyeri Berat (Severe)", fill=(153, 27, 27, 255), font=font_caption)
    d2.text((margin, 685), "Membatasi aktivitas harian, butuh pereda nyeri & tirah baring.", fill=(153, 27, 27, 255), font=font_body)

    # Red Flag Warning Card (Rounded 20px)
    d2.rounded_rectangle([margin, 735, width - margin, 845], radius=20, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d2.text((margin + 25, 755), "[!] Peringatan Anomali Medis:", fill=(153, 27, 27, 255), font=font_sub_bold)
    d2.text((margin + 25, 792), "Nyeri level tinggi terdeteksi. Ini akan ditandai pada laporan SpOG sebagai indikasi dismenore berat/endometriosis.", fill=(153, 27, 27, 255), font=font_caption)

    # Section 3: BBT & Analgesic (Rounded 20px)
    d2.rounded_rectangle([margin, 875, width // 2 - 15, 985], radius=20, fill=c_white, outline=c_border, width=2)
    d2.text((margin + 25, 895), "Suhu Basal Tubuh (°C)", fill=c_slate_600, font=font_caption)
    d2.text((margin + 25, 930), "36.50 °C", fill=c_slate_900, font=font_section)

    d2.rounded_rectangle([width // 2 + 15, 875, width - margin, 985], radius=20, fill=(248, 250, 252, 255), outline=c_border, width=2)
    d2.text((width // 2 + 35, 900), "[V] Analgesik", fill=c_slate_900, font=font_section)
    d2.text((width // 2 + 35, 940), "Konsumsi pereda nyeri", fill=c_slate_600, font=font_caption)

    # Section 4: Cervical Mucus (Pill chips with 100px radius)
    d2.text((margin, 1025), "Karakteristik Lendir Serviks (Sintotermal)", fill=c_slate_900, font=font_section)
    mucus_chips = [("Kering", False), ("Krim", False), ("Cair", False), ("Putih Telur", True)]
    m_chip_w = (width - margin * 2 - 30) // 4
    for i, (label, active) in enumerate(mucus_chips):
        cx1 = margin + i * (m_chip_w + 10)
        cx2 = cx1 + m_chip_w
        if active:
            d2.rounded_rectangle([cx1, 1075, cx2, 1145], radius=100, fill=(207, 250, 254, 255), outline=(14, 116, 144, 255), width=2)
            d2.text((cx1 + (m_chip_w - 90) // 2, 1098), label, fill=(14, 116, 144, 255), font=font_sub_bold)
        else:
            d2.rounded_rectangle([cx1, 1075, cx2, 1145], radius=100, fill=(248, 250, 252, 255), outline=c_border, width=2)
            d2.text((cx1 + (m_chip_w - 70) // 2, 1098), label, fill=c_slate_900, font=font_sub_bold)

    # Section 5: Clinical Notes
    d2.text((margin, 1185), "Catatan Tambahan Pasien", fill=c_slate_900, font=font_section)
    d2.rounded_rectangle([margin, 1235, width - margin, 1400], radius=20, fill=c_white, outline=c_border, width=2)
    d2.text((margin + 25, 1265), "Nyeri pelvis terasa sejak pagi, konsumsi paracetamol 500mg...", fill=c_slate_900, font=font_body)

    # Save Pill Button (Fully rounded 100px pill with gradient)
    draw_gradient_rect(d2, margin, 1720, width - margin, 1830, corner_radius=100)
    d2.text((width // 2 - 150, 1755), "Simpan Catatan Harian", fill=c_white, font=font_section)

    save_image_safe(im2, os.path.join(raw_dir, "daily-log.png"))
    print("✓ Saved exact cyclejournal_modern_compose_app daily-log.png")

    # =========================================================================
    # 3. SCREEN 3: MEDICAL REPORT SCREEN (with Pro Upgrade Card & FIGO standard)
    # =========================================================================
    im3 = Image.new("RGBA", (width, height), (251, 251, 252, 255))
    d3 = ImageDraw.Draw(im3)
    draw_status_bar(d3)

    # TopAppBar
    d3.text((margin, 80), "<-  Laporan Klinis Siklus & SpOG", fill=c_slate_900, font=font_bold_title)

    # Pro Entitlement Banner Card (Amber Gradient Badge)
    draw_gradient_rect(d3, margin, 150, width - margin, 275, r1=245, g1=158, b1=11, r2=234, g2=88, b2=12, corner_radius=24)
    d3.text((margin + 30, 175), "PRO LIFETIME ACCESS", fill=c_white, font=font_sub_bold)
    d3.text((margin + 30, 215), "Buka ekspor PDF klinis SpOG tanpa batas, bebas iklan selamanya.", fill=(254, 243, 199, 255), font=font_caption)
    d3.rounded_rectangle([width - margin - 200, 185, width - margin - 30, 240], radius=100, fill=c_white)
    d3.text((width - margin - 180, 202), "Beli Rp 49.000", fill=(217, 119, 6, 255), font=font_sub_bold)

    # A4 Paper Mock Card (Rounded 28px)
    d3.rounded_rectangle([margin, 305, width - margin, 1460], radius=28, fill=c_white, outline=(226, 232, 240, 255), width=2)

    # A4 Header
    im3.paste(logo_small, (margin + 30, 335), logo_small)
    d3.text((margin + 100, 335), "LAPORAN KLINIS SIKLUS MENSTRUASI", fill=c_slate_900, font=font_sub_bold)
    d3.text((margin + 100, 370), "Dokumen Rekam Mandiri Standar FIGO untuk Rujukan SpOG", fill=c_slate_600, font=font_caption)
    d3.line([(margin + 30, 415), (width - margin - 30, 415)], fill=c_border, width=2)

    d3.text((margin + 30, 435), "ID Pasien: px-7f9a2b1c4e0d", fill=c_slate_900, font=font_body)
    d3.text((width - margin - 260, 435), "Tanggal: 14 Sep 2026", fill=c_slate_600, font=font_body)

    # Section 1: FIGO Summary
    d3.text((margin + 30, 495), "1. RINGKASAN METRIK FIGO", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 535, width - margin - 30, 640], radius=16, fill=(248, 250, 252, 255), outline=c_border, width=1)
    col_x = (width - margin * 2 - 60) // 4
    d3.text((margin + 50, 555), "Rata-rata Siklus", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50, 590), "28.0 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((margin + 50 + col_x, 555), "Variabilitas (SD)", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50 + col_x, 590), "±1.5 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((margin + 50 + col_x * 2, 555), "Rentang Siklus", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50 + col_x * 2, 590), "26 - 31 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((margin + 50 + col_x * 3, 555), "Durasi Haid", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50 + col_x * 3, 590), "5.0 Hari", fill=c_slate_900, font=font_sub_bold)

    # Section 2: Red Flags
    d3.text((margin + 30, 670), "2. INDIKASI ANOMALI & RED FLAGS KLINIS", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 710, width - margin - 30, 805], radius=16, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d3.text((margin + 50, 730), "[!] ANO_07: Dismenore Berat (Skor VAS >= 7)", fill=(153, 27, 27, 255), font=font_sub_bold)
    d3.text((margin + 50, 765), "Detail: Skor VAS 8/10 terdeteksi pada area pelvis (12 Sep 2026).", fill=(153, 27, 27, 255), font=font_caption)

    # Section 3: Cycle History Table
    d3.text((margin + 30, 835), "3. LOG HISTORIS SIKLUS (6 SIKLUS TERAKHIR)", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 875, width - margin - 30, 920], radius=10, fill=(241, 245, 249, 255))
    d3.text((margin + 50, 890), "TGL AWAL", fill=c_slate_900, font=font_caption)
    d3.text((margin + 240, 890), "TGL AKHIR", fill=c_slate_900, font=font_caption)
    d3.text((margin + 440, 890), "PANJANG", fill=c_slate_900, font=font_caption)
    d3.text((margin + 600, 890), "DURASI", fill=c_slate_900, font=font_caption)
    d3.text((margin + 740, 890), "OVULASI", fill=c_slate_900, font=font_caption)

    table_rows = [
        ("01 Jan 2026", "26 Jan 2026", "26 Hari", "5 Hari", "15 Jan 2026"),
        ("27 Jan 2026", "23 Feb 2026", "28 Hari", "5 Hari", "11 Feb 2026"),
        ("24 Feb 2026", "25 Mar 2026", "30 Hari", "5 Hari", "13 Mar 2026"),
        ("26 Mar 2026", "22 Apr 2026", "28 Hari", "5 Hari", "09 Apr 2026"),
        ("23 Apr 2026", "21 Mei 2026", "29 Hari", "5 Hari", "08 Mei 2026")
    ]
    for idx, (t1, t2, l, d, o) in enumerate(table_rows):
        ry = 930 + idx * 52
        d3.text((margin + 50, ry), t1, fill=c_slate_600, font=font_caption)
        d3.text((margin + 240, ry), t2, fill=c_slate_600, font=font_caption)
        d3.text((margin + 440, ry), l, fill=c_slate_900, font=font_caption)
        d3.text((margin + 600, ry), d, fill=c_slate_600, font=font_caption)
        d3.text((margin + 740, ry), o, fill=c_slate_600, font=font_caption)

    # Section 4: Doctor sign-off
    d3.text((margin + 30, 1205), "4. CATATAN & VERIFIKASI KLINIS DOKTER SpOG", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 1245, width - margin - 30, 1420], radius=16, outline=c_border, width=2)
    d3.text((margin + 50, 1265), "Diagnosa Medis / Rekomendasi Terapi:", fill=c_slate_400, font=font_caption)
    d3.line([(width - margin - 280, 1375), (width - margin - 60, 1375)], fill=c_slate_400, width=1)
    d3.text((width - margin - 250, 1385), "Tanda Tangan & Cap Dokter", fill=c_slate_600, font=font_caption)

    # Export CTA Pill Buttons (Fully rounded 100px)
    draw_gradient_rect(d3, margin, 1500, width - margin, 1600, corner_radius=100)
    d3.text((width // 2 - 150, 1530), "Download PDF Siap SpOG", fill=c_white, font=font_section)

    d3.rounded_rectangle([margin, 1625, width - margin, 1725], radius=100, outline=c_coral, width=3)
    d3.text((width // 2 - 160, 1655), "Ekspor Data CSV (Excel)", fill=c_coral, font=font_section)

    # Floating Bottom Nav Bar
    draw_bottom_nav(d3, active_tab=2)

    save_image_safe(im3, os.path.join(raw_dir, "medical-report.png"))
    print("✓ Saved exact cyclejournal_modern_compose_app medical-report.png")

    # =========================================================================
    # 4. SCREEN 4: PRIVACY & SETTINGS SCREEN (SettingsScreen with Pro Badge & Toggles)
    # =========================================================================
    im4 = Image.new("RGBA", (width, height), (251, 251, 252, 255))
    d4 = ImageDraw.Draw(im4)
    draw_status_bar(d4)

    # TopAppBar
    d4.text((margin, 80), "<-  Pengaturan & Privasi Mutlak", fill=c_slate_900, font=font_bold_title)

    # Card 1: Pro Lifetime Upgrade Card (Amber Gradient)
    draw_gradient_rect(d4, margin, 150, width - margin, 275, r1=245, g1=158, b1=11, r2=234, g2=88, b2=12, corner_radius=24)
    d4.text((margin + 30, 175), "LISENSI SEUMUR HIDUP (PRO)", fill=c_white, font=font_sub_bold)
    d4.text((margin + 30, 215), "Beli putus satu kali: bebas iklan, ekspor PDF tanpa batas.", fill=(254, 243, 199, 255), font=font_caption)
    d4.rounded_rectangle([width - margin - 200, 185, width - margin - 30, 240], radius=100, fill=c_white)
    d4.text((width - margin - 180, 202), "Beli Rp 49.000", fill=(217, 119, 6, 255), font=font_sub_bold)

    # Card 2: Anonymous ID (Rounded 28px card)
    d4.rounded_rectangle([margin, 305, width - margin, 440], radius=28, fill=(255, 248, 248, 255), outline=(255, 228, 230, 255), width=2)
    d4.text((margin + 30, 325), "ID PASIEN ANONIM", fill=c_slate_600, font=font_caption)
    d4.text((margin + 30, 365), "px-7f9a2b1c4e0d", fill=c_pink, font=font_bold_title)
    d4.text((width - margin - 100, 375), "[Salin]", fill=c_pink, font=font_sub_bold)

    # Card 3: Cloudflare D1 Zero-Knowledge (Rounded 28px card)
    d4.text((margin, 475), "Cadangan Cloud Zero-Knowledge", fill=c_slate_900, font=font_section)
    d4.rounded_rectangle([margin, 520, width - margin, 790], radius=28, fill=c_white, outline=c_border, width=2)
    d4.text((margin + 30, 545), "[O] Terkoneksi ke Vault Cloudflare D1", fill=(22, 163, 74, 255), font=font_sub_bold)
    d4.text((margin + 30, 595), "Cadangan terakhir: 12 Sep 2026, 03:15 WIB", fill=c_slate_600, font=font_body)
    d4.text((margin + 30, 638), "Ciphertext terenkripsi penuh dari ponsel sebelum dikirim.", fill=c_slate_400, font=font_caption)

    draw_gradient_rect(d4, margin + 30, 690, width // 2 - 15, 760, corner_radius=100)
    d4.text((margin + 70, 710), "Cadangkan", fill=c_white, font=font_sub_bold)

    d4.rounded_rectangle([width // 2 + 15, 690, width - margin - 30, 760], radius=100, outline=c_border, width=2)
    d4.text((width // 2 + 75, 710), "Pulihkan", fill=c_slate_900, font=font_sub_bold)

    # Card 4: Security & Discreet Toggles (Rounded 28px card)
    d4.text((margin, 825), "Keamanan & Mode Samaran", fill=c_slate_900, font=font_section)
    d4.rounded_rectangle([margin, 870, width - margin, 1220], radius=28, fill=c_white, outline=c_border, width=2)

    # Toggle A: Discreet Mode (Active ON)
    d4.text((margin + 30, 905), "Mode Samaran (Discreet Shield)", fill=c_slate_900, font=font_sub_bold)
    d4.text((margin + 30, 940), "Samarkan istilah sensitif menjadi Fase 01, 02...", fill=c_slate_400, font=font_caption)
    d4.rounded_rectangle([width - margin - 100, 900, width - margin - 30, 940], radius=100, fill=c_pink)
    d4.ellipse([width - margin - 65, 902, width - margin - 33, 938], fill=c_white)

    d4.line([(margin + 30, 980), (width - margin - 30, 980)], fill=c_border, width=1)

    # Toggle B: Biometrics
    d4.text((margin + 30, 1015), "Kunci Sidik Jari (Biometrik)", fill=c_slate_900, font=font_sub_bold)
    d4.rounded_rectangle([width - margin - 100, 1010, width - margin - 30, 1050], radius=100, fill=c_pink)
    d4.ellipse([width - margin - 65, 1012, width - margin - 33, 1048], fill=c_white)

    d4.line([(margin + 30, 1085), (width - margin - 30, 1085)], fill=c_border, width=1)

    # Toggle C: Dark Mode
    d4.text((margin + 30, 1120), "Tema Gelap Subuh (OLED Dark)", fill=c_slate_900, font=font_sub_bold)
    d4.rounded_rectangle([width - margin - 100, 1115, width - margin - 30, 1155], radius=100, fill=(226, 232, 240, 255))
    d4.ellipse([width - margin - 97, 1117, width - margin - 65, 1153], fill=c_white)

    # Card 5: Danger Zone (Nuke - Rounded 28px card)
    d4.text((margin, 1260), "Zona Bahaya (Pemusnahan Data)", fill=(220, 38, 38, 255), font=font_section)
    d4.rounded_rectangle([margin, 1305, width - margin, 1580], radius=28, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d4.text((margin + 30, 1335), "Hak untuk Dilupakan (GDPR & UU PDP):", fill=(153, 27, 27, 255), font=font_sub_bold)
    d4.text((margin + 30, 1380), "Menghapus permanen database SQLCipher lokal, memusnahkan master key di Android Keystore, dan menghapus ciphertext di Cloudflare D1.", fill=(153, 27, 27, 255), font=font_caption)

    d4.rounded_rectangle([margin + 30, 1475, width - margin - 30, 1550], radius=100, fill=(239, 68, 68, 255))
    d4.text((width // 2 - 190, 1495), "HAPUS SEMUA DATA PERMANEN", fill=c_white, font=font_sub_bold)

    # Floating Bottom Nav Bar
    draw_bottom_nav(d4, active_tab=3)

    save_image_safe(im4, os.path.join(raw_dir, "privacy-security.png"))
    print("✓ Saved exact cyclejournal_modern_compose_app privacy-security.png")

    # =========================================================================
    # 5. SCREEN 5: CYCLE SPLASH SCREEN (CycleSplashScreen from user file)
    # =========================================================================
    im5 = Image.new("RGBA", (width, height), (251, 251, 252, 255))
    d5 = ImageDraw.Draw(im5)
    draw_status_bar(d5)

    # Center Glowing Rings
    scx = width // 2
    scy = 520
    # Outer ambient glow ring
    d5.ellipse([scx - 220, scy - 220, scx + 220, scy + 220], outline=(255, 138, 113, 35), width=6)
    d5.ellipse([scx - 170, scy - 170, scx + 170, scy + 170], outline=(255, 94, 125, 65), width=8)

    # Big Pearl Logo Emblem
    logo_big = logo_master.resize((260, 260))
    im5.paste(logo_big, (scx - 130, scy - 130), logo_big)

    # Brand Title in Gradient
    d5.text((scx - 145, 710), "CycleJournal", fill=c_pink, font=font_bold_hero)
    d5.text((scx - 280, 785), "Medical-Grade Cycle Journal & Privacy-First Tracker", fill=c_slate_600, font=font_sub_bold)

    # Badge Pill (Rounded 100px)
    d5.rounded_rectangle([scx - 260, 845, scx + 260, 895], radius=100, fill=(255, 241, 242, 255), outline=(255, 228, 230, 255), width=1)
    d5.text((scx - 230, 860), "100% OFFLINE • ZERO-KNOWLEDGE • STANDAR FIGO", fill=(190, 18, 60, 255), font=font_caption)

    # Clinical Trust Card (Rounded 28px card)
    d5.rounded_rectangle([margin, 955, width - margin, 1360], radius=28, fill=c_white, outline=c_border, width=2)
    d5.text((margin + 30, 990), "Keamanan Medis & Privasi Mutlak", fill=c_slate_900, font=font_section)
    disclaimer_text = (
        "CycleJournal adalah instrumen pencatatan mandiri untuk evaluasi ginekologi (SpOG) "
        "berdasarkan standar klasifikasi siklus FIGO.\n\n"
        "Seluruh catatan biomarker, suhu basal BBT, dan skala nyeri disimpan 100% lokal terenkripsi "
        "hardware SQLCipher di ponsel Anda tanpa akun, tanpa email, dan tanpa pelacak pihak ketiga."
    )
    d5.text((margin + 30, 1045), disclaimer_text, fill=c_slate_600, font=font_body)

    # 4 Trust Features
    d5.text((margin + 30, 1210), "[V] Laporan PDF A4 Siap Dokter SpOG", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 30, 1255), "[V] Aturan 3-over-6 BBT Shift & Lendir Serviks", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 30, 1300), "[V] Skala Nyeri VAS 0-10 & Red Flag Dismenore", fill=c_slate_900, font=font_sub_bold)

    # Primary Action Buttons
    draw_gradient_rect(d5, margin, 1520, width - margin, 1630, corner_radius=100)
    d5.text((width // 2 - 120, 1555), "Mulai Jurnal Saya", fill=c_white, font=font_section)

    d5.rounded_rectangle([margin, 1660, width - margin, 1770], radius=100, fill=c_white, outline=c_coral, width=3)
    d5.text((width // 2 - 130, 1695), "Buka Kunci PIN / Bio", fill=c_coral, font=font_section)

    save_image_safe(im5, os.path.join(raw_dir, "data-portability.png"))
    print("✓ Saved exact cyclejournal_modern_compose_app splash/onboarding screen")

if __name__ == "__main__":
    render_screens()
