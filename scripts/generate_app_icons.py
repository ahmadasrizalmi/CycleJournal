import os
from PIL import Image, ImageDraw

def generate_icons(logo_path="design/logo cyclejournal.jpg", res_dir="app/src/main/res", store_dir="store_assets"):
    os.makedirs(store_dir, exist_ok=True)
    if not os.path.exists(logo_path):
        print(f"Error: {logo_path} not found.")
        return

    img = Image.open(logo_path).convert("RGBA")
    
    # 1. Google Play Store High-Res Icon (512x512 PNG, 32-bit)
    play_icon = img.resize((512, 512), Image.Resampling.LANCZOS)
    play_icon_path = os.path.join(store_dir, "play_store_icon_512.png")
    play_icon.save(play_icon_path, "PNG")
    print(f"Generated: {play_icon_path}")

    # 2. In-App & Medical PDF Header Logo (128x128 PNG)
    drawable_dir = os.path.join(res_dir, "drawable")
    os.makedirs(drawable_dir, exist_ok=True)
    pdf_logo = img.resize((128, 128), Image.Resampling.LANCZOS)
    pdf_logo_path = os.path.join(drawable_dir, "logo_pdf_header.png")
    pdf_logo.save(pdf_logo_path, "PNG")
    print(f"Generated: {pdf_logo_path}")

    # 3. Android Mipmap Icons across all density buckets
    mipmap_densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }
    for folder, size in mipmap_densities.items():
        out_dir = os.path.join(res_dir, folder)
        os.makedirs(out_dir, exist_ok=True)
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        resized.save(os.path.join(out_dir, "ic_launcher.png"), "PNG")
        resized.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG")
        print(f"Generated: {folder} ({size}x{size})")

    # 4. Google Play Feature Graphic (1024x500 PNG)
    width, height = 1024, 500
    fg = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(fg)
    # Gradient background from Coral (#FF8A71) to Pink (#FF5E7D)
    for x in range(width):
        r = int(0xFF + (0xFF - 0xFF) * (x / width))
        g = int(0x8A + (0x5E - 0x8A) * (x / width))
        b = int(0x71 + (0x7D - 0x71) * (x / width))
        draw.line([(x, 0), (x, height)], fill=(r, g, b, 255))
    
    # Place circular emblem on right half
    logo_size = 360
    logo_small = img.resize((logo_size, logo_size), Image.Resampling.LANCZOS)
    fg.paste(logo_small, (width - logo_size - 80, (height - logo_size) // 2), logo_small)
    
    fg_path = os.path.join(store_dir, "feature_graphic_1024x500.png")
    fg.save(fg_path, "PNG")
    print(f"Generated: {fg_path}")

if __name__ == "__main__":
    generate_icons()
