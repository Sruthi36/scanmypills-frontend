import os

directory = 'app/src/main/java/com/simats/scanmypills/'

for filename in os.listdir(directory):
    if filename.endswith('.kt'):
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        if 'class ' in content and 'AppCompatActivity' in content:
            if '@Suppress("DEPRECATION")' not in content:
                lines = content.split('\n')
                new_lines = []
                for line in lines:
                    if line.strip().startswith('class ') and 'AppCompatActivity' in line:
                        new_lines.append('@Suppress("DEPRECATION")')
                    new_lines.append(line)
                
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write('\n'.join(new_lines))
                print(f'Added suppress to {filename}')
