import os

directory = r'c:\Users\mannu\AndroidStudioProjects\scanmyPills\app\src\main\java\com\simats\scanmypills'
files = [
    'AllMedicinesActivity.kt', 'EditMedicineActivity.kt', 'MedicineDetailsActivity.kt',
    'MainActivity.kt', 'IdentifyResultActivity.kt', 'IdentifyPillActivity.kt',
    'ScanPillActivity.kt', 'ScanReviewActivity.kt', 'RemindersActivity.kt', 'SettingsActivity.kt'
]

for filename in files:
    filepath = os.path.join(directory, filename)
    if not os.path.exists(filepath): continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    new_content = content.replace('R.drawable.ic_scan_nav', 'R.drawable.ic_scan')
    new_content = new_content.replace('R.drawable.ic_identify_nav', 'R.drawable.ic_camera')
    new_content = new_content.replace('R.drawable.ic_reminders_nav', 'R.drawable.ic_bell')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f"Updated nav icons in {filename}")
