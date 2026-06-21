import json  
log_file = r'C:\Users\ardam\.gemini\antigravity\brain\0ebbbc93-2d99-4173-821f-44084c4d9851\.system_generated\logs\transcript_full.jsonl'  
target = None  
with open(log_file, 'r', encoding='utf-8') as f:  
    for line in f:  
        if '\" "type\:\VIEW_FILE\' in line and 'charts.js' in line:  
            target = line  
if target:  
    with open('charts_extracted.json', 'w', encoding='utf-8') as out:  
        out.write(target)  
