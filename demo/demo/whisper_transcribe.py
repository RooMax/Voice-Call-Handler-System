import sys
import whisper
import os

# Get and normalize the audio path
file_path = sys.argv[1]
file_path = os.path.abspath(file_path)

# Transcribe using Whisper (no extra quotes)
model = whisper.load_model("base")
result = model.transcribe(file_path)
print(result["text"])
