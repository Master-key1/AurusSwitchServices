package com.auruspay.logservice.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.auruspay.logservice.dto.LogRequest;
import com.auruspay.logservice.dto.ZabaxRequest;
import com.auruspay.logservice.service.LogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/aurus/")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    // 🔥 Host Logs
    @PostMapping("/host/log")  // aurus/host/log
    public ResponseEntity<?> fetchHostLogs(@Valid @RequestBody LogRequest request) {
        return execute("HOST", request);
    }

    // 🔥 ATV Logs
    @PostMapping("/atv/log") // aurus/atv/log
    public ResponseEntity<?> fetchAtvLogs(@Valid @RequestBody LogRequest request) {
        return execute("ATV", request);
    }

    // 🔥 Common method (removes duplicate code)
    private ResponseEntity<?> execute(String team, LogRequest request) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("{} log request: ip={}, txnId={}, logPath={}",
                    team,
                    request.getIp(),
                    request.getTxnId(),
                    request.getLogPath());

            // ✅ Create DTO per request (thread-safe)
            ZabaxRequest zabaxRequest = new ZabaxRequest();
            zabaxRequest.setIp(request.getIp());
            zabaxRequest.setTxn_id(request.getTxnId());
            zabaxRequest.setLog_file(request.getLogPath());

            Map<String, Object> response = logService.getLogs(zabaxRequest, request,team);

            long duration = System.currentTimeMillis() - startTime;

            log.info("{} log success txnId={} in {} ms",
            		team, request.getTxnId(), duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("{} log failed txnId={} : {}",
            		team, request.getTxnId(), e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    ));
        }
    }
}