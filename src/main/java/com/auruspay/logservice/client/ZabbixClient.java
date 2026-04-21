package com.auruspay.logservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.auruspay.logservice.config.ZabbixConfig;
import com.auruspay.logservice.dto.LogRequest;
import com.auruspay.logservice.dto.ZabaxRequest;

@Component
public class ZabbixClient {

    private static final Logger log = LoggerFactory.getLogger(ZabbixClient.class);

    private final RestTemplate restTemplate;
    private final ZabbixConfig config;

    public ZabbixClient(RestTemplate restTemplate, ZabbixConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public String fetchLogs(ZabaxRequest zabaxRequest ,LogRequest request) {

        String fullUrl = buildUrl();

        try {
            log.info("Calling Zabbix API: {}", fullUrl);
            log.info("Request payload: {}", zabaxRequest);

            String response = restTemplate.postForObject(fullUrl, zabaxRequest, String.class);

            log.info("Received response from Zabbix (length={})",
                    response != null ? response.length() : 0);

            return response;

        } catch (RestClientException e) {
            log.error("Error calling Zabbix API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch logs from Zabbix", e);
        }
    }

    // 🔥 Clean URL builder
    private String buildUrl() {
        return config.getUrl() + ":" + config.getPort() + config.getEndpoint();
    }
}