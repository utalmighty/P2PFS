package com.Huduk.P2PFS.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RestController;

import com.Huduk.P2PFS.Service.RequestProcessService;

@RestController
public class PrivateMessageController {
	
	@Autowired
	RequestProcessService requestService;
	
	@MessageMapping("/candidate")
	public void candidateLogic(@Header("simpSessionId") String sessionId,
			@Header("nativeHeaders") LinkedMultiValueMap<String, String> headers,
			Principal principal, String candidate) {
		
		requestService.candidateLogic(principal, headers, candidate);
	}
	
	@MessageMapping("/search")
	public void requestId(@Header("simpSessionId") String sessionId,
			@Header("nativeHeaders") LinkedMultiValueMap<String, String> headers,
			Principal principal, String anything) {
		
		requestService.searchLogic(sessionId, headers, principal, anything);
	}
	
	@MessageMapping("/offer")
	public void generateUrl(@Header("simpSessionId") String sessionId,
			@Header("nativeHeaders") LinkedMultiValueMap<String, String> headers,
			Principal principal, String offerSDP) {
		
		requestService.offerLogic(sessionId, headers, principal, offerSDP);
	}
	
	@MessageMapping("/answer")
	public void sendAnswer(@Header("simpSessionId") String sessionId,
			@Header("nativeHeaders") LinkedMultiValueMap<String, String> headers,
			Principal principal, String answerSDP) {
		
		requestService.answerLogic(sessionId, headers, principal, answerSDP);
	}
	
}
