package com.Huduk.P2PFS.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Service.CountService;

@Controller
public class CountController {
	
	@Autowired
	CountService countService;

	@MessageMapping("/count")
	@SendTo("/topic/count")
	public Count greeting() throws Exception {
		countService.updateCurrentCount();
		return countService.getCurrentCount();
	}
}
