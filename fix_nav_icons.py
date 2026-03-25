import os
import re

directory = r'c:\Users\mannu\AndroidStudioProjects\scanmyPills\app\src\main\java\com\simats\scanmypills'
files = [
    'AllMedicinesActivity.kt', 'EditMedicineActivity.kt', 'MedicineDetailsActivity.kt',
    'MainActivity.kt', 'IdentifyResultActivity.kt', 'IdentifyPillActivity.kt',
    'ScanPillActivity.kt', 'ScanReviewActivity.kt', 'RemindersActivity.kt', 'SettingsActivity.kt'
]

replacement_profile = """    private fun loadNavIcons() {
        binding.ivNavHome.setImageResource(R.drawable.ic_home)
        binding.ivNavScan.setImageResource(R.drawable.ic_scan_nav)
        binding.ivNavIdentify.setImageResource(R.drawable.ic_identify_nav)
        binding.ivNavReminders.setImageResource(R.drawable.ic_reminders_nav)
        binding.ivNavProfile.setImageResource(R.drawable.ic_settings)
    }"""

replacement_settings = """    private fun loadNavIcons() {
        binding.ivNavHome.setImageResource(R.drawable.ic_home)
        binding.ivNavScan.setImageResource(R.drawable.ic_scan_nav)
        binding.ivNavIdentify.setImageResource(R.drawable.ic_identify_nav)
        binding.ivNavReminders.setImageResource(R.drawable.ic_reminders_nav)
        binding.ivNavSettings.setImageResource(R.drawable.ic_settings)
    }"""

for filename in files:
    filepath = os.path.join(directory, filename)
    if not os.path.exists(filepath): continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    if 'private fun loadNavIcons()' in content and 'image2url.com' in content:
        # Find the start index
        start_idx = content.find('private fun loadNavIcons()')
        # Find the end of the method by counting braces
        brace_count = 0
        end_idx = start_idx
        found_first_brace = False
        
        for i in range(start_idx, len(content)):
            if content[i] == '{':
                brace_count += 1
                found_first_brace = True
            elif content[i] == '}':
                brace_count -= 1
            
            if found_first_brace and brace_count == 0:
                end_idx = i + 1
                break
                
        old_func = content[start_idx:end_idx]
        
        # Determine if it uses ivNavProfile or ivNavSettings
        if 'ivNavProfile' in old_func:
            content = content.replace(old_func, replacement_profile)
        else:
            content = content.replace(old_func, replacement_settings)
            
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filename}")
