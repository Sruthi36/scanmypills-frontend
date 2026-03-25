import os

LAYOUT_DIR = r"c:\Users\mannu\AndroidStudioProjects\scanmyPills\app\src\main\res\layout"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content
    content = content.replace('@drawable/bg_dashboard_header', '@color/primary')
    content = content.replace('@color/bg_main', '@color/white')

    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {os.path.basename(filepath)}")

for filename in os.listdir(LAYOUT_DIR):
    if filename.endswith(".xml"):
        process_file(os.path.join(LAYOUT_DIR, filename))
