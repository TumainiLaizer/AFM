import os

input_file = 'app/src/main/assets/databases/nation_names.csv'
output_dir = 'app/src/main/assets/databases/names'
os.makedirs(output_dir, exist_ok=True)

with open(input_file, 'r', encoding='utf-8') as f:
    lines = f.readlines()

i = 0
count = 0
while i < len(lines):
    nation = lines[i].strip()
    if not nation:
        i += 1
        continue

    if i + 2 >= len(lines):
        break

    first_names = lines[i+1].strip().replace('\t', ',')
    last_names = lines[i+2].strip().replace('\t', ',')

    nation_file_name = nation.lower().replace(' ', '_')

    with open(f"{output_dir}/first_names_{nation_file_name}.csv", 'w', encoding='utf-8') as f_out:
        f_out.write(first_names)

    with open(f"{output_dir}/surnames_{nation_file_name}.csv", 'w', encoding='utf-8') as f_out:
        f_out.write(last_names)

    print(f"Split {nation}")
    i += 3
    count += 1

print(f"Successfully split {count} nations.")
