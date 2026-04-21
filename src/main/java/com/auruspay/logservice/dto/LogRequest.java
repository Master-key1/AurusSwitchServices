package com.auruspay.logservice.dto;
import jakarta.validation.constraints.NotBlank;

public class LogRequest {
	
	 @NotBlank(message = "IP is required")
	 private String ip;
	 @NotBlank(message = "txnid is required")
	 private String txnId;
	 @NotBlank(message = "UUID is required")
	private String uuid;
	 @NotBlank(message = "logpath is required")
	private String logPath;
	 
	   
	
	 public LogRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	 
	 // getters & setters
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getLogPath() {
		return logPath;
	}
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	@Override
	public String toString() {
		return "LogRequest [ip=" + ip + ", uuid=" + uuid + ", logPath=" + logPath + "]";
	}
	
	
	
	

}




   
