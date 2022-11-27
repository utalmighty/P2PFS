package com.Huduk.P2PFS.Models;

/**
 * @author utalm
 *
 */
public class Message {
	
	private String from;
    private String text;
    private String time;
    
    public Message() {}

    public Message(String from, String text, String time) {
		super();
		this.from = from;
		this.text = text;
		this.time = time;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@Override
	public String toString() {
		return "Message [from=" + from + ", text=" + text + ", time=" + time + "]";
	}
	
	
}