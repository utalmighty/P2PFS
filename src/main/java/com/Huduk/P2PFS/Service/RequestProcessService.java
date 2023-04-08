package com.Huduk.P2PFS.Service;

import java.security.Principal;

import org.springframework.util.LinkedMultiValueMap;

import com.Huduk.P2PFS.Models.Count;

public interface RequestProcessService {
	
	public void candidateLogic(Principal principal, LinkedMultiValueMap<String, String> headers, String candidate);
	public void offerLogic(String sessionId, LinkedMultiValueMap<String, String> headers, Principal principal, String offerSDP);
	public void searchLogic(String sessionId, LinkedMultiValueMap<String, String> headers, Principal principal, String anything);
	public void answerLogic(String sessionId, LinkedMultiValueMap<String, String> headers, Principal principal, String answerSDP);
	
	public Count getCurrentCount();
	public Count updateAndGetNewCount(LinkedMultiValueMap<String, String> headers);

}
