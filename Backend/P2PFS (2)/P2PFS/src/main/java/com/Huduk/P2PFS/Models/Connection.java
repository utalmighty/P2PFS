package com.Huduk.P2PFS.Models;

public class Connection {
	private FileMetaData file;
	private Peers peers;
	public Connection(FileMetaData file, Peers peers) {
		super();
		this.file = file;
		this.peers = peers;
	}
	public FileMetaData getFile() {
		return file;
	}
	public void setFile(FileMetaData file) {
		this.file = file;
	}
	public Peers getPeers() {
		return peers;
	}
	public Connection() {
		super();
	}
	public void setPeers(Peers peers) {
		this.peers = peers;
	}
	
	
}
