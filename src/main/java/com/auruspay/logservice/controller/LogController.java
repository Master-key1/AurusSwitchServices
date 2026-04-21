package com.auruspay.logservice.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.auruspay.logservice.dto.LogRequest;
import com.auruspay.logservice.dto.ZabaxRequest;
import com.auruspay.logservice.service.LogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    @Autowired
    private ZabaxRequest zabaxRequest  ;
    
    private final LogService logservice;

    public LogController(LogService logservice) {
        this.logservice = logservice;
    }

    // 🔥 Main API
    @PostMapping("/log")
    public ResponseEntity<?> fetchLogs(@Valid @RequestBody LogRequest request) {

        log.info("Received log request: ip={}, txnId={}, logPath={}",
                request.getIp(), request.getTxnId(), request.getLogPath());

        long startTime = System.currentTimeMillis();

        try {
        	 zabaxRequest.setIp(request.getIp());
        	 zabaxRequest.setTxn_id(request.getTxnId());
        	 zabaxRequest.setLog_file(request.getLogPath());
        	 
            Map<String, Object> response = logservice.getLogs(zabaxRequest,request);

            long duration = System.currentTimeMillis() - startTime;

            log.info("Log fetched successfully for txnId={} in {} ms",
                    request.getTxnId(), duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("Error while fetching logs for txnId={} : {}",
                    request.getTxnId(), e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}