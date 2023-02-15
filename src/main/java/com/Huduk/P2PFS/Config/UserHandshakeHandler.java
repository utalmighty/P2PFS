package com.Huduk.P2PFS.Config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler{

	@Override
	protected Principal determineUser(org.springframework.http.server.ServerHttpRequest request,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {
		String randomId = UUID.randomUUID().toString();
		return new PrincipalImpl(randomId);
	}
	
}
