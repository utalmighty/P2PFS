package com.Huduk.P2PFS.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;

import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Service.CountService;
import com.Huduk.P2PFS.Service.FileService;

import jakarta.websocket.server.PathParam;

@Controller
public class CountController {
	
	@Autowired
	CountService countService;
	
	@Autowired
	private FileService fileService;

	@MessageMapping("/count")
	@SendTo("/topic/count")
	public Count greeting() throws Exception {
		return countService.getCurrentCount();
	}
	
	@MessageMapping("/updateCount/increment")
	@SendTo("/topic/count")
	public Count updateCount(@Header("nativeHeaders") LinkedMultiValueMap<String, String> headers) {
		String id = headers.get("id").get(0);
		if (fileService.isValidCountIncrementRequest(id)) {
			countService.updateCurrentCount();
		}
		return countService.getCurrentCount();
	}
}
