# Voice Call Handler System

## Overview
An intelligent voice call handling system built with Spring Boot that automatically processes incoming calls using AI-powered speech recognition. The system transcribes voice messages and routes them based on content analysis.

## Features
- Speech-to-text conversion using OpenAI's Whisper AI
- Intelligent keyword-based call routing
- Support for both single and batch call processing
- Automated response selection based on call content
- Real-time audio file processing

### Call Categories
The system recognizes and routes 5 types of requests:
- Sales inquiries
- Support requests
- Callback requests
- Agent transfers
- Do-not-call requests

## Technical Stack
- Java 21
- Spring Boot 3.5.1
- Maven
- Python (for Whisper AI integration)
- OpenAI Whisper Model (base)

## API Endpoints

## Process Single Call
```http 
POST /api/call-handler
Content-Type: multipart/form-data
```
## Process Single Call
```http
POST /api/batch-call-handler
Content-Type: multipart/form-data
````
## Get Audio File
```http

GET /api/audio/{fileName} 
````
## Setup Instructions
Prerequisites
- Java 21 JDK
- Python with OpenAI Whisper installed
- Maven
- Audio files for responses

Installation
- Clone the repository
- Install Python dependencies:
````pip install whisper````
- Build the project:
```mvn clean install```
- Place response audio files in ````src/main/resources/audio/:````
- sales_prompt.wav
- support_prompt.wav
- callback_prompt.wav
- transfer_agent_prompt.wav
- dnc_prompt.wav

## Running the Application
```mvn spring-boot:run```
- The application will start on port 8080.

## Configuration
- Server port: 8080 (configurable in ````application.properties````)
- Whisper model: Base version (configurable in ````whisper_transcribe.py````)
## License
- Apache License, Version 2.0

## Notes

- Currently uses Spring Boot snapshot version (3.5.1-SNAPSHOT)
- Includes development tools for easier debugging
- Requires proper audio file setup in resources directory
