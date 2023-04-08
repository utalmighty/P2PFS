package com.Huduk.P2PFS.Models;

public class Error {
	private String user;
	private String message;
	private String description = "";
	private boolean alertUser = false;
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isAlertUser() {
		return alertUser;
	}
	public void setAlertUser(boolean alertUser) {
		this.alertUser = alertUser;
	}
}
