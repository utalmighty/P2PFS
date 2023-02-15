package com.Huduk.P2PFS.Models;

public class User {
	private String sessionId;
	private String principal;
	private RTCDescription description; // WebRTC description

	public User() {
		super();
	}

	public User(String sessionId, String principal, RTCDescription description) {
		super();
		this.sessionId = sessionId;
		this.principal = principal;
		this.description = description;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public RTCDescription getDescription() {
		return description;
	}

	public void setDescription(RTCDescription description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "User [sessionId=" + sessionId + ", principal=" + principal + ", description=" + description + "]";
	}

}
