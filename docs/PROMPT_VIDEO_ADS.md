# Prompt: Play Store video ads from the raw captures

Two deliverables, one source of truth:

- **Horizontal** 1920×1080 (16:9) — the Play Store listing promo video (Google Play takes a
  YouTube link, so this cut is also the YouTube upload) and any landscape ad placement.
- **Vertical** 1080×1920 (9:16) — Shorts, Reels, TikTok and vertical ad inventory.

Both are cut from the same eight raw captures in `out/raw/pixel-10-pro/`, so the two exports stay
in sync. Copy is Indonesian-first with an English variant, matching the app's two listing locales.

The storyboard and the exact beats are part of the prompt below: paste the whole block into the
agent that has the `openmontage` skill installed.

---

## Prompt (copy from here)

Use the **openmontage** skill to plan, composite and render two Play Store video ads for the
CycleJournal Android app. Treat this as a motion-graphics montage over app captures, not as
screen recording. Work from the assets already in this repository; do not invent new app art.

**Inputs**

- Screens (portrait, 1279×2853 PNG): `out/raw/pixel-10-pro/dashboard.png`,
  `calendar.png`, `daily-log.png`, `clinical-detail.png`, `bbt-chart.png`,
  `bilingual-text-size.png`, `medical-report.png`, `privacy-security.png`
- Framed store stills, if a beat needs a finished tile: `out/screenshots/pixel-10-pro/id-ID/`
  and `.../en-US/` (1080×1920)
- Device art: `out/web/frames/pixel-10-pro.webp`, `out/web/frames/17-pro-blue.png`
- Logo / store art: `store_assets/play_store_icon_512.png`,
  `store_assets/feature_graphic_1024x500.png`
- Typefaces (bundled, use these, do not substitute): `out/web/fonts/Montserrat-700.ttf` for
  headlines, `out/web/fonts/DMSans-400.ttf` for subheads and captions
- Copy to reuse verbatim, both languages: the `scenes[]` block in `goldie.config.ts`
  (headline + subhead per scene, `id-ID` and `en-US`)

**Brand**

- Background: coral-to-pink gradient, `linear-gradient(145deg, #FF8A71 0%, #FF5E7D 100%)`
- Headline `#FFFFFF`, subhead `#FFF1F2`
- Motion language: calm and clinical. The audience is logging basal temperature at 05:30 and
  reading a report with a doctor. No whooshes, no glitch effects, no emoji.

**Beat sheet — 20 s horizontal, 15 s vertical.** The vertical cut drops beats 7 and 8 and keeps
the same order, so both cuts tell one story. Beats 1–4 carry the pitch; a viewer who stops at 7 s
must still know what the app does.

| # | Time (16:9) | Screen | On-screen copy (id-ID) | Motion | Audio |
|---|---|---|---|---|---|
| 1 | 0.0–2.5 | `dashboard.png` | **Siklus Anda, terpantau klinis** | Device rises 4%, slow push-in 100%→106%, copy fades in from 12 px below | Ambient pad in, soft low pulse |
| 2 | 2.5–5.0 | `calendar.png` | **Lihat seluruh siklus sekilas** | Horizontal parallax on the month grid, device tilts −6° then settles | Light tick on the transition |
| 3 | 5.0–7.5 | `daily-log.png` | **Catat harian dalam dua ketukan** | Two-step zoom: sheet in, then a 0.2 s pulse on the bleeding row (draw a soft ripple, do not fake a cursor) | Muted tap-tap |
| 4 | 7.5–10.0 | `clinical-detail.png` | **Suhu basal, lendir, gejala** | Slow vertical pan down the panel, chips fade in staggered 80 ms | Pad swells slightly |
| 5 | 10.0–12.5 | `bbt-chart.png` | **Pantau pergeseran suhu** | Animate the temperature line left-to-right as a draw-on, 1.2 s, ease-out | Single warm chime on the shift |
| 6 | 12.5–15.0 | `medical-report.png` | **Laporan siap dibaca dokter** | Page settles with a 3° counter-tilt, metrics highlight in sequence | Paper-soft swell |
| 7 | 15.0–17.5 | `privacy-security.png` | **Privat sejak dirancang** | Lock glyph draws, list rows slide in from the right | Pulse tightens, then resolves |
| 8 | 17.5–20.0 | `bilingual-text-size.png` → end card | **Dua bahasa, ukuran sesuai Anda** → **Gratis · Offline · Tanpa pelacak** | Screens cross-dissolve into the end card: logo, wordmark, Play badge, gradient background | Pad resolves to a clean tonic |

Vertical cut (15 s): beats 1, 2, 3, 4, 5, 6, then the end card from beat 8; copy sits above the
device, device centred, bottom 420 px kept clear for platform UI.

English variant: same beats and timing, copy swapped for the `en-US` strings in `goldie.config.ts`.

**Compliance — non-negotiable, this is a health app**

- No diagnosis, treatment, cure or prevention claims. Predictions are estimates: say
  "prediksi" / "predicted", never "menjamin" / "guarantees".
- Add a legible end-card line: **"Bukan alat kontrasepsi."** / "Not a contraceptive."
- No medical imagery, no blood, no before/after body shots.
- No Google Play badge imitations beyond the standard badge; no "number one" or "best" claims.
- Music and SFX must be royalty-free or licensed for commercial use; list the licence of every
  track in the render notes.

**Output specs**

- Horizontal: 1920×1080, H.264 High, `yuv420p`, 30 fps, AAC 48 kHz stereo, ~12 Mbps, ≤ 30 s
- Vertical: 1080×1920, same codec and audio settings, ~8 Mbps, ≤ 20 s
- No alpha, sRGB, safe margins ≥ 64 px (16:9) and ≥ 90 px sides (9:16), captions inside the
  safe area and never over the device screen
- Deliver to `out/video-ads/`:
  - `cyclejournal-ad-1920x1080-id.mp4`, `cyclejournal-ad-1920x1080-en.mp4`
  - `cyclejournal-ad-1080x1920-id.mp4`, `cyclejournal-ad-1080x1920-en.mp4`
  - `cyclejournal-ad-1920x1080-id-thumb.jpg` (first-frame-plus-copy still for the YouTube card)
  - `storyboard.md` (this beat sheet with the final timings) and `notes.md` (fonts, music
    licences, render settings, ffmpeg commands)
- Keep the project file so a later edit re-renders both cuts from one timeline.

**Acceptance**

1. Both ratios exist for both languages, with the specs above verified by `ffprobe`.
2. Every beat's copy is legible at 480 px wide (mobile-first check) — export one frame per beat
   as PNG for review and iterate on any that fail.
3. The vertical cut is under 20 s and its first 3 s name the app and the benefit.
4. No beat shows a blank or loading screen: the captures are populated by the demo seed, and a
   beat that looks empty is a bug to fix, not to ship.
5. Render notes name every font, track and licence.

## Prompt ends

---

## Notes for the operator

- **The current captures are English on both locales** (the device locale drives the app UI, and
  goldie's locale pinning did not reach it). Before rendering the Indonesian cut, set the device
  locale to `id-ID` (`adb shell setprop persist.sys.locale id-ID` + reboot) and re-run
  `goldie all --locale id-ID`, so the app UI and the captions agree. Until then, render the
  English cut from the existing captures and treat the Indonesian cut as blocked on that re-run.
- Google Play wants a **YouTube link** for the listing video, so upload the horizontal cut to
  YouTube first (unlisted is fine), then paste the URL in the Play Console listing.
- Suggested YouTube title/description: `CycleJournal — pelacak siklus & laporan SpOG` /
  `Aplikasi kalender haid offline. Prediksi haid, ovulasi, dan masa subur; catat suhu basal,
  lendir serviks, dan skala nyeri; ekspor laporan PDF siap dokter. Data tetap di perangkat.`
