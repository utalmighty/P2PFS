package com.Huduk.P2PFS.Config;

import java.security.Principal;
import java.util.UUID;

public class PrincipalImpl implements Principal {
	
	private String user;
	
	public PrincipalImpl(String username) {
		user = username;
	}
	
	@Override
	public String getName() {
		return user;
	}

}
