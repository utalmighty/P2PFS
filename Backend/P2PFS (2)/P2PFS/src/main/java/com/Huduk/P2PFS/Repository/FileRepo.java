package com.Huduk.P2PFS.Repository;

import com.Huduk.P2PFS.Models.Connection;
import com.Huduk.P2PFS.Models.FileMetaData;
import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.User;

public interface FileRepo {
	
	public String setSource(User source, FileMetaData file);
	public boolean isValidId(String id);
	public Peers setDestination(String id, User destination) throws Exception;
	public Connection getConnectionById(String id) throws Exception;
	public void deleteId(String id);
}
