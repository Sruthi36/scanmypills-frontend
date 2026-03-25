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
        
    # Replace ic_scan with ic_barcode_scanner
    new_content = content.replace('R.drawable.ic_scan)', 'R.drawable.ic_barcode_scanner)')
    new_content = new_content.replace('R.drawable.ic_scan\n', 'R.drawable.ic_barcode_scanner\n')
    
    # Replace ProfileActivity with SettingsActivity in Nav Bar
    # Since we don't want to replace all ProfileActivity instances (just the navbar ones),
    # let's be somewhat specific if possible, or just replace since the settings nav handles this everywhere.
    
    # the nav bar setup usually looks like:
    # binding.navProfile.setOnClickListener {
    #     val intent = Intent(this, ProfileActivity::class.java)
    
    # We will search for 'navProfile.setOnClickListener' and replace the Intent inside it.
    
    lines = new_content.split('\n')
    in_nav_profile = False
    for i in range(len(lines)):
        if 'binding.navProfile.setOnClickListener' in lines[i]:
            in_nav_profile = True
        
        if in_nav_profile and 'Intent(this, ProfileActivity::class.java)' in lines[i]:
            lines[i] = lines[i].replace('ProfileActivity', 'SettingsActivity')
            in_nav_profile = False
            
        elif in_nav_profile and '}' in lines[i]:
            in_nav_profile = False # exited block
            
    # Also in case they use navSettings
    in_nav_profile = False
    for i in range(len(lines)):
        if 'binding.navSettings.setOnClickListener' in lines[i]:
            in_nav_profile = True
        
        if in_nav_profile and 'Intent(this, ProfileActivity::class.java)' in lines[i]:
            lines[i] = lines[i].replace('ProfileActivity', 'SettingsActivity')
            in_nav_profile = False
            
        elif in_nav_profile and '}' in lines[i]:
            in_nav_profile = False # exited block

    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines))
    print(f"Updated {filename}")
