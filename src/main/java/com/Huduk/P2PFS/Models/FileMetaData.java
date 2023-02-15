package com.Huduk.P2PFS.Models;

public class FileMetaData {
	private String name;
	private long size;
	private String hash;
	
	public FileMetaData() {
		super();
	}
	public FileMetaData(String name, long size) {
		super();
		this.name = name;
		this.size = size;
	}
	public FileMetaData(String name, long size, String hash) {
		super();
		this.name = name;
		this.size = size;
		this.hash = hash;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
}
