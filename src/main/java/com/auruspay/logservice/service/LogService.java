package com.auruspay.logservice.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.auruspay.logservice.client.ZabbixClient;
import com.auruspay.logservice.dto.LogRequest;
import com.auruspay.logservice.dto.ZabaxRequest;
import com.auruspay.logservice.parser.host.HostLogParser;

@Service
public class LogService {

    private static final Logger log = LoggerFactory.getLogger(LogService.class); // ✅ Logger

    private final ZabbixClient zabbixClient;
    private final HostLogParser hostLogParser;

    public LogService(ZabbixClient zabbixClient, HostLogParser hostLogParser) {
        this.zabbixClient = zabbixClient;
        this.hostLogParser = hostLogParser;
    }

    public Map<String, Object> getLogs(ZabaxRequest zabaxRequest ,LogRequest request) {

        long startTime = System.currentTimeMillis();

        // 🔥 Validation (FIXED)
        if (request.getIp() == null || request.getIp().isEmpty() ||
            request.getUuid() == null || request.getUuid().isEmpty() ||
            request.getLogPath() == null || request.getLogPath().isEmpty()) {

            log.error("Invalid request received: {}", request);
            throw new RuntimeException("Invalid Details: ip/uuid/logPath required");
        }

        try {
            // 🔍 Log request
            log.info("Fetching logs from Zabbix for IP={}, UUID={}",
                    request.getIp(), request.getUuid());

            // 🌐 External API call
            String logDetails = zabbixClient.fetchLogs( zabaxRequest , request);

            log.debug("Raw log response received (length={} chars)",
                    logDetails != null ? logDetails.length() : 0);

            // 🔄 Parsing logs
            Map<String, Object> dataMap = hostLogParser.parseLog( zabaxRequest , request, logDetails);

            long duration = System.currentTimeMillis() - startTime;

            // ✅ Success log
            log.info("Logs processed successfully for UUID={} in {} ms",
                    request.getUuid(), duration);

            return dataMap;

        } catch (Exception e) {

            // ❌ Error log
            log.error("Error processing logs for UUID={} : {}",
                    request.getUuid(), e.getMessage(), e);

            throw new RuntimeException("Failed to fetch/process logs", e);
        }
    }
}