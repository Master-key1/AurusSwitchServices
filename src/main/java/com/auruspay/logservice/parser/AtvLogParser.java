package com.auruspay.logservice.parser;

import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auruspay.logservice.decryptor.AurusDecryptor;
import com.auruspay.logservice.dto.LogRequest;

@Component
public class AtvLogParser {

    private static final Logger log = LoggerFactory.getLogger(AtvLogParser.class);

    // 🔥 Precompiled issue pattern (performance)
    private static final Pattern ISSUE_PATTERN =
            Pattern.compile("ERROR|Exception|Timeout|Declined|Failed", Pattern.CASE_INSENSITIVE);

    // 🔥 Markers (easy to maintain)
    private static final Map<String, String> MARKERS = Map.of(
            "VAULT ENCRYPTED REQUEST :", "VAULT REQUEST",
            "VAULT ENCRYPTED RESPONSE :", "VAULT RESPONSE",
            "ATV RESPONSE CODE :", "AtvResCode",
            "ATV RESPONSE MESSAGE :", "AtvResMessage"
    );

    public Map<String, Object> parseLog(LogRequest request, String logDetails) {

        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();

        String txnId = request.getTxnId();
        String uuid = request.getUuid();

        result.put("TIMESTAMP", new Date().toString());
        result.put("TXNID", txnId);
        result.put("UUID", uuid);

        if (logDetails == null || logDetails.isBlank()) {
            log.warn("Empty logDetails received for UUID={}", uuid);
            result.put("IssuesCount", 0);
            result.put("Issues", Collections.emptyMap());
            result.put("LogDetails", "");
            return result;
        }

        String[] lines = logDetails.split("\\r?\\n");

        Map<String, String> issues = new LinkedHashMap<>();
        int issueCount = 1;

        for (String line : lines) {

            if (line == null || !line.contains(uuid)) continue;

            // 🔹 Process all markers dynamically
            for (Map.Entry<String, String> entry : MARKERS.entrySet()) {
                processLine(line, entry.getKey(), entry.getValue(), result);
            }

            // 🔹 Issue detection (optimized)
            if (ISSUE_PATTERN.matcher(line).find()) {
                issues.put("Issue " + issueCount++, line.trim());
            }
        }

        result.put("IssuesCount", issues.size());
        result.put("Issues", issues);
        result.put("LogDetails", logDetails);

        long duration = System.currentTimeMillis() - startTime;

        log.info("ATV log parsed for UUID={} | Issues={} | Time={} ms",
                uuid, issues.size(), duration);

        return result;
    }

    // 🔥 Cleaner + optimized processor
    private void processLine(String line, String marker, String key, Map<String, Object> map) {

        int index = line.indexOf(marker);
        if (index == -1) return;

        String value = line.substring(index + marker.length()).trim();
        if (value.isEmpty()) return;

        map.put(key, value);

        // 🔐 Decrypt only VAULT fields
        if ("VAULT REQUEST".equals(key) || "VAULT RESPONSE".equals(key)) {
            try {
                String decrypted = AurusDecryptor.decryptor(value);
                map.put(key + " (DECRYPTED)", decrypted);
            } catch (Exception e) {
                log.warn("Decryption failed for {}: {}", key, e.getMessage());
                map.put(key + " (DECRYPTED)", "Decryption failed");
            }
        }
    }
}