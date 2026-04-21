package com.auruspay.logservice.dto;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;

@Component
public class ZabaxRequest {

    @NotBlank(message = "IP is required")
    private String ip;

    @NotBlank(message = "txnId is required")
    private String txn_id;

    @NotBlank(message = "logPath is required")
    private String log_file;

    public ZabaxRequest() {}

    // 🔥 Trim values (safe handling)
    public String getIp() {
        return ip != null ? ip.trim() : null;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTxn_id() {
        return txn_id != null ? txn_id.trim() : null;
    }

    public void setTxn_id(String txn_id) {
        this.txn_id = txn_id;
    }

    public String getLog_file() {
        return log_file != null ? log_file.trim() : null;
    }

    public void setLog_file(String log_file) {
        this.log_file = log_file;
    }

	@Override
	public String toString() {
		return "ZabaxRequest [ip=" + ip + ", txn_id=" + txn_id + ", log_file=" + log_file + "]";
	}

    
}