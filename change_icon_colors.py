import os
import re

directory = r'c:\Users\mannu\AndroidStudioProjects\scanmyPills\app\src\main\res\drawable'
icons = [
    'ic_home.xml',
    'ic_barcode_scanner.xml',
    'ic_camera.xml',
    'ic_bell.xml',
    'ic_settings.xml'
]

for icon in icons:
    filepath = os.path.join(directory, icon)
    if not os.path.exists(filepath):
        print(f"File not found: {icon}")
        continue
        
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # Replace any fillColor with #404040
    # The current color might be #000000, #8D92A3, etc.
    new_content = re.sub(r'android:fillColor=".*?"', 'android:fillColor="#404040"', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
        
    print(f"Updated {icon}")
