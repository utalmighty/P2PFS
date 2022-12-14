package com.Huduk.P2PFS.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.RTCDescription;
import com.Huduk.P2PFS.Models.User;
import com.Huduk.P2PFS.Repository.FileRepo;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	FileRepo fileRepo;
	
	@Override
	public Map<String, Object> generateUniqueUrl(String sessionId, String destination, RTCDescription description) {
		Map<String, Object> resp = new HashMap<>();
		User sourceUser = new User(sessionId, destination, description);
		String urlData = fileRepo.setSource(sourceUser);
		resp.put("message", "Offer Created");
		resp.put("id", urlData);
		return resp;
	}

	@Override
	public Peers getPeersById(String id) throws Exception {
		try {
			return fileRepo.getPeersById(id);
		}
		catch(Exception e) {
			throw new Exception("Peer not available or offline");
		}
	}

	@Override
	public void addDestination(String sessionId, String destination, RTCDescription description, String id) throws Exception{
		User user = new User(sessionId, destination, description);
		try {
			fileRepo.setDestination(id, user);
		} catch (Exception e) {
			throw new Exception("Peer not available or offline in addDestination");
		}
	}

}
