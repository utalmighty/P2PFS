package com.Huduk.P2PFS.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import com.Huduk.P2PFS.ExceptionHandler.ExceptionController;
import com.Huduk.P2PFS.Models.Connection;
import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Models.FileMetaData;
import com.Huduk.P2PFS.Models.RTCDescription;

@Service
public class RequestProcessServiceImpl implements RequestProcessService{
	
	private static Logger logger = LoggerFactory.getLogger(RequestProcessServiceImpl.class);
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	CountService countService;
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	ExceptionController exceptionController;
	
	@Override
	public void candidateLogic(Principal principal, LinkedMultiValueMap<String, String> headers, String candidate) {
		// Exchange candidates
		logger.debug("Recevied Candidate: {}", headers);
		String id = getValueElseThrowError(headers, "id");
		String peer = getValueElseThrowError(headers, "peer");
		Connection conn = null;
		try {
			 conn = fileService.getConnectionById(id);
		}catch (Exception e){
			// TODO: handle through ExceptionControllerAdvice
			exceptionController.sendPrivateException(principal.getName(), "Peer went offline now.", "The other peer went offline", true);
			return;
		}
		
		String destination = "";
		switch (peer.toUpperCase()) {
			case "OFFER" :
				if (conn.getPeers().getDestination() != null) 
					destination = conn.getPeers().getDestination().getPrincipal();
				else {
					exceptionController.sendPrivateException(principal.getName(), "Invalid destination peer", "Other peer is not valid", true);
					return;
				}
				break;
			case "ANSWER" :
				if(conn.getPeers().getSource() != null) 
					destination = conn.getPeers().getSource().getPrincipal();
				else {
					exceptionController.sendPrivateException(principal.getName(), "Invalid source peer", "Other peer is not valid", true);
					return;
				}
				break;
			default :
				exceptionController.sendPrivateException(principal.getName(), "You can only be offer/answer", "Type "+ peer+ "is invalid. Only offer/answer are allowed.", true);
				return;
		}
		if (!destination.equals("")) {
			Map<String, String> candidateData = new HashMap<>();
			candidateData.put("icecandidate", candidate);
			template.convertAndSendToUser(destination, "/queue/send", candidateData);
		}
		else {
			exceptionController.sendPrivateException(principal.getName(), "Invalid user", "Other peer is not valid", true);
		}
	}
	
	@Override
	public void offerLogic(String sessionId, LinkedMultiValueMap<String, String> headers, Principal principal, String offerSDP) {
		logger.debug("File meta data: "+ headers);
		String destination = principal.getName();
		RTCDescription desc = new RTCDescription();
		String filename = headers.getFirst("filename");
		long filesize = Long.parseLong(headers.getFirst("filesize"));
		desc.setType("offer");
		desc.setSdp(offerSDP);
		FileMetaData file = new FileMetaData(filename, filesize);
		Map<String, Object> data = fileService.generateUniqueUrl(sessionId, destination, desc, file);
		template.convertAndSendToUser(destination, "/queue/send", data);
	}
	
	@Override
	public void searchLogic(String sessionId, LinkedMultiValueMap<String, String> headers, Principal principal, String anything) {
		String destination = principal.getName();
		String id = getValueElseThrowError(headers, "id");
		Map<String, Object> offerResp = new HashMap<>();
		Connection conn;
		try {
			conn = fileService.getConnectionById(id);
		}
		catch (Exception e) {
			exceptionController.sendPrivateException(destination, "Peer offline", id+" wrong peer or peer offline.", true);
			return;
		}
		offerResp.put("offer", conn.getPeers().getSource().getDescription());
		offerResp.put("filename", conn.getFile().getName());
		offerResp.put("filesize", conn.getFile().getSize());
		template.convertAndSendToUser(destination, "/queue/send", offerResp);
	}
	
	@Override
	public void answerLogic(String sessionId, LinkedMultiValueMap<String, String> headers, Principal principal, String answerSDP) {
		String destination = principal.getName();
		String id = getValueElseThrowError(headers, "id");		
		RTCDescription desc = new RTCDescription();
		desc.setType("answer");
		desc.setSdp(answerSDP);
		Connection conn;
		try {
			fileService.addDestination(sessionId, destination, desc, id);
			conn = fileService.getConnectionById(id);
		}
		catch (Exception e) {
			exceptionController.sendPrivateException(destination, "Peer offline", id+" wrong peer or peer offline.", true);
			return;
		}
		Map<String, Object> answerResp = new HashMap<>();
		answerResp.put("answer", conn.getPeers().getDestination().getDescription());
		template.convertAndSendToUser(conn.getPeers().getSource().getPrincipal(), "/queue/send", answerResp);
	}
	
	private String getValueElseThrowError(LinkedMultiValueMap<String, String> headers, String key) {
		List<String> list = headers.get(key);
		if (Objects.isNull(list)) {
			// Throw error;
			//throw new Exception("Cant find key "+ key);
			return "";
		}
		else return list.get(0);
		
	}

	@Override
	public Count getCurrentCount() {
		return countService.getCurrentCount();
	}

	@Override
	public Count updateAndGetNewCount(LinkedMultiValueMap<String, String> headers) {
		return countService.updateAndGetNewCount(headers);
	}

}
