package com.Huduk.P2PFS.Models;

public class Peers {

	private User source;
	private User destination;

	public Peers() {
		super();
	}

	public Peers(User source, User destination) {
		super();
		this.source = source;
		this.destination = destination;
	}

	public User getSource() {
		return source;
	}

	public void setSource(User source) {
		this.source = source;
	}

	public User getDestination() {
		return destination;
	}

	public void setDestination(User destination) {
		this.destination = destination;
	}

	@Override
	public String toString() {
		return "Connection [source=" + source + ", destination=" + destination + "]";
	}

}
