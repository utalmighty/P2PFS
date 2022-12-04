package com.Huduk.P2PFS.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Service.CountService;

@Controller
public class CountController {
	
	@Autowired
	CountService countService;
	
	@Autowired
	private SimpMessagingTemplate template;

	@MessageMapping("/count")
	@SendTo("/topic/count")
	public Count greeting() throws Exception {
		countService.updateCurrentCount();
		return countService.getCurrentCount();
	}
	
	@MessageMapping("/private")
	public void privateMessage(@Header("simpSessionId") String sessionId, Principal principal) {
		String destination = principal.getName();
		String message = "Utkarsh Jaiswal Message: "+sessionId + " Principal: "+ principal.getName();
		System.out.println("Session Id: "+ sessionId + " Principal Name: " + destination);
		template.convertAndSendToUser(destination, "/queue/send", message);
//		String userQueue = "/user/" + destination + "/queue/send";
//		template.convertAndSend(userQueue, message);
	}
}
