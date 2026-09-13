# -*- coding: utf-8 -*-
"""Capture every store scene from the local emulator at each store form factor.

goldie's own capture step replays stale iOS simulator frames on this machine, so the raw frames
come from the emulator and goldie only composes the design. Run:  python scripts/capture_all_devices.py
"""
import os, re, subprocess, sys, time

ROOT = "D:/9 KANDA/APPS Android/CycleJournal"
RAW = os.path.join(ROOT, "out/raw")
ADB = "C:/Android/Sdk/platform-tools/adb.exe"
SERIAL = "emulator-5554"

DEVICES = {
    "pixel-10-pro": {"size": "1080x1920", "density": "420"},
    "tablet-7": {"size": "1200x1920", "density": "240"},
    "tablet-10": {"size": "1600x2560", "density": "320"},
}
# The phone markets every page; the tablets carry the eight screens that read best wide.
PHONE_SCENES = ["dashboard", "bbt-chart", "dark-mode", "calendar", "day-detail", "daily-log",
                "fertility-signs", "fertility-explainer", "medical-report", "report-export",
                "bilingual-text-size", "privacy-security", "language", "backup", "pin-lock", "no-account"]
TABLET_SCENES = ["dashboard", "calendar", "daily-log", "fertility-signs", "medical-report",
                 "privacy-security", "dark-mode", "no-account"]
DEMO = "com.app.cyclejournal"
DEBUG = "com.app.cyclejournal.debug"


def sh(*args, timeout=120):
    return subprocess.run([ADB, "-s", SERIAL, *args], capture_output=True, text=True, timeout=timeout).stdout


def dump():
    sh("shell", "uiautomator", "dump", "/sdcard/window_dump.xml")
    return sh("shell", "cat", "/sdcard/window_dump.xml")


def find(pred, t=None):
    t = t if t is not None else dump()
    for m in re.finditer(r"<node[^>]*>", t):
        node = m.group(0)
        if pred(node):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                return ((x1 + x2) // 2, (y1 + y2) // 2)
    return None


def by_id(tag, t=None):
    return find(lambda n: 'resource-id="%s"' % tag in n, t)


def by_text(sub, t=None):
    return find(lambda n: re.search(r'text="[^"]*%s' % re.escape(sub), n) is not None, t)


def by_desc(sub, t=None):
    return find(lambda n: re.search(r'content-desc="[^"]*%s' % re.escape(sub), n) is not None, t)


def tap(pt, wait=2.0):
    if not pt:
        return False
    sh("shell", "input", "tap", str(pt[0]), str(pt[1]))
    time.sleep(wait)
    return True


def screen_size():
    m = re.search(r"Override size: (\d+)x(\d+)", sh("shell", "wm", "size")) or \
        re.search(r"Physical size: (\d+)x(\d+)", sh("shell", "wm", "size"))
    if m:
        return int(m.group(1)), int(m.group(2))
    return 1080, 1920


def swipe_up(fraction_from=0.78, fraction_to=0.30, ms=450):
    w, h = screen_size()
    x = w // 2
    sh("shell", "input", "swipe", str(x), str(int(h * fraction_from)), str(x), str(int(h * fraction_to)), str(ms))
    time.sleep(1.8)


def capture(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    sh("shell", "screencap", "-p", "/sdcard/_cap.png")
    subprocess.run([ADB, "-s", SERIAL, "pull", "/sdcard/_cap.png", path], capture_output=True)
    size = os.path.getsize(path)
    prev = capture.prev_size if hasattr(capture, "prev_size") else -1
    flag = "  <-- same bytes as the previous scene, check the navigation" if size == prev else ""
    capture.prev_size = size
    print("   ", os.path.basename(path), size, flag)


def set_locale(locale, pkg):
    """The app follows the system language until the user picks one; the header button toggles."""
    pill = "ID" if locale == "id-ID" else "EN"
    for _ in range(3):
        t = dump()
        if any(re.search(r'text="%s"' % pill, n) for n in re.findall(r"<node[^>]*>", t)):
            return True
        if not tap(by_desc("language") or by_desc("bahasa"), 2.5):
            return False
    return False


def capture_set(pkg, scenes, outdir, locale, seed_home=True):
    for scene in scenes:
        path = os.path.join(outdir, scene + ".png")
        if scene == "dashboard":
            if seed_home:
                sh("shell", "am", "force-stop", pkg); time.sleep(1.5)
                sh("shell", "am", "start", "-n", "%s/.MainActivity" % pkg); time.sleep(11)
            capture(path)
        elif scene == "bbt-chart":
            # hero carousel: the third card is the temperature card
            sh("shell", "input", "swipe", "700", "700", "200", "700", "300"); time.sleep(1.2)
            sh("shell", "input", "swipe", "700", "700", "200", "700", "300"); time.sleep(1.5)
            capture(path)
            sh("shell", "input", "swipe", "200", "700", "700", "700", "300"); time.sleep(1.0)
            sh("shell", "input", "swipe", "200", "700", "700", "700", "300"); time.sleep(1.2)
        elif scene == "dark-mode":
            tap(by_desc("tema gelap") or by_desc("dark theme"), 2.5)
            capture(path)
            tap(by_desc("tema terang") or by_desc("light theme"), 2.5)
        elif scene == "calendar":
            tap(by_id("nav_calendar"), 3.0); capture(path)
        elif scene == "day-detail":
            t = dump()
            day = by_text("13", t) or by_text("14", t)
            tap(day, 2.5); capture(path)
            sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
        elif scene == "daily-log":
            tap(by_id("fab_log"), 3.0); capture(path)
        elif scene == "fertility-signs":
            tap(by_id("sheet_detail_toggle"), 2.0); capture(path)
        elif scene == "fertility-explainer":
            for _ in range(5):
                if by_id("log_mucus_info"):
                    break
                swipe_up(0.80, 0.40)
            if tap(by_id("log_mucus_info"), 2.5):
                capture(path)
                sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
            sh("shell", "input", "keyevent", "4"); time.sleep(2)
            if by_text("Buang") or by_text("Discard"):
                tap(by_text("Buang") or by_text("Discard"), 2.5)
        elif scene == "medical-report":
            tap(by_id("nav_report"), 3.0); capture(path)
        elif scene == "report-export":
            swipe_up(0.78, 0.30)
            capture(path)
        elif scene == "bilingual-text-size":
            tap(by_id("nav_settings"), 3.0); capture(path)
        elif scene == "privacy-security":
            swipe_up(0.78, 0.40)
            capture(path)
        elif scene == "language":
            if not by_id("settings_language"):
                swipe_up(0.78, 0.35)
            if tap(by_id("settings_language"), 2.5):
                capture(path)
                sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
        elif scene == "backup":
            if tap(by_id("settings_backup"), 2.5):
                capture(path)
                sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
        elif scene == "pin-lock":
            if not by_id("settings_pin"):
                swipe_up(0.78, 0.45)
            if tap(by_id("settings_pin"), 2.5):
                capture(path)
                sh("shell", "input", "keyevent", "4"); time.sleep(1.5)
        elif scene == "no-account":
            sh("shell", "pm", "clear", DEBUG)
            sh("shell", "am", "force-stop", DEBUG); time.sleep(1)
            sh("shell", "am", "start", "-n", "%s/.MainActivity" % DEBUG); time.sleep(12)
            capture(path)


def main():
    only = sys.argv[1:] or list(DEVICES)
    for dev in only:
        cfg = DEVICES[dev]
        scenes = PHONE_SCENES if dev == "pixel-10-pro" else TABLET_SCENES
        sh("shell", "wm", "size", cfg["size"])
        sh("shell", "wm", "density", cfg["density"])
        time.sleep(3)
        print("== %s (%s @ %s)" % (dev, cfg["size"], cfg["density"]))
        for locale in ("id-ID", "en-US"):
            outdir = os.path.join(RAW, dev, locale)
            sh("shell", "am", "force-stop", DEMO); time.sleep(1)
            sh("shell", "am", "start", "-n", "%s/.MainActivity" % DEMO); time.sleep(12)
            set_locale(locale, DEMO)
            print("  %s / %s" % (dev, locale))
            capture_set(DEMO, scenes, outdir, locale)
        sh("shell", "wm", "size", "reset")
        sh("shell", "wm", "density", "reset")
    print("done")


if __name__ == "__main__":
    main()
