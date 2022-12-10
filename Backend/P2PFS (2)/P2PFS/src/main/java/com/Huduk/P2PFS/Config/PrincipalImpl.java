package com.Huduk.P2PFS.Config;

import java.security.Principal;
import java.util.Objects;

public class PrincipalImpl implements Principal {
	
	private String user;
	
	public PrincipalImpl(String username) {
		user = username;
	}
	
	@Override
	public String getName() {
		return user;
	}

	@Override
	public int hashCode() {
		return user.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrincipalImpl other = (PrincipalImpl) obj;
		return user.equals(other.getName());
	}
	
	
}
