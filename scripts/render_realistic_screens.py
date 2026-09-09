import os
import json
from PIL import Image, ImageDraw, ImageFont

def render_screens():
    raw_dir = "out/raw/pixel-10-pro"
    os.makedirs(raw_dir, exist_ok=True)
    width, height = 1080, 1920

    # Fonts
    font_bold_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 46)
    font_section = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 34)
    font_sub_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 26)
    font_body = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 26)
    font_caption = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 22)
    font_small = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 19)

    logo_master = Image.open("logo cyclejournal.jpg").convert("RGBA")

    # Colors
    c_white = (255, 255, 255, 255)
    c_slate_900 = (30, 41, 59, 255)
    c_slate_600 = (100, 116, 139, 255)
    c_slate_400 = (148, 163, 184, 255)
    c_border = (241, 245, 249, 255)
    c_coral = (255, 138, 113, 255)
    c_pink = (255, 94, 125, 255)

    def draw_status_bar(draw):
        draw.text((64, 26), "09:41", fill=c_slate_900, font=font_caption)
        draw.text((width - 170, 26), "100% 5G", fill=c_slate_900, font=font_caption)

    def draw_gradient_rect(draw, x1, y1, x2, y2, r1=255, g1=138, b1=113, r2=255, g2=94, b2=125):
        h = max(1, y2 - y1)
        for y in range(y1, y2):
            ratio = (y - y1) / h
            r = int(r1 + (r2 - r1) * ratio)
            g = int(g1 + (g2 - g1) * ratio)
            b = int(b1 + (b2 - b1) * ratio)
            draw.line([(x1, y), (x2, y)], fill=(r, g, b, 255))

    # =========================================================================
    # 1. SCREEN 1: DASHBOARD (CycleHomeScreen)
    # =========================================================================
    im1 = Image.new("RGBA", (width, height), c_white)
    d1 = ImageDraw.Draw(im1)
    draw_status_bar(d1)

    # TopAppBar
    logo_small = logo_master.resize((60, 60))
    im1.paste(logo_small, (50, 75), logo_small)
    d1.text((126, 80), "CycleJournal", fill=c_slate_900, font=font_bold_title)
    d1.text((width - 150, 92), "[PDF]  [*]", fill=c_pink, font=font_sub_bold)

    # Hero Card (Gradient)
    draw_gradient_rect(d1, 50, 160, width - 50, 480)
    d1.text((90, 195), "Hari ke-14 Siklus", fill=c_white, font=font_bold_title)
    # Pill
    d1.rectangle([90, 270, 290, 316], fill=(255, 255, 255, 60))
    d1.text((115, 278), "JENDELA SUBUR", fill=c_white, font=font_caption)
    d1.text((90, 345), "Perkiraan Ovulasi dalam 2 hari (Akurasi FIGO)", fill=(255, 241, 242, 255), font=font_body)
    d1.text((90, 395), "Panjang Siklus Rata-rata: 28 Hari (±1.5 Hari)", fill=(255, 241, 242, 220), font=font_caption)

    # Month Navigator
    d1.text((50, 520), "<", fill=c_slate_600, font=font_section)
    d1.text((width // 2 - 120, 520), "September 2026", fill=c_slate_900, font=font_section)
    d1.text((width - 70, 520), ">", fill=c_slate_600, font=font_section)

    # Day headers
    days = ["Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"]
    col_w = (width - 100) // 7
    for i, name in enumerate(days):
        x = 50 + i * col_w + 30
        d1.text((x, 590), name, fill=c_slate_600, font=font_caption)

    # Calendar Grid Cells (Realistic FIGO Shading)
    dates = [
        [1, 2, 3, 4, 5, 6, 7],
        [8, 9, 10, 11, 12, 13, 14],
        [15, 16, 17, 18, 19, 20, 21],
        [22, 23, 24, 25, 26, 27, 28],
        [29, 30, 0, 0, 0, 0, 0]
    ]

    for row_idx, week in enumerate(dates):
        y = 660 + row_idx * 115
        for col_idx, day_num in enumerate(week):
            if day_num == 0: continue
            cx = 50 + col_idx * col_w + col_w // 2
            cy = y + 35

            # Menstruation (Days 8-12)
            if day_num in [8, 9, 10, 11, 12]:
                d1.ellipse([cx - 40, cy - 40, cx + 40, cy + 40], fill=(255, 228, 230, 255))
                d1.text((cx - 16, cy - 20), str(day_num), fill=(190, 18, 60, 255), font=font_sub_bold)
            # Fertile Window (Days 17-21)
            elif day_num in [17, 18, 19, 21]:
                d1.ellipse([cx - 40, cy - 40, cx + 40, cy + 40], fill=(207, 250, 254, 255))
                d1.text((cx - 16, cy - 20), str(day_num), fill=(14, 116, 144, 255), font=font_sub_bold)
            # Ovulation Day (Day 20)
            elif day_num == 20:
                d1.ellipse([cx - 40, cy - 40, cx + 40, cy + 40], fill=(207, 250, 254, 255), outline=(14, 116, 144, 255), width=3)
                d1.ellipse([cx - 6, cy + 24, cx + 6, cy + 36], fill=(6, 182, 212, 255))
                d1.text((cx - 16, cy - 20), str(day_num), fill=(14, 116, 144, 255), font=font_sub_bold)
            # Today / Selected (Day 14)
            elif day_num == 14:
                d1.ellipse([cx - 40, cy - 40, cx + 40, cy + 40], outline=c_coral, width=4)
                d1.text((cx - 16, cy - 20), str(day_num), fill=c_slate_900, font=font_sub_bold)
            else:
                d1.text((cx - 16, cy - 20), str(day_num), fill=c_slate_900, font=font_sub_bold)

    # Legend
    leg_y = 1270
    d1.ellipse([90, leg_y, 115, leg_y + 25], fill=(255, 228, 230, 255))
    d1.text((125, leg_y), "Menstruasi", fill=c_slate_600, font=font_caption)

    d1.ellipse([430, leg_y, 455, leg_y + 25], fill=(207, 250, 254, 255))
    d1.text((465, leg_y), "Masa Subur", fill=c_slate_600, font=font_caption)

    d1.ellipse([780, leg_y, 805, leg_y + 25], outline=(14, 116, 144, 255), width=2)
    d1.ellipse([790, leg_y + 10, 796, leg_y + 16], fill=(6, 182, 212, 255))
    d1.text((815, leg_y), "Ovulasi", fill=c_slate_600, font=font_caption)

    # Symptom Preview Card
    d1.rectangle([50, 1340, width - 50, 1660], fill=(248, 250, 252, 255), outline=c_border, width=2)
    d1.text((90, 1375), "Catatan Hari Ini (14 Sep 2026)", fill=c_slate_900, font=font_section)
    d1.text((90, 1435), "• Aliran Haid: Tidak ada perdarahan", fill=c_slate_600, font=font_body)
    d1.text((90, 1485), "• Suhu Basal (BBT): 36.45 °C (Baseline Shift)", fill=c_slate_600, font=font_body)
    d1.text((90, 1535), "• Lendir Serviks: Putih Telur (Peak Fertility)", fill=(14, 116, 144, 255), font=font_body)
    d1.text((90, 1585), "• Skala Nyeri: 0 / 10 (Bebas nyeri)", fill=c_slate_600, font=font_body)

    # Bottom Sticky Action Button
    draw_gradient_rect(d1, 60, 1720, width - 60, 1830)
    d1.text((360, 1755), "Catat Gejala Hari Ini", fill=c_white, font=font_section)
    im1.save(os.path.join(raw_dir, "dashboard.png"), "PNG")
    print("Saved high-res dashboard.png")

    # =========================================================================
    # 2. SCREEN 2: DAILY LOG INPUT SHEET (DailyLogInputSheet)
    # =========================================================================
    im2 = Image.new("RGBA", (width, height), (250, 250, 250, 255))
    d2 = ImageDraw.Draw(im2)
    draw_status_bar(d2)

    # Top dimmer area
    d2.rectangle([0, 0, width, 180], fill=(0, 0, 0, 30))

    # Bottom Sheet Surface
    d2.rectangle([0, 180, width, height], fill=c_white)
    # Sheet handle
    d2.rectangle([width // 2 - 40, 205, width // 2 + 40, 215], fill=(226, 232, 240, 255))

    d2.text((60, 240), "Log Harian Biomarker", fill=c_slate_900, font=font_bold_title)
    d2.text((60, 300), "Senin, 14 September 2026", fill=c_slate_600, font=font_body)
    d2.text((width - 100, 250), "[X]", fill=c_slate_400, font=font_section)

    d2.line([(60, 360), (width - 60, 360)], fill=c_border, width=2)

    # Section 1: Flow
    d2.text((60, 390), "Pendarahan Haid (Flow)", fill=c_slate_900, font=font_section)
    flow_chips = [("Tidak", False), ("Bercak", False), ("Ringan", False), ("Sedang", True), ("Deras", False)]
    chip_w = (width - 120 - 40) // 5
    for i, (label, active) in enumerate(flow_chips):
        cx1 = 60 + i * (chip_w + 10)
        cx2 = cx1 + chip_w
        if active:
            d2.rectangle([cx1, 445, cx2, 520], fill=c_pink)
            d2.text((cx1 + 25, 470), label, fill=c_white, font=font_sub_bold)
        else:
            d2.rectangle([cx1, 445, cx2, 520], fill=(248, 250, 252, 255), outline=c_border, width=2)
            d2.text((cx1 + 25, 470), label, fill=c_slate_900, font=font_sub_bold)

    # Section 2: Pain VAS (7 / 10)
    d2.text((60, 560), "Skala Nyeri Klinis (VAS)", fill=c_slate_900, font=font_section)
    d2.text((width - 160, 560), "7 / 10", fill=(220, 38, 38, 255), font=font_section)

    # Slider Track
    d2.rectangle([60, 630, width - 60, 646], fill=(241, 245, 249, 255))
    active_slider_w = int((width - 120) * 0.7)
    d2.rectangle([60, 630, 60 + active_slider_w, 646], fill=(220, 38, 38, 255))
    d2.ellipse([60 + active_slider_w - 18, 638 - 18, 60 + active_slider_w + 18, 638 + 18], fill=(220, 38, 38, 255))

    d2.text((60, 670), "7–10: Nyeri hebat, membatasi gerak atau butuh tirah baring.", fill=(153, 27, 27, 255), font=font_body)

    # Red Flag Warning Card
    d2.rectangle([60, 725, width - 60, 845], fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d2.text((90, 745), "[!] Peringatan Anomali Medis:", fill=(153, 27, 27, 255), font=font_sub_bold)
    d2.text((90, 785), "Nyeri level tinggi terdeteksi. Ini akan ditandai pada laporan SpOG sebagai indikasi dismenore berat/endometriosis.", fill=(153, 27, 27, 255), font=font_caption)

    # Section 3: BBT & Analgesic
    d2.rectangle([60, 880, width // 2 - 20, 990], fill=c_white, outline=c_border, width=2)
    d2.text((90, 900), "Suhu Basal (°C)", fill=c_slate_600, font=font_caption)
    d2.text((90, 935), "36.50 °C", fill=c_slate_900, font=font_section)

    d2.rectangle([width // 2 + 10, 880, width - 60, 990], fill=(248, 250, 252, 255), outline=c_border, width=2)
    d2.text((width // 2 + 40, 905), "[V] Analgesik", fill=c_slate_900, font=font_section)
    d2.text((width // 2 + 40, 945), "Konsumsi pereda nyeri", fill=c_slate_600, font=font_caption)

    # Section 4: Cervical Mucus
    d2.text((60, 1030), "Karakteristik Lendir Serviks", fill=c_slate_900, font=font_section)
    mucus_chips = [("Kering", False), ("Krim", False), ("Cair", False), ("Putih Telur", True)]
    m_chip_w = (width - 120 - 30) // 4
    for i, (label, active) in enumerate(mucus_chips):
        cx1 = 60 + i * (m_chip_w + 10)
        cx2 = cx1 + m_chip_w
        if active:
            d2.rectangle([cx1, 1085, cx2, 1160], fill=(207, 250, 254, 255), outline=(14, 116, 144, 255), width=2)
            d2.text((cx1 + 25, 1110), label, fill=(14, 116, 144, 255), font=font_sub_bold)
        else:
            d2.rectangle([cx1, 1085, cx2, 1160], fill=(248, 250, 252, 255), outline=c_border, width=2)
            d2.text((cx1 + 35, 1110), label, fill=c_slate_900, font=font_sub_bold)

    # Section 5: Notes
    d2.text((60, 1200), "Catatan Tambahan", fill=c_slate_900, font=font_section)
    d2.rectangle([60, 1250, width - 60, 1420], fill=c_white, outline=c_border, width=2)
    d2.text((90, 1280), "Nyeri pelvis terasa sejak pagi, konsumsi paracetamol 500mg...", fill=c_slate_900, font=font_body)

    # Save Button
    draw_gradient_rect(d2, 60, 1720, width - 60, 1830)
    d2.text((360, 1755), "Simpan Catatan Harian", fill=c_white, font=font_section)

    im2.save(os.path.join(raw_dir, "daily-log.png"), "PNG")
    print("Saved high-res daily-log.png")

    # =========================================================================
    # 3. SCREEN 3: MEDICAL REPORT SCREEN (ReportScreen)
    # =========================================================================
    im3 = Image.new("RGBA", (width, height), c_white)
    d3 = ImageDraw.Draw(im3)
    draw_status_bar(d3)

    # TopAppBar
    d3.text((60, 80), "<-  Laporan Klinis Siklus & SpOG", fill=c_slate_900, font=font_bold_title)

    # A4 Paper Mock Card
    d3.rectangle([50, 160, width - 50, 1480], fill=c_white, outline=(226, 232, 240, 255), width=2)

    # A4 Header
    im3.paste(logo_small, (80, 190), logo_small)
    d3.text((160, 190), "LAPORAN KLINIS SIKLUS MENSTRUASI", fill=c_slate_900, font=font_sub_bold)
    d3.text((160, 225), "Dokumen Rekam Mandiri Standar FIGO untuk Rujukan SpOG", fill=c_slate_600, font=font_caption)
    d3.line([(80, 270), (width - 80, 270)], fill=c_border, width=2)

    d3.text((80, 290), "ID Pasien: px-7f9a2b1c4e0d", fill=c_slate_900, font=font_body)
    d3.text((width - 340, 290), "Tanggal: 14 Sep 2026", fill=c_slate_600, font=font_body)

    # Section 1: FIGO Summary
    d3.text((80, 360), "1. RINGKASAN METRIK FIGO", fill=c_slate_900, font=font_sub_bold)
    d3.rectangle([80, 410, width - 80, 520], fill=(248, 250, 252, 255), outline=c_border, width=1)
    col_x = (width - 160) // 4
    d3.text((100, 430), "Rata-rata Siklus", fill=c_slate_600, font=font_caption)
    d3.text((100, 465), "28.0 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((100 + col_x, 430), "Variabilitas (SD)", fill=c_slate_600, font=font_caption)
    d3.text((100 + col_x, 465), "±1.5 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((100 + col_x * 2, 430), "Rentang Siklus", fill=c_slate_600, font=font_caption)
    d3.text((100 + col_x * 2, 465), "26 - 31 Hari", fill=c_slate_900, font=font_sub_bold)

    d3.text((100 + col_x * 3, 430), "Durasi Haid", fill=c_slate_600, font=font_caption)
    d3.text((100 + col_x * 3, 465), "5.0 Hari", fill=c_slate_900, font=font_sub_bold)

    # Section 2: Red Flags
    d3.text((80, 560), "2. INDIKASI ANOMALI & RED FLAGS KLINIS", fill=c_slate_900, font=font_sub_bold)
    d3.rectangle([80, 610, width - 80, 710], fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d3.text((110, 635), "[!] ANO_07: Dismenore Berat (Skor VAS >= 7)", fill=(153, 27, 27, 255), font=font_sub_bold)
    d3.text((110, 670), "Detail: Skor VAS 8/10 terdeteksi pada area pelvis (12 Sep 2026).", fill=(153, 27, 27, 255), font=font_caption)

    # Section 3: Cycle History Table
    d3.text((80, 750), "3. LOG HISTORIS SIKLUS (6 SIKLUS TERAKHIR)", fill=c_slate_900, font=font_sub_bold)
    d3.rectangle([80, 800, width - 80, 850], fill=(241, 245, 249, 255))
    d3.text((100, 815), "TGL AWAL", fill=c_slate_900, font=font_caption)
    d3.text((320, 815), "TGL AKHIR", fill=c_slate_900, font=font_caption)
    d3.text((540, 815), "PANJANG", fill=c_slate_900, font=font_caption)
    d3.text((720, 815), "DURASI", fill=c_slate_900, font=font_caption)
    d3.text((860, 815), "OVULASI", fill=c_slate_900, font=font_caption)

    table_rows = [
        ("01 Jan 2026", "26 Jan 2026", "26 Hari", "5 Hari", "15 Jan 2026"),
        ("27 Jan 2026", "23 Feb 2026", "28 Hari", "5 Hari", "11 Feb 2026"),
        ("24 Feb 2026", "25 Mar 2026", "30 Hari", "5 Hari", "13 Mar 2026"),
        ("26 Mar 2026", "22 Apr 2026", "28 Hari", "5 Hari", "09 Apr 2026"),
        ("23 Apr 2026", "21 Mei 2026", "29 Hari", "5 Hari", "08 Mei 2026")
    ]
    for idx, (t1, t2, l, d, o) in enumerate(table_rows):
        ry = 860 + idx * 55
        d3.text((100, ry), t1, fill=c_slate_600, font=font_caption)
        d3.text((320, ry), t2, fill=c_slate_600, font=font_caption)
        d3.text((540, ry), l, fill=c_slate_900, font=font_caption)
        d3.text((720, ry), d, fill=c_slate_600, font=font_caption)
        d3.text((860, ry), o, fill=c_slate_600, font=font_caption)

    # Section 4: Doctor sign-off
    d3.text((80, 1170), "4. CATATAN & VERIFIKASI KLINIS DOKTER SpOG", fill=c_slate_900, font=font_sub_bold)
    d3.rectangle([80, 1220, width - 80, 1420], outline=c_border, width=2)
    d3.text((100, 1240), "Diagnosa Medis / Rekomendasi Terapi:", fill=c_slate_400, font=font_caption)
    d3.line([(width - 360, 1370), (width - 120, 1370)], fill=c_slate_400, width=1)
    d3.text((width - 320, 1380), "Tanda Tangan & Cap Dokter", fill=c_slate_600, font=font_caption)

    # Export CTA Buttons
    draw_gradient_rect(d3, 60, 1540, width - 60, 1650)
    d3.text((360, 1575), "Ekspor PDF Siap SpOG", fill=c_white, font=font_section)

    d3.rectangle([60, 1680, width - 60, 1790], outline=c_coral, width=3)
    d3.text((360, 1715), "Ekspor Data CSV (Excel)", fill=c_coral, font=font_section)

    im3.save(os.path.join(raw_dir, "medical-report.png"), "PNG")
    print("Saved high-res medical-report.png")

    # =========================================================================
    # 4. SCREEN 4: PRIVACY & SETTINGS SCREEN (SettingsScreen)
    # =========================================================================
    im4 = Image.new("RGBA", (width, height), c_white)
    d4 = ImageDraw.Draw(im4)
    draw_status_bar(d4)

    # TopAppBar
    d4.text((60, 80), "<-  Pengaturan & Privasi Mutlak", fill=c_slate_900, font=font_bold_title)

    # Card 1: Anonymous ID
    d4.rectangle([50, 160, width - 50, 310], fill=(255, 248, 248, 255), outline=(255, 228, 230, 255), width=2)
    d4.text((80, 185), "ID PASIEN ANONIM", fill=c_slate_600, font=font_caption)
    d4.text((80, 225), "px-7f9a2b1c4e0d", fill=c_pink, font=font_bold_title)
    d4.text((width - 140, 235), "[Salin]", fill=c_pink, font=font_sub_bold)

    # Card 2: Cloudflare D1 Zero-Knowledge
    d4.text((50, 350), "Cadangan Cloud Zero-Knowledge", fill=c_slate_900, font=font_section)
    d4.rectangle([50, 400, width - 50, 680], fill=c_white, outline=c_border, width=2)
    d4.text((80, 430), "[O] Terkoneksi ke Vault Cloudflare D1", fill=(22, 163, 74, 255), font=font_sub_bold)
    d4.text((80, 480), "Cadangan terakhir: 12 Sep 2026, 03:15 WIB", fill=c_slate_600, font=font_body)
    d4.text((80, 525), "Ciphertext terenkripsi penuh dari ponsel sebelum dikirim.", fill=c_slate_400, font=font_caption)

    draw_gradient_rect(d4, 80, 580, width // 2 - 20, 650)
    d4.text((120, 600), "Cadangkan", fill=c_white, font=font_sub_bold)

    d4.rectangle([width // 2 + 20, 580, width - 80, 650], outline=c_border, width=2)
    d4.text((width // 2 + 80, 600), "Pulihkan", fill=c_slate_900, font=font_sub_bold)

    # Card 3: Security
    d4.text((50, 720), "Keamanan & Autentikasi", fill=c_slate_900, font=font_section)
    d4.rectangle([50, 770, width - 50, 1050], fill=c_white, outline=c_border, width=2)

    d4.text((80, 810), "Kunci Sidik Jari (Biometrik)", fill=c_slate_900, font=font_sub_bold)
    d4.rectangle([width - 160, 805, width - 90, 845], fill=c_pink)
    d4.ellipse([width - 125, 807, width - 93, 843], fill=c_white)

    d4.line([(80, 880), (width - 80, 880)], fill=c_border, width=1)
    d4.text((80, 920), "Ubah PIN 4-Digit", fill=c_slate_900, font=font_sub_bold)
    d4.text((width - 120, 920), ">", fill=c_slate_400, font=font_sub_bold)

    d4.line([(80, 970), (width - 80, 970)], fill=c_border, width=1)
    d4.text((80, 1000), "Kunci Otomatis (30 Detik di Background)", fill=c_slate_600, font=font_body)

    # Card 4: Danger Zone (Nuke)
    d4.text((50, 1090), "Zona Bahaya (Pemusnahan Data)", fill=(220, 38, 38, 255), font=font_section)
    d4.rectangle([50, 1140, width - 50, 1450], fill=(254, 242, 242, 255), outline=(239, 68, 68, 255), width=2)
    d4.text((80, 1170), "Hak untuk Dilupakan (GDPR & UU PDP):", fill=(153, 27, 27, 255), font=font_sub_bold)
    d4.text((80, 1220), "Menghapus permanen database SQLCipher lokal, memusnahkan master key di Android Keystore, dan menghapus ciphertext di Cloudflare D1.", fill=(153, 27, 27, 255), font=font_caption)

    d4.rectangle([80, 1330, width - 80, 1420], fill=(239, 68, 68, 255))
    d4.text((280, 1355), "HAPUS SEMUA DATA PERMANEN", fill=c_white, font=font_sub_bold)

    im4.save(os.path.join(raw_dir, "privacy-security.png"), "PNG")
    print("Saved high-res privacy-security.png")

    # =========================================================================
    # 5. SCREEN 5: ONBOARDING & WELCOME (OnboardingScreen)
    # =========================================================================
    im5 = Image.new("RGBA", (width, height), c_white)
    d5 = ImageDraw.Draw(im5)
    draw_status_bar(d5)

    # Big Logo Emblem
    logo_big = logo_master.resize((240, 240))
    im5.paste(logo_big, (width // 2 - 120, 160), logo_big)

    d5.text((width // 2 - 140, 430), "CycleJournal", fill=c_pink, font=font_bold_title)
    d5.text((width // 2 - 270, 500), "Medical-Grade Cycle Journal & Privacy Tracker", fill=c_slate_600, font=font_sub_bold)

    # Badge pill
    d5.rectangle([width // 2 - 260, 560, width // 2 + 260, 610], fill=(255, 241, 242, 255))
    d5.text((width // 2 - 230, 575), "100% OFFLINE • ZERO-KNOWLEDGE • STANDAR FIGO", fill=(190, 18, 60, 255), font=font_caption)

    # Disclaimer Card
    d5.rectangle([60, 660, width - 60, 980], fill=(248, 250, 252, 255), outline=c_border, width=2)
    d5.text((90, 690), "Penafian Medis & Privasi Mutlak", fill=c_slate_900, font=font_section)
    disclaimer_text = (
        "CycleJournal adalah instrumen pencatatan mandiri untuk evaluasi klinis "
        "ginekologi (SpOG) dan bukan pengganti diagnosis medis dokter spesialis atau kontrasepsi.\n\n"
        "Semua data kesehatan Anda tersimpan 100% lokal terenkripsi hardware SQLCipher "
        "tanpa akun, tanpa email, dan tanpa pelacak pihak ketiga."
    )
    d5.text((90, 750), disclaimer_text, fill=c_slate_600, font=font_body)

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
    d5.text((90, 1240), "[V] Laporan PDF A4 Siap Dokter SpOG", fill=c_slate_900, font=font_sub_bold)
    d5.text((90, 1310), "[V] Aturan 3-over-6 BBT Shift & Lendir Serviks", fill=c_slate_900, font=font_sub_bold)
    d5.text((90, 1380), "[V] Skala Nyeri VAS 0-10 & Red Flag Dismenore", fill=c_slate_900, font=font_sub_bold)
    d5.text((90, 1450), "[V] Cadangan Cloud Terenkripsi AES-256-GCM", fill=c_slate_900, font=font_sub_bold)

    # Final Button
    draw_gradient_rect(d5, 60, 1720, width - 60, 1830)
    d5.text((320, 1755), "Mulai Menggunakan CycleJournal", fill=c_white, font=font_section)

    im5.save(os.path.join(raw_dir, "data-portability.png"), "PNG")
    print("Saved high-res data-portability.png (onboarding)")

render_screens()
