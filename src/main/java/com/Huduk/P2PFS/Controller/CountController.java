package com.Huduk.P2PFS.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;

import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Service.RequestProcessService;

@Controller
public class CountController {
	
	@Autowired
	RequestProcessService requestService;

	@MessageMapping("/count")
	@SendTo("/topic/count")
	public Count greeting() throws Exception {
		return requestService.getCurrentCount();
	}
	
	@MessageMapping("/updateCount/increment")
	@SendTo("/topic/count")
	public Count updateCountAndGetNewCount(@Header("nativeHeaders") LinkedMultiValueMap<String, String> headers) {
		return requestService.updateAndGetNewCount(headers);
	}
}
