package com.careplus.dto;

import java.io.Serializable;
import java.util.UUID;

import com.careplus.network.RequestType;

public class Request implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	private String correlationId;
	private String sessionToken;
	private RequestType actionType;
	private Object payload;
	
	public Request() {
		this.correlationId = UUID.randomUUID().toString();
		this.sessionToken = "";
		
	}
	
	public Request(RequestType actionType, Object payload) {
		this.correlationId = UUID.randomUUID().toString();
		this.sessionToken = "";
		this.actionType = actionType;
		this.payload = payload;
		
	}

	public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public RequestType getActionType() { return actionType; }
    public void setActionType(RequestType actionType) { this.actionType = actionType; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
	
	
	
	
}
