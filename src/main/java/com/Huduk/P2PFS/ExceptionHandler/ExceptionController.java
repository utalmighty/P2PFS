package com.Huduk.P2PFS.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.Huduk.P2PFS.Models.Error;

public class ExceptionController {
	
	@Autowired
	private static SimpMessagingTemplate template;
	
	public static void sendPrivateException(Error error) {
		Map<String, Object> errorResp = new HashMap<>();
		errorResp.put("error", error.getMessage());
		errorResp.put("description", error.getDescription());
		errorResp.put("alert", error.isAlertUser());
		template.convertAndSendToUser(error.getUser(), "/queue/send", errorResp);
	}
	
	public static void sendPrivateException(String user, String errorMessage, String description, boolean alertUser) {
		Error error = new Error();
		error.setUser(user);
		error.setMessage(errorMessage);
		error.setDescription(description);
		error.setAlertUser(alertUser);
		sendPrivateException(error);
	}
	
	@SendTo("/topic/error")
	public static void sendPublicException(Error error) {
		Map<String, Object> errorResp = new HashMap<>();
		errorResp.put("error", error.getMessage());
		errorResp.put("description", error.getDescription());
	}

}
