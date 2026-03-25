import os

filepath = r'c:\Users\mannu\AndroidStudioProjects\scanmyPills\app\src\main\res\layout\activity_main.xml'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

if 'NestedScrollView' not in content:
    wrapper_start = '''    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@+id/bottomNav">

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="24dp">'''

    wrapper_end = '''        </androidx.constraintlayout.widget.ConstraintLayout>
    </androidx.core.widget.NestedScrollView>'''

    lines = content.split('\n')

    insert_start = -1
    insert_end = -1

    for i, line in enumerate(lines):
        if '<!-- Header Gradient Background -->' in line:
            insert_start = i
        if '<!-- Bottom Navigation -->' in line:
            insert_end = i
            
    if insert_start != -1 and insert_end != -1:
        lines.insert(insert_end, wrapper_end) # insert end first to not mess up start index
        lines.insert(insert_start, wrapper_start)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines))
        print("Updated activity_main.xml")
