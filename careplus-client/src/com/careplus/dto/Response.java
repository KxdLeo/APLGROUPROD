package com.careplus.dto;
import java.io.Serializable;
import java.util.UUID;

public class Response implements Serializable {


	private static final long serialVersionUID = 1L;
	
	private String correlationId;
	private String status;
	private String message;
	private Object payload;
	private long timestamp;
	
	public Response() {
		this.correlationId =  UUID.randomUUID().toString();
		this.status ="";
		this.message = "";
		this.payload = null;
		this.timestamp = System.currentTimeMillis();
			
	}
	 public String getCorrelationId() { return correlationId; }
	    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

	    public String getStatus() { return status; }
	    public void setStatus(String status) { this.status = status; }

	    public String getMessage() { return message; }
	    public void setMessage(String message) { this.message = message; }

	    public Object getPayload() { return payload; }
	    public void setPayload(Object payload) { this.payload = payload; }

	    public long getTimestamp() { return timestamp; }
	    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	

}
