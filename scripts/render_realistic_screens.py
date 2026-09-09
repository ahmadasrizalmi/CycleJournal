import os
import json
import math
from PIL import Image, ImageDraw, ImageFont

def render_screens():
    raw_dir = os.path.abspath("out/raw/pixel-10-pro")
    os.makedirs(raw_dir, exist_ok=True)
    width, height = 1080, 1920

    # Fonts
    font_bold_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 44)
    font_section = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 32)
    font_sub_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 25)
    font_body = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 24)
    font_caption = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 21)
    font_small = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 18)
    font_tab = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 18)

    logo_master = Image.open("logo cyclejournal.jpg").convert("RGBA")

    # Colors
    c_white = (255, 255, 255, 255)
    c_slate_900 = (30, 41, 59, 255)
    c_slate_600 = (100, 116, 139, 255)
    c_slate_400 = (148, 163, 184, 255)
    c_border = (241, 245, 249, 255)
    c_coral = (255, 138, 113, 255)
    c_pink = (255, 94, 125, 255)

    margin = 84

    def draw_status_bar(draw):
        draw.text((margin, 30), "09:41", fill=c_slate_900, font=font_caption)
        draw.text((width - margin - 110, 30), "100% 5G", fill=c_slate_900, font=font_caption)

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

    def draw_gradient_rect(draw, x1, y1, x2, y2, r1=255, g1=138, b1=113, r2=255, g2=94, b2=125, corner_radius=32):
        # Draw gradient with rounded pill ends
        h = max(1, y2 - y1)
        mask = Image.new("L", (x2 - x1, h), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.rounded_rectangle([0, 0, x2 - x1, h], radius=corner_radius, fill=255)
        
        grad = Image.new("RGBA", (x2 - x1, h))
        grad_draw = ImageDraw.Draw(grad)
        for y in range(h):
            ratio = y / h
            r = int(r1 + (r2 - r1) * ratio)
            g = int(g1 + (g2 - g1) * ratio)
            b = int(b1 + (b2 - b1) * ratio)
            grad_draw.line([(0, y), (x2 - x1, y)], fill=(r, g, b, 255))
        
        # Composite back
        draw._image.paste(grad, (x1, y1), mask)

    def draw_bottom_nav(draw, img, active_tab=0):
        # Floating pill bottom navigation bar (NeedMCP wf-tabs & wf-fab style)
        bar_w = width - margin * 2
        bar_h = 100
        bar_y = height - 140
        bar_x = margin

        # Background card with rounded 50px pill shape
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
        draw.text((bar_x + 56, bar_y + 58), "Beranda", fill=t1_col, font=font_tab)

        # Tab 2: Kalender (Calendar Icon)
        t2_col = c_pink if active_tab == 1 else c_slate_400
        cx = bar_x + 245
        cy = bar_y + 24
        draw.rectangle([cx - 12, cy, cx + 12, cy + 24], outline=t2_col, width=2)
        draw.line([(cx - 12, cy + 8), (cx + 12, cy + 8)], fill=t2_col, width=2)
        draw.text((bar_x + 215, bar_y + 58), "Kalender", fill=t2_col, font=font_tab)

        # Center Elevated Action Button (wf-fab: 84px circle in gradient with + icon)
        fab_size = 84
        fab_cx = width // 2
        fab_cy = bar_y + bar_h // 2 - 16
        draw_gradient_rect(draw, fab_cx - fab_size // 2, fab_cy - fab_size // 2, fab_cx + fab_size // 2, fab_cy + fab_size // 2, corner_radius=fab_size // 2)
        draw.line([(fab_cx - 14, fab_cy), (fab_cx + 14, fab_cy)], fill=c_white, width=4)
        draw.line([(fab_cx, fab_cy - 14), (fab_cx, fab_cy + 14)], fill=c_white, width=4)

        # Tab 3: Laporan SpOG (Document Icon)
        t3_col = c_pink if active_tab == 2 else c_slate_400
        rx = bar_x + 625
        ry = bar_y + 24
        draw.rectangle([rx - 10, ry, rx + 10, ry + 24], outline=t3_col, width=2)
        draw.line([(rx - 6, ry + 6), (rx + 6, ry + 6)], fill=t3_col, width=2)
        draw.line([(rx - 6, ry + 12), (rx + 6, ry + 12)], fill=t3_col, width=2)
        draw.line([(rx - 6, ry + 18), (rx + 2, ry + 18)], fill=t3_col, width=2)
        draw.text((bar_x + 595, bar_y + 58), "Laporan", fill=t3_col, font=font_tab)

        # Tab 4: Pengaturan (Sliders / Settings Icon)
        t4_col = c_pink if active_tab == 3 else c_slate_400
        sx = bar_x + 795
        sy = bar_y + 24
        draw.line([(sx - 12, sy + 4), (sx + 12, sy + 4)], fill=t4_col, width=2)
        draw.ellipse([sx - 4, sy + 1, sx + 2, sy + 7], fill=t4_col)
        draw.line([(sx - 12, sy + 14), (sx + 12, sy + 14)], fill=t4_col, width=2)
        draw.ellipse([sx + 2, sy + 11, sx + 8, sy + 17], fill=t4_col)
        draw.text((bar_x + 755, bar_y + 58), "Pengaturan", fill=t4_col, font=font_tab)

    # =========================================================================
    # 1. SCREEN 1: DASHBOARD (CycleHomeScreen)
    # =========================================================================
    im1 = Image.new("RGBA", (width, height), c_white)
    d1 = ImageDraw.Draw(im1)
    draw_status_bar(d1)

    # TopAppBar
    logo_small = logo_master.resize((56, 56))
    im1.paste(logo_small, (margin, 82), logo_small)
    d1.text((margin + 70, 86), "CycleJournal", fill=c_slate_900, font=font_bold_title)

    # Hero Card (Gradient with Circular Cycle Progress Meter)
    draw_gradient_rect(d1, margin, 160, width - margin, 520, corner_radius=32)

    # Circular Gauge (NeedMCP Circular Progress Meter) on Right
    gauge_cx = width - margin - 150
    gauge_cy = 340
    gauge_r = 95
    # Gauge background ring
    d1.ellipse([gauge_cx - gauge_r, gauge_cy - gauge_r, gauge_cx + gauge_r, gauge_cy + gauge_r], outline=(255, 255, 255, 70), width=12)
    # Gauge active arc (50% progress for Day 14 of 28)
    d1.arc([gauge_cx - gauge_r, gauge_cy - gauge_r, gauge_cx + gauge_r, gauge_cy + gauge_r], start=-90, end=90, fill=c_white, width=12)
    d1.text((gauge_cx - 40, gauge_cy - 30), "Hari", fill=(255, 241, 242, 230), font=font_caption)
    d1.text((gauge_cx - 30, gauge_cy - 5), "14", fill=c_white, font=font_bold_title)
    d1.text((gauge_cx - 45, gauge_cy + 45), "dari 28", fill=(255, 241, 242, 230), font=font_caption)

    # Hero Left Details
    d1.text((margin + 35, 195), "Fase Folikuler", fill=c_white, font=font_bold_title)
    # Pill
    d1.rounded_rectangle([margin + 35, 265, margin + 245, 312], radius=100, fill=(255, 255, 255, 230))
    d1.text((margin + 55, 275), "JENDELA SUBUR", fill=(190, 18, 60, 255), font=font_caption)
    d1.text((margin + 35, 345), "Ovulasi dalam 2 hari", fill=(255, 241, 242, 255), font=font_sub_bold)
    d1.text((margin + 35, 395), "Akurasi Berstandar Medis FIGO", fill=(255, 241, 242, 220), font=font_caption)
    d1.text((margin + 35, 435), "Panjang Siklus: 28 Hari (±1.5 Hari)", fill=(255, 241, 242, 220), font=font_caption)

    # Month Navigator
    d1.text((margin + 20, 560), "<", fill=c_slate_600, font=font_section)
    d1.text((width // 2 - 110, 560), "September 2026", fill=c_slate_900, font=font_section)
    d1.text((width - margin - 35, 560), ">", fill=c_slate_600, font=font_section)

    # Day headers
    days = ["Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"]
    col_w = (width - margin * 2) // 7
    for i, name in enumerate(days):
        x = margin + i * col_w + (col_w - 36) // 2
        d1.text((x, 625), name, fill=c_slate_600, font=font_caption)

    # Calendar Grid Cells (Realistic FIGO Shading)
    dates = [
        [1, 2, 3, 4, 5, 6, 7],
        [8, 9, 10, 11, 12, 13, 14],
        [15, 16, 17, 18, 19, 20, 21],
        [22, 23, 24, 25, 26, 27, 28],
        [29, 30, 0, 0, 0, 0, 0]
    ]

    for row_idx, week in enumerate(dates):
        y = 690 + row_idx * 110
        for col_idx, day_num in enumerate(week):
            if day_num == 0: continue
            cx = margin + col_idx * col_w + col_w // 2
            cy = y + 35

            if day_num in [8, 9, 10, 11, 12]:
                d1.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], fill=(255, 228, 230, 255))
                d1.text((cx - 15, cy - 18), str(day_num), fill=(190, 18, 60, 255), font=font_sub_bold)
            elif day_num in [17, 18, 19, 21]:
                d1.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], fill=(207, 250, 254, 255))
                d1.text((cx - 15, cy - 18), str(day_num), fill=(14, 116, 144, 255), font=font_sub_bold)
            elif day_num == 20:
                d1.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], fill=(207, 250, 254, 255), outline=(14, 116, 144, 255), width=3)
                d1.ellipse([cx - 5, cy + 22, cx + 5, cy + 32], fill=(6, 182, 212, 255))
                d1.text((cx - 15, cy - 18), str(day_num), fill=(14, 116, 144, 255), font=font_sub_bold)
            elif day_num == 14:
                d1.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], outline=c_coral, width=4)
                d1.text((cx - 15, cy - 18), str(day_num), fill=c_slate_900, font=font_sub_bold)
            else:
                d1.text((cx - 15, cy - 18), str(day_num), fill=c_slate_900, font=font_sub_bold)

    # Legend
    leg_y = 1270
    d1.ellipse([margin + 20, leg_y, margin + 45, leg_y + 25], fill=(255, 228, 230, 255))
    d1.text((margin + 55, leg_y), "Menstruasi", fill=c_slate_600, font=font_caption)

    d1.ellipse([width // 2 - 80, leg_y, width // 2 - 55, leg_y + 25], fill=(207, 250, 254, 255))
    d1.text((width // 2 - 45, leg_y), "Masa Subur", fill=c_slate_600, font=font_caption)

    d1.ellipse([width - margin - 150, leg_y, width - margin - 125, leg_y + 25], outline=(14, 116, 144, 255), width=2)
    d1.ellipse([width - margin - 140, leg_y + 10, width - margin - 134, leg_y + 16], fill=(6, 182, 212, 255))
    d1.text((width - margin - 115, leg_y), "Ovulasi", fill=c_slate_600, font=font_caption)

    # Symptom Preview Card (Rounded 28px card)
    d1.rounded_rectangle([margin, 1330, width - margin, 1680], radius=28, fill=(248, 250, 252, 255), outline=c_border, width=2)
    d1.text((margin + 35, 1360), "Catatan Hari Ini (14 Sep 2026)", fill=c_slate_900, font=font_section)
    d1.text((margin + 35, 1420), "• Aliran Haid: Tidak ada perdarahan", fill=c_slate_600, font=font_body)
    d1.text((margin + 35, 1470), "• Suhu Basal (BBT): 36.45 °C (Baseline Shift)", fill=c_slate_600, font=font_body)
    d1.text((margin + 35, 1520), "• Lendir Serviks: Putih Telur (Peak Fertility)", fill=(14, 116, 144, 255), font=font_body)
    d1.text((margin + 35, 1570), "• Skala Nyeri: 0 / 10 (Bebas nyeri)", fill=c_slate_600, font=font_body)

    # Draw Floating Bottom Nav Bar
    draw_bottom_nav(d1, im1, active_tab=0)

    save_image_safe(im1, os.path.join(raw_dir, "dashboard.png"))
    print("Saved modern NeedMCP-styled dashboard.png")

    # =========================================================================
    # 2. SCREEN 2: DAILY LOG INPUT SHEET (DailyLogInputSheet)
    # =========================================================================
    im2 = Image.new("RGBA", (width, height), (250, 250, 250, 255))
    d2 = ImageDraw.Draw(im2)
    draw_status_bar(d2)

    # Top dimmer area
    d2.rectangle([0, 0, width, 180], fill=(0, 0, 0, 30))

    # Bottom Sheet Surface with 40px rounded top corners
    d2.rounded_rectangle([0, 180, width, height], radius=40, fill=c_white)
    # Sheet handle
    d2.rounded_rectangle([width // 2 - 40, 205, width // 2 + 40, 215], radius=10, fill=(226, 232, 240, 255))

    d2.text((margin, 240), "Log Harian Biomarker", fill=c_slate_900, font=font_bold_title)
    d2.text((margin, 300), "Senin, 14 September 2026", fill=c_slate_600, font=font_body)
    d2.text((width - margin - 40, 250), "[X]", fill=c_slate_400, font=font_section)

    d2.line([(margin, 360), (width - margin, 360)], fill=c_border, width=2)

    # Section 1: Flow (Pill chips with 100px radius)
    d2.text((margin, 390), "Pendarahan Haid (Flow)", fill=c_slate_900, font=font_section)
    flow_chips = [("Tidak", False), ("Bercak", False), ("Ringan", False), ("Sedang", True), ("Deras", False)]
    chip_w = (width - margin * 2 - 40) // 5
    for i, (label, active) in enumerate(flow_chips):
        cx1 = margin + i * (chip_w + 10)
        cx2 = cx1 + chip_w
        if active:
            draw_gradient_rect(d2, cx1, 445, cx2, 515, corner_radius=100)
            d2.text((cx1 + (chip_w - 60) // 2, 468), label, fill=c_white, font=font_sub_bold)
        else:
            d2.rounded_rectangle([cx1, 445, cx2, 515], radius=100, fill=(248, 250, 252, 255), outline=c_border, width=2)
            d2.text((cx1 + (chip_w - 60) // 2, 468), label, fill=c_slate_900, font=font_sub_bold)

    # Section 2: Pain VAS (7 / 10)
    d2.text((margin, 560), "Skala Nyeri Klinis (VAS)", fill=c_slate_900, font=font_section)
    d2.text((width - margin - 90, 560), "7 / 10", fill=(220, 38, 38, 255), font=font_section)

    # Slider Track
    d2.rounded_rectangle([margin, 630, width - margin, 646], radius=10, fill=(241, 245, 249, 255))
    active_slider_w = int((width - margin * 2) * 0.7)
    d2.rounded_rectangle([margin, 630, margin + active_slider_w, 646], radius=10, fill=(220, 38, 38, 255))
    d2.ellipse([margin + active_slider_w - 18, 638 - 18, margin + active_slider_w + 18, 638 + 18], fill=(220, 38, 38, 255))

    d2.text((margin, 670), "7–10: Nyeri hebat, membatasi gerak atau butuh tirah baring.", fill=(153, 27, 27, 255), font=font_body)

    # Red Flag Warning Card (Rounded 20px)
    d2.rounded_rectangle([margin, 725, width - margin, 845], radius=20, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d2.text((margin + 25, 745), "[!] Peringatan Anomali Medis:", fill=(153, 27, 27, 255), font=font_sub_bold)
    d2.text((margin + 25, 785), "Nyeri level tinggi terdeteksi. Ini akan ditandai pada laporan SpOG sebagai indikasi dismenore berat/endometriosis.", fill=(153, 27, 27, 255), font=font_caption)

    # Section 3: BBT & Analgesic (Rounded 20px)
    d2.rounded_rectangle([margin, 880, width // 2 - 15, 990], radius=20, fill=c_white, outline=c_border, width=2)
    d2.text((margin + 25, 900), "Suhu Basal (°C)", fill=c_slate_600, font=font_caption)
    d2.text((margin + 25, 935), "36.50 °C", fill=c_slate_900, font=font_section)

    d2.rounded_rectangle([width // 2 + 15, 880, width - margin, 990], radius=20, fill=(248, 250, 252, 255), outline=c_border, width=2)
    d2.text((width // 2 + 35, 905), "[V] Analgesik", fill=c_slate_900, font=font_section)
    d2.text((width // 2 + 35, 945), "Konsumsi pereda nyeri", fill=c_slate_600, font=font_caption)

    # Section 4: Cervical Mucus (Pill chips with 100px radius)
    d2.text((margin, 1030), "Karakteristik Lendir Serviks", fill=c_slate_900, font=font_section)
    mucus_chips = [("Kering", False), ("Krim", False), ("Cair", False), ("Putih Telur", True)]
    m_chip_w = (width - margin * 2 - 30) // 4
    for i, (label, active) in enumerate(mucus_chips):
        cx1 = margin + i * (m_chip_w + 10)
        cx2 = cx1 + m_chip_w
        if active:
            d2.rounded_rectangle([cx1, 1085, cx2, 1155], radius=100, fill=(207, 250, 254, 255), outline=(14, 116, 144, 255), width=2)
            d2.text((cx1 + (m_chip_w - 90) // 2, 1108), label, fill=(14, 116, 144, 255), font=font_sub_bold)
        else:
            d2.rounded_rectangle([cx1, 1085, cx2, 1155], radius=100, fill=(248, 250, 252, 255), outline=c_border, width=2)
            d2.text((cx1 + (m_chip_w - 70) // 2, 1108), label, fill=c_slate_900, font=font_sub_bold)

    # Section 5: Notes
    d2.text((margin, 1200), "Catatan Tambahan", fill=c_slate_900, font=font_section)
    d2.rounded_rectangle([margin, 1250, width - margin, 1420], radius=20, fill=c_white, outline=c_border, width=2)
    d2.text((margin + 25, 1280), "Nyeri pelvis terasa sejak pagi, konsumsi paracetamol 500mg...", fill=c_slate_900, font=font_body)

    # Save Pill Button (Fully rounded 100px pill)
    draw_gradient_rect(d2, margin, 1720, width - margin, 1830, corner_radius=100)
    d2.text((width // 2 - 150, 1755), "Simpan Catatan Harian", fill=c_white, font=font_section)

    save_image_safe(im2, os.path.join(raw_dir, "daily-log.png"))
    print("Saved modern NeedMCP-styled daily-log.png")

    # =========================================================================
    # 3. SCREEN 3: MEDICAL REPORT SCREEN (ReportScreen)
    # =========================================================================
    im3 = Image.new("RGBA", (width, height), c_white)
    d3 = ImageDraw.Draw(im3)
    draw_status_bar(d3)

    # TopAppBar
    d3.text((margin, 80), "<-  Laporan Klinis Siklus & SpOG", fill=c_slate_900, font=font_bold_title)

    # A4 Paper Mock Card (Rounded 28px)
    d3.rounded_rectangle([margin, 160, width - margin, 1480], radius=28, fill=c_white, outline=(226, 232, 240, 255), width=2)

    # A4 Header
    im3.paste(logo_small, (margin + 30, 190), logo_small)
    d3.text((margin + 100, 190), "LAPORAN KLINIS SIKLUS MENSTRUASI", fill=c_slate_900, font=font_sub_bold)
    d3.text((margin + 100, 225), "Dokumen Rekam Mandiri Standar FIGO untuk Rujukan SpOG", fill=c_slate_600, font=font_caption)
    d3.line([(margin + 30, 270), (width - margin - 30, 270)], fill=c_border, width=2)

    d3.text((margin + 30, 290), "ID Pasien: px-7f9a2b1c4e0d", fill=c_slate_900, font=font_body)
    d3.text((width - margin - 260, 290), "Tanggal: 14 Sep 2026", fill=c_slate_600, font=font_body)

    # Section 1: FIGO Summary
    d3.text((margin + 30, 360), "1. RINGKASAN METRIK FIGO", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 410, width - margin - 30, 520], radius=16, fill=(248, 250, 252, 255), outline=c_border, width=1)
    col_x = (width - margin * 2 - 60) // 4
    d3.text((margin + 50, 430), "Rata-rata Siklus", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50, 465), "28.0 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((margin + 50 + col_x, 430), "Variabilitas (SD)", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50 + col_x, 465), "±1.5 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((margin + 50 + col_x * 2, 430), "Rentang Siklus", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50 + col_x * 2, 465), "26 - 31 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((margin + 50 + col_x * 3, 430), "Durasi Haid", fill=c_slate_600, font=font_caption)
    d3.text((margin + 50 + col_x * 3, 465), "5.0 Hari", fill=c_slate_900, font=font_sub_bold)

    # Section 2: Red Flags
    d3.text((margin + 30, 560), "2. INDIKASI ANOMALI & RED FLAGS KLINIS", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 610, width - margin - 30, 710], radius=16, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d3.text((margin + 50, 635), "[!] ANO_07: Dismenore Berat (Skor VAS >= 7)", fill=(153, 27, 27, 255), font=font_sub_bold)
    d3.text((margin + 50, 670), "Detail: Skor VAS 8/10 terdeteksi pada area pelvis (12 Sep 2026).", fill=(153, 27, 27, 255), font=font_caption)

    # Section 3: Cycle History Table
    d3.text((margin + 30, 750), "3. LOG HISTORIS SIKLUS (6 SIKLUS TERAKHIR)", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 800, width - margin - 30, 850], radius=10, fill=(241, 245, 249, 255))
    d3.text((margin + 50, 815), "TGL AWAL", fill=c_slate_900, font=font_caption)
    d3.text((margin + 240, 815), "TGL AKHIR", fill=c_slate_900, font=font_caption)
    d3.text((margin + 440, 815), "PANJANG", fill=c_slate_900, font=font_caption)
    d3.text((margin + 600, 815), "DURASI", fill=c_slate_900, font=font_caption)
    d3.text((margin + 740, 815), "OVULASI", fill=c_slate_900, font=font_caption)

    table_rows = [
        ("01 Jan 2026", "26 Jan 2026", "26 Hari", "5 Hari", "15 Jan 2026"),
        ("27 Jan 2026", "23 Feb 2026", "28 Hari", "5 Hari", "11 Feb 2026"),
        ("24 Feb 2026", "25 Mar 2026", "30 Hari", "5 Hari", "13 Mar 2026"),
        ("26 Mar 2026", "22 Apr 2026", "28 Hari", "5 Hari", "09 Apr 2026"),
        ("23 Apr 2026", "21 Mei 2026", "29 Hari", "5 Hari", "08 Mei 2026")
    ]
    for idx, (t1, t2, l, d, o) in enumerate(table_rows):
        ry = 860 + idx * 55
        d3.text((margin + 50, ry), t1, fill=c_slate_600, font=font_caption)
        d3.text((margin + 240, ry), t2, fill=c_slate_600, font=font_caption)
        d3.text((margin + 440, ry), l, fill=c_slate_900, font=font_caption)
        d3.text((margin + 600, ry), d, fill=c_slate_600, font=font_caption)
        d3.text((margin + 740, ry), o, fill=c_slate_600, font=font_caption)

    # Section 4: Doctor sign-off
    d3.text((margin + 30, 1170), "4. CATATAN & VERIFIKASI KLINIS DOKTER SpOG", fill=c_slate_900, font=font_sub_bold)
    d3.rounded_rectangle([margin + 30, 1220, width - margin - 30, 1420], radius=16, outline=c_border, width=2)
    d3.text((margin + 50, 1240), "Diagnosa Medis / Rekomendasi Terapi:", fill=c_slate_400, font=font_caption)
    d3.line([(width - margin - 280, 1370), (width - margin - 60, 1370)], fill=c_slate_400, width=1)
    d3.text((width - margin - 250, 1380), "Tanda Tangan & Cap Dokter", fill=c_slate_600, font=font_caption)

    # Export CTA Pill Buttons (Fully rounded 100px)
    draw_gradient_rect(d3, margin, 1540, width - margin, 1640, corner_radius=100)
    d3.text((width // 2 - 140, 1570), "Ekspor PDF Siap SpOG", fill=c_white, font=font_section)

    d3.rounded_rectangle([margin, 1670, width - margin, 1770], radius=100, outline=c_coral, width=3)
    d3.text((width // 2 - 160, 1700), "Ekspor Data CSV (Excel)", fill=c_coral, font=font_section)

    save_image_safe(im3, os.path.join(raw_dir, "medical-report.png"))
    print("Saved modern NeedMCP-styled medical-report.png")

    # =========================================================================
    # 4. SCREEN 4: PRIVACY & SETTINGS SCREEN (SettingsScreen)
    # =========================================================================
    im4 = Image.new("RGBA", (width, height), c_white)
    d4 = ImageDraw.Draw(im4)
    draw_status_bar(d4)

    # TopAppBar
    d4.text((margin, 80), "<-  Pengaturan & Privasi Mutlak", fill=c_slate_900, font=font_bold_title)

    # Card 1: Anonymous ID (Rounded 28px card)
    d4.rounded_rectangle([margin, 160, width - margin, 310], radius=28, fill=(255, 248, 248, 255), outline=(255, 228, 230, 255), width=2)
    d4.text((margin + 30, 185), "ID PASIEN ANONIM", fill=c_slate_600, font=font_caption)
    d4.text((margin + 30, 225), "px-7f9a2b1c4e0d", fill=c_pink, font=font_bold_title)
    d4.text((width - margin - 100, 235), "[Salin]", fill=c_pink, font=font_sub_bold)

    # Card 2: Cloudflare D1 Zero-Knowledge (Rounded 28px card)
    d4.text((margin, 350), "Cadangan Cloud Zero-Knowledge", fill=c_slate_900, font=font_section)
    d4.rounded_rectangle([margin, 400, width - margin, 680], radius=28, fill=c_white, outline=c_border, width=2)
    d4.text((margin + 30, 430), "[O] Terkoneksi ke Vault Cloudflare D1", fill=(22, 163, 74, 255), font=font_sub_bold)
    d4.text((margin + 30, 480), "Cadangan terakhir: 12 Sep 2026, 03:15 WIB", fill=c_slate_600, font=font_body)
    d4.text((margin + 30, 525), "Ciphertext terenkripsi penuh dari ponsel sebelum dikirim.", fill=c_slate_400, font=font_caption)

    draw_gradient_rect(d4, margin + 30, 580, width // 2 - 15, 650, corner_radius=100)
    d4.text((margin + 70, 600), "Cadangkan", fill=c_white, font=font_sub_bold)

    d4.rounded_rectangle([width // 2 + 15, 580, width - margin - 30, 650], radius=100, outline=c_border, width=2)
    d4.text((width // 2 + 75, 600), "Pulihkan", fill=c_slate_900, font=font_sub_bold)

    # Card 3: Security (Rounded 28px card)
    d4.text((margin, 720), "Keamanan & Autentikasi", fill=c_slate_900, font=font_section)
    d4.rounded_rectangle([margin, 770, width - margin, 1050], radius=28, fill=c_white, outline=c_border, width=2)

    d4.text((margin + 30, 810), "Kunci Sidik Jari (Biometrik)", fill=c_slate_900, font=font_sub_bold)
    d4.rounded_rectangle([width - margin - 100, 805, width - margin - 30, 845], radius=100, fill=c_pink)
    d4.ellipse([width - margin - 65, 807, width - margin - 33, 843], fill=c_white)

    d4.line([(margin + 30, 880), (width - margin - 30, 880)], fill=c_border, width=1)
    d4.text((margin + 30, 920), "Ubah PIN 4-Digit", fill=c_slate_900, font=font_sub_bold)
    d4.text((width - margin - 60, 920), ">", fill=c_slate_400, font=font_sub_bold)

    d4.line([(margin + 30, 970), (width - margin - 30, 970)], fill=c_border, width=1)
    d4.text((margin + 30, 1000), "Kunci Otomatis (30 Detik di Background)", fill=c_slate_600, font=font_body)

    # Card 4: Danger Zone (Nuke - Rounded 28px card)
    d4.text((margin, 1090), "Zona Bahaya (Pemusnahan Data)", fill=(220, 38, 38, 255), font=font_section)
    d4.rounded_rectangle([margin, 1140, width - margin, 1450], radius=28, fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d4.text((margin + 30, 1170), "Hak untuk Dilupakan (GDPR & UU PDP):", fill=(153, 27, 27, 255), font=font_sub_bold)
    d4.text((margin + 30, 1220), "Menghapus permanen database SQLCipher lokal, memusnahkan master key di Android Keystore, dan menghapus ciphertext di Cloudflare D1.", fill=(153, 27, 27, 255), font=font_caption)

    d4.rounded_rectangle([margin + 30, 1330, width - margin - 30, 1415], radius=100, fill=(239, 68, 68, 255))
    d4.text((width // 2 - 190, 1355), "HAPUS SEMUA DATA PERMANEN", fill=c_white, font=font_sub_bold)

    # Draw Floating Bottom Nav Bar
    draw_bottom_nav(d4, im4, active_tab=3)

    save_image_safe(im4, os.path.join(raw_dir, "privacy-security.png"))
    print("Saved modern NeedMCP-styled privacy-security.png")

    # =========================================================================
    # 5. SCREEN 5: ONBOARDING & WELCOME (OnboardingScreen)
    # =========================================================================
    im5 = Image.new("RGBA", (width, height), c_white)
    d5 = ImageDraw.Draw(im5)
    draw_status_bar(d5)

    # Big Logo Emblem
    logo_big = logo_master.resize((240, 240))
    im5.paste(logo_big, (width // 2 - 120, 160), logo_big)

    d5.text((width // 2 - 130, 430), "CycleJournal", fill=c_pink, font=font_bold_title)
    d5.text((width // 2 - 260, 500), "Medical-Grade Cycle Journal & Privacy Tracker", fill=c_slate_600, font=font_sub_bold)

    # Badge pill (Rounded 100px)
    d5.rounded_rectangle([width // 2 - 250, 560, width // 2 + 250, 610], radius=100, fill=(255, 241, 242, 255))
    d5.text((width // 2 - 220, 575), "100% OFFLINE • ZERO-KNOWLEDGE • STANDAR FIGO", fill=(190, 18, 60, 255), font=font_caption)

    # Disclaimer Card (Rounded 28px card)
    d5.rounded_rectangle([margin, 660, width - margin, 980], radius=28, fill=(248, 250, 252, 255), outline=c_border, width=2)
    d5.text((margin + 30, 690), "Penafian Medis & Privasi Mutlak", fill=c_slate_900, font=font_section)
    disclaimer_text = (
        "CycleJournal adalah instrumen pencatatan mandiri untuk evaluasi klinis "
        "ginekologi (SpOG) dan bukan pengganti diagnosis medis dokter spesialis atau kontrasepsi.\n\n"
        "Semua data kesehatan Anda tersimpan 100% lokal terenkripsi hardware SQLCipher "
        "tanpa akun, tanpa email, dan tanpa pelacak pihak ketiga."
    )
    d5.text((margin + 30, 750), disclaimer_text, fill=c_slate_600, font=font_body)

    # PIN Setup Visualization
    d5.text((width // 2 - 110, 1050), "Setup PIN 4-Digit", fill=c_slate_900, font=font_section)
    for i in range(4):
        cx = width // 2 - 75 + i * 50
        cy = 1130
        if i < 2:
            d5.ellipse([cx - 15, cy - 15, cx + 15, cy + 15], fill=c_pink)
        else:
            d5.ellipse([cx - 15, cy - 15, cx + 15, cy + 15], fill=(226, 232, 240, 255))

    # Features summary
    d5.text((margin + 30, 1240), "[V] Laporan PDF A4 Siap Dokter SpOG", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 30, 1310), "[V] Aturan 3-over-6 BBT Shift & Lendir Serviks", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 30, 1380), "[V] Skala Nyeri VAS 0-10 & Red Flag Dismenore", fill=c_slate_900, font=font_sub_bold)
    d5.text((margin + 30, 1450), "[V] Cadangan Cloud Terenkripsi AES-256-GCM", fill=c_slate_900, font=font_sub_bold)

    # Final Button (Rounded 100px pill)
    draw_gradient_rect(d5, margin, 1720, width - margin, 1830, corner_radius=100)
    d5.text((width // 2 - 190, 1755), "Mulai Menggunakan CycleJournal", fill=c_white, font=font_section)

    save_image_safe(im5, os.path.join(raw_dir, "data-portability.png"))
    print("Saved modern NeedMCP-styled data-portability.png (onboarding)")

if __name__ == "__main__":
    render_screens()
