package com.Huduk.P2PFS.Config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
	
	private static Logger logger = LoggerFactory.getLogger(UserHandshakeHandler.class);


	@Override
	protected Principal determineUser(ServerHttpRequest request,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {
		String randomId = UUID.randomUUID().toString();
		logger.error(randomId);
		return new PrincipalImpl(randomId);
	}
	
}
