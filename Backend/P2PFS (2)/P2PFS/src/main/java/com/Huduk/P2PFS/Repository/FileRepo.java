package com.Huduk.P2PFS.Repository;

import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.User;

public interface FileRepo {
	
	public String setSource(User source);
	public boolean isValidId(String id);
	public Peers setDestination(String id, User destination) throws Exception;
	public Peers getPeersById(String id) throws Exception;
}
