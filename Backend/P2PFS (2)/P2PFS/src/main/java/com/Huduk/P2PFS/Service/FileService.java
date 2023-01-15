package com.Huduk.P2PFS.Service;

import java.util.Map;

import com.Huduk.P2PFS.Models.Connection;
import com.Huduk.P2PFS.Models.FileMetaData;
import com.Huduk.P2PFS.Models.RTCDescription;

public interface FileService {
	
	public Map<String, Object> generateUniqueUrl(String sessionId, String destination, RTCDescription description, FileMetaData file);
	public Connection getConnectionById(String id) throws Exception;
	void addDestination(String sessionId, String destination, RTCDescription description, String id) throws Exception;
}
