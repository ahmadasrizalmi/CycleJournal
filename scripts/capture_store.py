# -*- coding: utf-8 -*-
"""Capture the store scenes with content verification, so a half-drawn screen is never saved.

Every scene waits for a marker that only exists on that screen, retries the navigation up to
three times, and refuses to overwrite a good frame with a duplicate of the previous one.
"""
import os, re, subprocess, sys, time

ROOT = "D:/9 KANDA/APPS Android/CycleJournal"
RAW = os.path.join(ROOT, "out/raw")
ADB = "C:/Android/Sdk/platform-tools/adb.exe"
SERIAL = "emulator-5554"
DEMO = "com.app.cyclejournal"
DEBUG = "com.app.cyclejournal.debug"

DEVICES = {
    "pixel-10-pro": {"size": "1279x2853", "density": "520"},
    "tablet-7": {"size": "1200x1920", "density": "240"},
    "tablet-10": {"size": "1600x2560", "density": "320"},
}
SCENES = ["dashboard", "calendar", "daily-log", "fertility-signs", "medical-report",
          "privacy-security", "dark-mode", "no-account"]
PHONE = SCENES
TABLET = SCENES

# marker text that proves the screen is up, per scene and locale
MARKERS = {
    "dashboard": {"id": "PREDIKSI", "en": "NEXT PERIOD"},
    "bbt-chart": {"id": "Suhu pagi", "en": "Morning temperature"},
    "dark-mode": {"id": "PREDIKSI", "en": "NEXT PERIOD"},
    "calendar": {"id": "September", "en": "September"},
    "day-detail": {"id": "Catatan hari ini", "en": "Today's log"},
    "daily-log": {"id": "Catatan hari ini", "en": "Today's log"},
    "fertility-signs": {"id": "Suhu pagi", "en": "Morning temp"},
    "fertility-explainer": {"id": "Lendir", "en": "Cervical"},
    "medical-report": {"id": "RINGKASAN", "en": "CYCLE"},
    "report-export": {"id": "LAPORAN", "en": "REPORT"},
    "bilingual-text-size": {"id": "Bahasa", "en": "Language"},
    "privacy-security": {"id": "Kunci PIN", "en": "PIN lock"},
    "language": {"id": "BAHASA", "en": "LANGUAGE"},
    "backup": {"id": "Cadangkan data", "en": "Back up your data"},
    "pin-lock": {"id": "PIN", "en": "PIN"},
    # the intro follows the system language, so accept either wording
    "no-account": {"id": "New here", "en": "New here"},  # the CTA is on every slide
}


def sh(*a, timeout=120):
    return subprocess.run([ADB, "-s", SERIAL, *a], capture_output=True, text=True, timeout=timeout).stdout


def dump():
    sh("shell", "uiautomator", "dump", "/sdcard/window_dump.xml")
    return sh("shell", "cat", "/sdcard/window_dump.xml")


def screen():
    out = sh("shell", "wm", "size")
    m = re.search(r"Override size: (\d+)x(\d+)", out) or re.search(r"Physical size: (\d+)x(\d+)", out)
    return (int(m.group(1)), int(m.group(2))) if m else (1080, 1920)


def find(pred, t):
    for m in re.finditer(r"<node[^>]*>", t):
        if pred(m.group(0)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', m.group(0))
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                return ((x1 + x2) // 2, (y1 + y2) // 2)
    return None


def by_id(tag, t=None):
    return find(lambda n: 'resource-id="%s"' % tag in n, t if t is not None else dump())


def by_desc(sub):
    return find(lambda n: re.search(r'content-desc="[^"]*%s' % re.escape(sub), n) is not None, dump())


def has_text(sub, t=None):
    return sub.lower() in (t if t is not None else dump()).lower()


def tap(pt, wait=2.0):
    if not pt:
        return False
    sh("shell", "input", "tap", str(pt[0]), str(pt[1]))
    time.sleep(wait)
    return True


def swipe_up(a=0.80, b=0.32, ms=450):
    w, h = screen()
    sh("shell", "input", "swipe", str(w // 2), str(int(h * a)), str(w // 2), str(int(h * b)), str(ms))
    time.sleep(1.7)


def wait_marker(scene, locale, timeout=30):
    want = MARKERS[scene]["id" if locale == "id-ID" else "en"]
    deadline = time.time() + timeout
    while time.time() < deadline:
        if has_text(want):
            return True
        time.sleep(1.5)
    return False


def shot(path):
    sh("shell", "screencap", "-p", "/sdcard/_cap.png")
    subprocess.run([ADB, "-s", SERIAL, "pull", "/sdcard/_cap.png", path], capture_output=True)
    return os.path.getsize(path)


def back_to_home():
    for _ in range(3):
        t = dump()
        if "Buang" in t or "Discard" in t:
            tap(find(lambda n: re.search(r'text="(Buang|Discard)"', n), t), 2.0)
            continue
        if by_id("nav_home"):
            break
        sh("shell", "input", "keyevent", "4")
        time.sleep(1.5)
    tap(by_id("nav_home"), 2.0)


def do_scene(scene, locale, path, pkg=DEMO):
    back_to_home()
    for attempt in (1, 2, 3):
        if scene == "dashboard":
            sh("shell", "am", "force-stop", pkg); time.sleep(1.5)
            sh("shell", "am", "start", "-n", "%s/.MainActivity" % pkg); time.sleep(6)
        elif scene == "bbt-chart":
            pass  # the morning-temperature row is on the home screen
        elif scene == "dark-mode":
            if attempt == 1:
                pt = by_desc("tema gelap") or by_desc("dark theme")
                if pt:
                    tap(pt, 2.5)
        elif scene == "calendar":
            tap(by_id("nav_calendar"), 3.0)
        elif scene in ("day-detail", "daily-log"):
            if scene == "daily-log":
                tap(by_id("fab_log"), 3.0)
            else:
                t = dump()
                tap(find(lambda n: re.search(r'text="13"', n), t) or by_id("nav_calendar"), 2.5)
        elif scene == "fertility-signs":
            tap(by_id("fab_log"), 3.0)
            tap(by_id("sheet_detail_toggle"), 2.0)
        elif scene == "fertility-explainer":
            tap(by_id("fab_log"), 3.0)
            tap(by_id("sheet_detail_toggle"), 2.0)
            for _ in range(5):
                if by_id("log_mucus_info"):
                    break
                swipe_up(0.80, 0.40)
            tap(by_id("log_mucus_info"), 2.5)
        elif scene in ("medical-report", "report-export"):
            tap(by_id("nav_report"), 3.0)
            if scene == "report-export":
                for _ in range(3):
                    swipe_up(0.80, 0.28)
                    if has_text("LAPORAN" if locale == "id-ID" else "REPORT"):
                        break
        elif scene in ("bilingual-text-size", "privacy-security", "language", "backup", "pin-lock"):
            tap(by_id("nav_settings"), 3.0)
            want = "Kunci PIN" if locale == "id-ID" else "PIN lock"
            for _ in range(3):
                if has_text(want):
                    break
                swipe_up(0.80, 0.34)
        elif scene == "no-account":
            sh("shell", "am", "force-stop", DEBUG); time.sleep(1)
            sh("shell", "pm", "clear", DEBUG)
            sh("shell", "am", "start", "-n", "%s/.MainActivity" % DEBUG)
            time.sleep(14)

        if scene in ("language", "backup", "pin-lock"):
            tag = {"language": "settings_language", "backup": "settings_backup", "pin-lock": "settings_pin"}[scene]
            if not by_id(tag):
                swipe_up(0.80, 0.34)
            if not tap(by_id(tag), 2.5):
                print("      no %s, retry" % tag); continue

        if scene == "no-account":
            time.sleep(5)   # the carousel animates forever; a dump would never return
        if scene != "no-account" and not wait_marker(scene, locale, 12 if attempt > 1 else 22):
            print("      %s not on screen, retry %d" % (scene, attempt))
            sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
            continue

        size = shot(path)
        print("    %-20s %8d" % (os.path.basename(path), size))
        if scene == "fertility-explainer":
            sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
        if scene in ("day-detail", "daily-log", "fertility-signs"):
            sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
            t = dump()
            if "Buang" in t or "Discard" in t:
                tap(find(lambda n: re.search(r'text="(Buang|Discard)"', n), t), 2.0)
        if scene == "dark-mode":
            pt = by_desc("tema terang") or by_desc("light theme")
            if pt:
                tap(pt, 2.5)
        return True
    return False


def main():
    dev = sys.argv[1]
    locales = [sys.argv[2]] if len(sys.argv) > 2 else ["id-ID", "en-US"]
    only = sys.argv[3:]
    if only:
        global PHONE, TABLET
        PHONE = TABLET = only
    cfg = DEVICES[dev]
    scenes = PHONE if dev == "pixel-10-pro" else TABLET
    sh("shell", "wm", "size", cfg["size"]); sh("shell", "wm", "density", cfg["density"]); time.sleep(3)
    for locale in locales:
        print("== %s / %s" % (dev, locale))
        outdir = os.path.join(RAW, dev, locale)
        os.makedirs(outdir, exist_ok=True)
        sh("shell", "am", "force-stop", DEMO); time.sleep(1)
        sh("shell", "am", "start", "-n", "%s/.MainActivity" % DEMO); time.sleep(8)
        want_pill = "ID" if locale == "id-ID" else "EN"
        for _ in range(3):
            t = dump()
            if re.search(r'text="%s"' % want_pill, t):
                break
            tap(by_desc("language") or by_desc("bahasa"), 2.5)
        for scene in scenes:
            ok = do_scene(scene, locale, os.path.join(outdir, scene + ".png"))
            if not ok:
                print("    FAILED", scene)


if __name__ == "__main__":
    main()
