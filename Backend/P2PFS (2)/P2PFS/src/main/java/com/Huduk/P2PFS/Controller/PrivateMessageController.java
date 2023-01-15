package com.Huduk.P2PFS.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.Huduk.P2PFS.Models.Candidate;
import com.Huduk.P2PFS.Models.Connection;
import com.Huduk.P2PFS.Models.FileMetaData;
import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.RTCDescription;
import com.Huduk.P2PFS.Service.FileService;

@RestController
public class PrivateMessageController {
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private FileService fileService;
	
	@MessageMapping("/candidate")
	public void candidateLogic(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String candidate) throws Exception{
		System.out.println("Recevied Candidate: "+headers);
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
		System.out.println("File info: "+ headers);
		String filename = headers.getFirst("filename");
		long filesize = Long.parseLong(headers.getFirst("filesize"));
		System.out.println("File: "+ filename + " "+ filesize);
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
		System.out.println("Recevied search request. Key: " +  id);
		
		Connection conn = fileService.getConnectionById(id);
		
		System.out.println("Sending Offer to destination");
		Map<String, Object> offerResp = new HashMap<>();
		offerResp.put("offer", conn.getPeers().getSource().getDescription());
		offerResp.put("filename", conn.getFile().getName());
		offerResp.put("filesize", conn.getFile().getSize());
		template.convertAndSendToUser(destination, "/queue/send", offerResp);
	}
	
	@MessageMapping("/answer")
	public void sendAnswer(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String answerSDP) throws Exception {
		
		String destination = principal.getName();
		String id = headers.get("id").get(0);
		System.out.println("Key received: "+ id);
		
		RTCDescription desc = new RTCDescription();
		desc.setType("answer");
		desc.setSdp(answerSDP);
		
		fileService.addDestination(sessionId, destination, desc, id);
		Connection conn = fileService.getConnectionById(id);
		
		System.out.println("Sending Answer to source");
		Map<String, Object> answerResp = new HashMap<>();
		answerResp.put("answer", conn.getPeers().getDestination().getDescription());
		template.convertAndSendToUser(conn.getPeers().getSource().getPrincipal(), "/queue/send", answerResp);
	}
	
	
	@MessageMapping("/private")
	public void privateMessage(@Header("simpSessionId") String sessionId, Principal principal, 
			 String payload) {
		System.out.println("Received>>" + payload);
		String destination = principal.getName();
		String message = "Session Id: "+ sessionId + " Principal Name: "+ destination;
		System.out.println(message);
		template.convertAndSendToUser(destination, "/queue/send", message);
	}
}
