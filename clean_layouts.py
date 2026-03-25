import os
import re

LAYOUT_DIR = r"c:\Users\mannu\AndroidStudioProjects\scanmyPills\app\src\main\res\layout"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # Remove gradient headers blocks entirely
    content = re.sub(r'<View[^>]*android:id="@+id/(top_background|headerBackground)"[^>]*/>', '', content)
    
    # Remove specific style attributes entirely
    styles_to_remove = [
        r'style="@style/AppCard"',
        r'style="@style/AppButton"',
        r'style="@style/FieldLabel"',
        r'style="@style/ReviewEditText"',
    ]
    for s in styles_to_remove:
        content = content.replace(s, '')

    # Remove specific app and android attributes added for premium look
    attrs_to_remove = [
        r'app:cardBackgroundColor="@color/white"',
        r'app:strokeWidth="0dp"',
        r'android:foreground="\?attr/selectableItemBackground[^"]*"',
        r'app:shapeAppearanceOverlay="[^"]*"',
        r'android:background="@color/bg_main"'
    ]
    for attr in attrs_to_remove:
        content = re.sub(attr, '', content)
        
    # Also change root background to white if bg_main was there
    content = content.replace('@color/bg_main', '@color/white')

    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {os.path.basename(filepath)}")

for filename in os.listdir(LAYOUT_DIR):
    if filename.endswith(".xml"):
        process_file(os.path.join(LAYOUT_DIR, filename))
