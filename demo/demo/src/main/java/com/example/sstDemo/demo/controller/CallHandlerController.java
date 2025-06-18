package com.example.sstDemo.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CallHandlerController {

    private final Map<String, String> keywordToAudioMap = new HashMap<>() {{
        put("sales", "sales_prompt.wav");
        put("support", "support_prompt.wav");
        put("callback", "callback_prompt.wav");
        put("agent", "transfer_agent_prompt.wav");
        put("do not call", "dnc_prompt.wav");
    }};

    @PostMapping(value = "/call-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> handleCall(@RequestParam("file") MultipartFile audioFile) {
        try {
            File tempAudio = File.createTempFile("user_audio", ".wav");
            audioFile.transferTo(tempAudio);

            String transcription = transcribeAudio(tempAudio.getAbsolutePath());
            String matchedAudio = matchKeyword(transcription);

            if (matchedAudio != null) {
                return ResponseEntity.ok("/api/audio/" + matchedAudio);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching prompt found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/batch-call-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Map<String, String>>> handleBatchCall(@RequestParam("files") MultipartFile[] audioFiles) {
        Map<String, Map<String, String>> results = new HashMap<>();

        for (MultipartFile audioFile : audioFiles) {
            Map<String, String> result = new HashMap<>();
            try {
                File tempAudio = File.createTempFile("user_audio", ".wav");
                audioFile.transferTo(tempAudio);

                String transcription = transcribeAudio(tempAudio.getAbsolutePath());
                String matchedAudio = matchKeyword(transcription);

                result.put("transcription", transcription);
                result.put("matchedPrompt", matchedAudio != null ? matchedAudio : "No match found");
            } catch (Exception e) {
                result.put("transcription", "");
                result.put("matchedPrompt", "Error: " + e.getMessage());
            }
            results.put(audioFile.getOriginalFilename(), result);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping(value = "/audio/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getAudio(@PathVariable String fileName) throws IOException {
        Path path = Paths.get("src/main/resources/audio", fileName);
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().body(resource);
    }

    private String transcribeAudio(String filePath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python", "demo/whisper_transcribe.py", filePath);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        InputStream result = process.getInputStream();
        String output = new String(result.readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        System.out.println("Whisper output:\n" + output);
        System.out.println("Exit code: " + exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("Whisper transcription failed.\nOutput:\n" + output);
        }

        return output.trim().toLowerCase();
    }

    private String matchKeyword(String text) {
        System.out.println("Checking transcription: " + text);
        for (String keyword : keywordToAudioMap.keySet()) {
            if (text.contains(keyword)) {
                System.out.println("Matched keyword: " + keyword);
            }
        }

        return keywordToAudioMap.entrySet().stream()
                .filter(entry -> text.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}