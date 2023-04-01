package com.Huduk.P2PFS.Service;

import org.springframework.util.LinkedMultiValueMap;

import com.Huduk.P2PFS.Models.Count;

public interface CountService {
	
	public Count getCurrentCount();
	public Count updateAndGetNewCount(LinkedMultiValueMap<String, String> updateRequest);

}
