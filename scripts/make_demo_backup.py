"""Builds a multi-month .cjbackup payload so the report/restore path can be exercised for real.

Produces 5 cycles (~4.5 months) of daily logs: 5-day bleeding, 28-day cycles, a biphasic basal
temperature curve, cervical mucus progression and a couple of painful days per period.

Run from the repository root: python scripts/make_demo_backup.py
"""

import json
import os
from datetime import date, timedelta

START = date(2026, 5, 4)          # first day of the oldest period in the file
TODAY = date(2026, 9, 13)
CYCLE = 28
PERIOD = 5

FLOWS = ["NONE", "SPOTTING", "LIGHT", "MEDIUM", "HEAVY"]
MUCUS = ["NONE", "DRY", "STICKY", "CREAMY", "WATERY", "EGG_WHITE"]

logs = []
cycle_starts = []
cursor = START
while cursor <= TODAY:
    cycle_starts.append(cursor)
    cursor = cursor + timedelta(days=CYCLE)

for index, start in enumerate(cycle_starts):
    for offset in range(CYCLE):
        day = start + timedelta(days=offset)
        if day > TODAY:
            break
        day_in_cycle = offset + 1
        ovulation_day = CYCLE - 14

        # bleeding: 5 days at the start of the cycle, tapering
        if day_in_cycle == 1:
            flow = "MEDIUM"
        elif day_in_cycle == 2:
            flow = "HEAVY"
        elif day_in_cycle == 3:
            flow = "MEDIUM"
        elif day_in_cycle == 4:
            flow = "LIGHT"
        elif day_in_cycle == 5:
            flow = "SPOTTING"
        else:
            flow = "NONE"

        # basal temperature: low follicular plateau, then a biphasic rise after ovulation
        if day_in_cycle <= ovulation_day:
            bbt = round(36.35 + 0.03 * (day_in_cycle % 4), 2)
        else:
            bbt = round(36.68 + 0.03 * (day_in_cycle % 5), 2)

        # cervical mucus follows the classic fertile progression
        if day_in_cycle in (ovulation_day - 1,):
            mucus = "EGG_WHITE"
        elif day_in_cycle in (ovulation_day - 3, ovulation_day - 2):
            mucus = "WATERY"
        elif day_in_cycle in (ovulation_day - 5, ovulation_day - 4):
            mucus = "CREAMY"
        elif day_in_cycle <= 6:
            mucus = "NONE"
        else:
            mucus = "DRY" if day_in_cycle % 2 else "STICKY"

        # pain: strongest on the first two bleeding days, occasional mid-cycle twinge
        if day_in_cycle <= 2:
            pain = 6 if index % 2 == 0 else 4
        elif day_in_cycle == 3:
            pain = 3
        elif day_in_cycle == ovulation_day:
            pain = 2
        else:
            pain = 0

        entry = {
            "date": day.isoformat(),
            "flow": flow,
            "bbt": bbt,
            "mucus": mucus,
            "pain_vas": pain,
            "analgesic": day_in_cycle <= 2,
        }
        if pain >= 4:
            entry["pain_loc"] = "pelvis, lower_back"
        if day_in_cycle == 1:
            entry["notes"] = "period started"
        if pain >= 6:
            entry["notes"] = "cramps, took a painkiller"
        logs.append(entry)

payload = {
    "version": 1,
    "user_id": "px-demo-9f2c41",
    "exported_at": 1789250000000,
    "daily_logs": logs,
}

out_dir = os.path.join(os.path.dirname(__file__), "..", "out")
os.makedirs(out_dir, exist_ok=True)
path = os.path.join(out_dir, "multi_month_backup.cjbackup")
with open(path, "w", encoding="utf-8") as fh:
    json.dump(payload, fh, ensure_ascii=False, indent=1)

print("cycles:", len(cycle_starts), "| logs:", len(logs))
print("range:", logs[0]["date"], "->", logs[-1]["date"])
print("written:", os.path.abspath(path), os.path.getsize(path), "bytes")
