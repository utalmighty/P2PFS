package com.Huduk.P2PFS.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;

import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.RTCDescription;
import com.Huduk.P2PFS.Service.FileService;

@Controller
public class PrivateMessageController {
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private FileService fileService;
	
	
	@MessageMapping("/offer")
	public void generateUrl(@Header("simpSessionId") String sessionId, Principal principal, String offerSDP) {
		String destination = principal.getName();
		RTCDescription desc = new RTCDescription();
		desc.setType("offer");
		desc.setSdp(offerSDP);
		Map<String, Object> data = fileService.generateUniqueUrl(sessionId, destination, desc);
		template.convertAndSendToUser(destination, "/queue/send", data);
	}
	
	@MessageMapping("/search")
	public void requestId(@Header("simpSessionId") String sessionId, @Header("nativeHeaders") LinkedMultiValueMap<String, String> headers, Principal principal, String anything) throws Exception {
		
		String destination = principal.getName();
		String id = headers.get("id").get(0);
		System.out.println("Recevied search request. Key: " +  id);
		
		Peers peers = fileService.getPeersById(id);
		
		System.out.println("Sending Offer to destination");
		Map<String, Object> offerResp = new HashMap<>();
		offerResp.put("offer", peers.getSource().getDescription());
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
		Peers peers = fileService.getPeersById(id);
		
		System.out.println("Sending Answer to source");
		Map<String, Object> answerResp = new HashMap<>();
		answerResp.put("answer", peers.getDestination().getDescription());
		template.convertAndSendToUser(peers.getSource().getPrincipal(), "/queue/send", answerResp);
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
