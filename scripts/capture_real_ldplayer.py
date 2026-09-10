import subprocess
import time
import os
import struct
import json
from PIL import Image

DEVICE = "emulator-5554"
OUT_DIR = os.path.abspath("out/raw/pixel-10-pro")
os.makedirs(OUT_DIR, exist_ok=True)

def adb(cmd):
    full_cmd = f"adb -s {DEVICE} {cmd}"
    res = subprocess.run(full_cmd, shell=True, capture_output=True, text=True)
    return res

def capture_screen(target_filename):
    remote_raw = "/sdcard/screen_cap.raw"
    local_raw = os.path.join(OUT_DIR, "temp.raw")
    target_png = os.path.join(OUT_DIR, target_filename)

    adb(f"shell rm -f {remote_raw}")
    res = adb(f"shell screencap {remote_raw}")
    if res.returncode != 0:
        print(f"Error executing screencap: {res.stderr}")
    
    adb(f"pull {remote_raw} \"{local_raw}\"")

    with open(local_raw, "rb") as f:
        header = f.read(16)
        w, h, fmt, colorspace = struct.unpack("<IIII", header)
        data = f.read()

    im = Image.frombytes("RGBA", (w, h), data)
    im.save(target_png, "PNG")
    if os.path.exists(local_raw):
        os.remove(local_raw)
    print(f"✓ Captured: {target_filename} ({w}x{h})")
    return target_png

def main():
    print("=== Starting Real App Capture for All 5 Scenes on LDPlayer ===")

    # Ensure app is active in foreground
    adb("shell am start -n com.app.cyclejournal/.MainActivity")
    time.sleep(2)

    # 1. SCENE 1: DASHBOARD (Title: Pelacak Siklus Berstandar Medis)
    print("1. Capturing Scene 1: Dashboard (Beranda)...")
    adb("shell input tap 130 2744")
    time.sleep(1.5)
    capture_screen("dashboard.png")

    # 2. SCENE 2: DAILY LOG (Title: Jurnal Gejala & Skala Nyeri Klinis)
    print("2. Capturing Scene 2: Daily Log Sheet (FAB +)...")
    adb("shell input tap 640 2700")
    time.sleep(2.0)
    capture_screen("daily-log.png")
    # Dismiss bottom sheet
    adb("shell input keyevent 4")
    time.sleep(1.5)

    # 3. SCENE 3: MEDICAL REPORT (Title: Laporan Medis Siap Dokter SpOG)
    print("3. Capturing Scene 3: Medical Report (Laporan)...")
    adb("shell input tap 895 2744")
    time.sleep(1.5)
    capture_screen("medical-report.png")

    # 4. SCENE 4: PRIVACY & SECURITY (Title: Privasi Mutlak Zero-Knowledge)
    print("4. Capturing Scene 4: Privacy & Security (Pengaturan Top)...")
    adb("shell input tap 1150 2744")
    time.sleep(1.5)
    capture_screen("privacy-security.png")

    # 5. SCENE 5: DATA PORTABILITY (Title: Kedaulatan Penuh Atas Data Anda)
    print("5. Capturing Scene 5: Data Portability & Danger Zone (Pengaturan Scrolled)...")
    adb("shell input swipe 640 2000 640 600 400")
    time.sleep(1.5)
    capture_screen("data-portability.png")

    # Write manifest.json for Goldie
    manifest = {
        "device": "pixel-10-pro",
        "screenshots": [
            {
                "sceneId": "dashboard",
                "file": os.path.abspath(os.path.join(OUT_DIR, "dashboard.png")).replace("\\", "/")
            },
            {
                "sceneId": "daily-log",
                "file": os.path.abspath(os.path.join(OUT_DIR, "daily-log.png")).replace("\\", "/")
            },
            {
                "sceneId": "medical-report",
                "file": os.path.abspath(os.path.join(OUT_DIR, "medical-report.png")).replace("\\", "/")
            },
            {
                "sceneId": "privacy-security",
                "file": os.path.abspath(os.path.join(OUT_DIR, "privacy-security.png")).replace("\\", "/")
            },
            {
                "sceneId": "data-portability",
                "file": os.path.abspath(os.path.join(OUT_DIR, "data-portability.png")).replace("\\", "/")
            }
        ]
    }

    manifest_path = os.path.join(OUT_DIR, "manifest.json")
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)
    print(f"✓ Saved manifest.json to {manifest_path}")

if __name__ == "__main__":
    main()
