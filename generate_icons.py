import os
from PIL import Image

# Configuration
SOURCE_IMAGE = "/home/kasyee/.gemini/antigravity/brain/92b6889a-9bdf-487a-981d-fb308d54b884/media__1778298747152.png"
ANDROID_RES_DIR = "/home/kasyee/AndroidStudioProjects/Kassaku/app/src/main/res"
WEB_PUBLIC_DIR = "/home/kasyee/KasSaku/public"

ANDROID_DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

def generate_android_icons(img):
    print("Generating Android icons...")
    backup_dir = "/home/kasyee/AndroidStudioProjects/Kassaku/backups/icons"
    os.makedirs(backup_dir, exist_ok=True)
    
    for density, size in ANDROID_DENSITIES.items():
        target_dir = os.path.join(ANDROID_RES_DIR, density)
        os.makedirs(target_dir, exist_ok=True)
        
        # Resize
        resized_img = img.resize((size, size), Image.LANCZOS)
        
        # Save ic_launcher.png
        launcher_path = os.path.join(target_dir, "ic_launcher.png")
        if os.path.exists(launcher_path):
            backup_path = os.path.join(backup_dir, f"{density}_ic_launcher.png.bak")
            os.rename(launcher_path, backup_path)
        resized_img.save(launcher_path)
        print(f"  Saved {launcher_path} ({size}x{size})")
        
        # Save ic_launcher_round.png
        launcher_round_path = os.path.join(target_dir, "ic_launcher_round.png")
        if os.path.exists(launcher_round_path):
            backup_path = os.path.join(backup_dir, f"{density}_ic_launcher_round.png.bak")
            os.rename(launcher_round_path, backup_path)
        resized_img.save(launcher_round_path)
        print(f"  Saved {launcher_round_path} ({size}x{size})")

def generate_web_assets(img):
    print("Generating Web assets...")
    os.makedirs(WEB_PUBLIC_DIR, exist_ok=True)
    backup_dir = "/home/kasyee/KasSaku/backups/icons"
    os.makedirs(backup_dir, exist_ok=True)
    
    # Save logo.png (512x512)
    logo_path = os.path.join(WEB_PUBLIC_DIR, "logo.png")
    if os.path.exists(logo_path):
        backup_path = os.path.join(backup_dir, "logo.png.bak")
        os.rename(logo_path, backup_path)
    web_logo = img.resize((512, 512), Image.LANCZOS)
    web_logo.save(logo_path)
    print(f"  Saved {logo_path} (512x512)")
    
    # Save favicon.ico
    favicon_path = os.path.join(WEB_PUBLIC_DIR, "favicon.ico")
    if os.path.exists(favicon_path):
        backup_path = os.path.join(backup_dir, "favicon.ico.bak")
        os.rename(favicon_path, backup_path)
    
    icon_sizes = [16, 32, 48]
    img.save(favicon_path, format='ICO', sizes=[(s, s) for s in icon_sizes])
    print(f"  Saved {favicon_path} (16, 32, 48)")

def main():
    if not os.path.exists(SOURCE_IMAGE):
        print(f"Error: Source image not found at {SOURCE_IMAGE}")
        return

    try:
        img = Image.open(SOURCE_IMAGE).convert("RGBA")
        generate_android_icons(img)
        generate_web_assets(img)
        print("Success: All icons generated successfully.")
    except Exception as e:
        print(f"Error during icon generation: {e}")

if __name__ == "__main__":
    main()
