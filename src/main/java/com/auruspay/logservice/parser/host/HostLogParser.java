package com.auruspay.logservice.parser.host;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auruspay.logservice.decryptor.AurusDecryptor;
import com.auruspay.logservice.dto.LogRequest;
import com.auruspay.logservice.dto.ZabaxRequest;

@Component
public class HostLogParser {

    private static final Logger log = LoggerFactory.getLogger(HostLogParser.class);

    public Map<String, Object> parseLog(ZabaxRequest zabaxRequest ,LogRequest request, String logDetails) {

        long startTime = System.currentTimeMillis();

        Map<String, Object> map = new LinkedHashMap<>();

        String txnId = request.getTxnId();
        String uuid = request.getUuid();

        map.put("TXNID", txnId);
        map.put("UUID", uuid);

        if (logDetails == null || logDetails.isEmpty()) {
            log.warn("Empty logDetails received for UUID={}", uuid);
            map.put("IssuesCount", 0);
            map.put("LogDetails", "");
            return map;
        }

        String[] logLines = logDetails.split("\\r?\\n");

        List<String> issues = new ArrayList<>();

        for (String line : logLines) {

            if (line == null || !line.contains(uuid)) continue;

            // 🔍 Debug log (optional)
            log.debug("Processing line: {}", line);

            processLine(line, "AURUSPAY ENCRYPTED REQUEST :", "AurusReq", map);
            processLine(line, "[STPL-GRAY-STREAM]- REQUEST :", "ProcReq", map);

            if (line.contains("[STPL-GRAY-STREAM]-FINAL RESPONSE :")) {
                processLine(line, "[STPL-GRAY-STREAM]-FINAL RESPONSE :", "ProcRes", map);
            } else if (line.contains("[STPL-GRAY-STREAM]-TRANSACTION RESPONSE :")) {
                processLine(line, "[STPL-GRAY-STREAM]-TRANSACTION RESPONSE :", "ProcRes", map);
            }

            processLine(line, "AURUSPAY ENCRYPTED RESPONSE :", "AurusRes", map);

            if (line.matches(".*(ERROR|Exception|Timeout|Declined|Failed).*")) {
                issues.add(line.trim());
            }
        }

        map.put("IssuesCount", issues.size());
        map.put("Issues", issues);   // ✅ Added details
        map.put("LogDetails", logDetails);

        long duration = System.currentTimeMillis() - startTime;

        log.info("Log parsing completed for UUID={} with {} issues in {} ms",
                uuid, issues.size(), duration);

        return map;
    }

    private void processLine(String line, String marker, String mapKey, Map<String, Object> map) {

        if (!line.contains(marker)) return;

        try {
            String encrypted = line.substring(line.indexOf(marker) + marker.length()).trim();

            // 🔥 Remove whitespace
            String sanitized = encrypted.replaceAll("\\s", "");

            log.debug("Decrypting {} data", mapKey);

            String decrypted = AurusDecryptor.decryptor(sanitized);

            map.put(mapKey + "Decrypt", decrypted);

        } catch (Exception e) {

            log.error("Decryption failed for {} : {}", mapKey, e.getMessage());

            map.put(mapKey + "Decrypt", "DECRYPTION_ERROR: " + e.getMessage());
        }
    }
}