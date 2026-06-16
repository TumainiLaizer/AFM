import sqlite3
import json
import os

def sqlite_to_json(db_path, output_dir="json_export"):
    """Convert all tables in SQLite database to JSON files"""
    
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    
    # Connect to database
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row  # This allows column access by name
    cursor = conn.cursor()
    
    # Get all table names
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")
    tables = cursor.fetchall()
    
    for table in tables:
        table_name = table[0]
        print(f"Exporting {table_name}...")
        
        # Get all data from table
        cursor.execute(f"SELECT * FROM {table_name}")
        rows = cursor.fetchall()
        
        # Convert to list of dictionaries
        data = [dict(row) for row in rows]
        
        # Write to JSON file
        output_file = os.path.join(output_dir, f"{table_name}.json")
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, default=str)
        
        print(f"  Exported {len(data)} records to {output_file}")
    
    conn.close()
    print(f"\n✅ Export complete! JSON files saved to: {output_dir}")

# Usage
if __name__ == "__main__":
    sqlite_to_json("afm_static.db", "static_data_export")