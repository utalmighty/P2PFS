package com.Huduk.P2PFS.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RestController;

import com.Huduk.P2PFS.ExceptionHandler.ExceptionController;
import com.Huduk.P2PFS.Models.Connection;
import com.Huduk.P2PFS.Models.FileMetaData;
import com.Huduk.P2PFS.Models.RTCDescription;
import com.Huduk.P2PFS.Service.FileService;

@RestController
public class PrivateMessageController {

	private static Logger logger = LoggerFactory.getLogger(PrivateMessageController.class);
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private FileService fileService;
	
	@MessageMapping("/candidate")
	public void candidateLogic(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String candidate) throws Exception{
		logger.info("Recevied Candidate: {}", headers);
		String id = headers.get("id").get(0);
		String peer = headers.get("peer").get(0);
		Connection conn = fileService.getConnectionById(id);
		String destination = "";
		if (peer.equalsIgnoreCase("offer") && conn.getPeers().getDestination() != null) {
			destination = conn.getPeers().getDestination().getPrincipal();
		}
		else if(peer.equalsIgnoreCase("answer") && conn.getPeers().getSource() != null) {
			destination = conn.getPeers().getSource().getPrincipal();
		}
		if (!destination.equals("")) {
			Map<String, String> candidateData = new HashMap<>();
			candidateData.put("icecandidate", candidate);
			template.convertAndSendToUser(destination, "/queue/send", candidateData);
		}
	}
	
	@MessageMapping("/offer")
	public void generateUrl(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String offerSDP) {
		String destination = principal.getName();
		RTCDescription desc = new RTCDescription();
		logger.info("File info: "+ headers);
		String filename = headers.getFirst("filename");
		long filesize = Long.parseLong(headers.getFirst("filesize"));
		desc.setType("offer");
		desc.setSdp(offerSDP);
		FileMetaData file = new FileMetaData(filename, filesize);
		Map<String, Object> data = fileService.generateUniqueUrl(sessionId, destination, desc, file);
		template.convertAndSendToUser(destination, "/queue/send", data);
	}
	
	@MessageMapping("/search")
	public void requestId(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String anything) throws Exception {
		
		String destination = principal.getName();
		String id = headers.get("id").get(0);
		logger.info("Recevied search request. Key: " +  id);
		Map<String, Object> offerResp = new HashMap<>();
		Connection conn = null;
		try {
			conn = fileService.getConnectionById(id);
		}
		catch (Exception e) {
			logger.info(e.getLocalizedMessage());
		}
		logger.info("Sending Offer to destination");
		if (conn != null) {
			offerResp.put("offer", conn.getPeers().getSource().getDescription());
			offerResp.put("filename", conn.getFile().getName());
			offerResp.put("filesize", conn.getFile().getSize());
			template.convertAndSendToUser(destination, "/queue/send", offerResp);
		}
		else {
			ExceptionController.sendPrivateException(destination, "Peer offline", id+"Wrong peer or peer offline.", true);
		}
	}
	
	@MessageMapping("/answer")
	public void sendAnswer(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String answerSDP) throws Exception {
		
		String destination = principal.getName();
		String id = headers.get("id").get(0);
		logger.info("Key received: "+ id);
		
		RTCDescription desc = new RTCDescription();
		desc.setType("answer");
		desc.setSdp(answerSDP);
		
		fileService.addDestination(sessionId, destination, desc, id);
		Connection conn = fileService.getConnectionById(id);
		
		logger.info("Sending Answer to source");
		Map<String, Object> answerResp = new HashMap<>();
		answerResp.put("answer", conn.getPeers().getDestination().getDescription());
		template.convertAndSendToUser(conn.getPeers().getSource().getPrincipal(), "/queue/send", answerResp);
	}
	
	
	@MessageMapping("/private")
	public void privateMessage(@Header("simpSessionId") String sessionId, Principal principal, 
			 String payload) {
		logger.info("Received>>" + payload);
		String destination = principal.getName();
		String message = "Session Id: "+ sessionId + " Principal Name: "+ destination;
		logger.info(message);
		template.convertAndSendToUser(destination, "/queue/send", message);
	}
}
