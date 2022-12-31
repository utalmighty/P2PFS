package com.Huduk.P2PFS.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Repository;

import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.User;

@Repository
public class FileRepoImpl implements FileRepo {
	
	private static final String RANGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
										+"abcdefghijklmnopqrstuvwxyz"
										+"01234567890"
										//+"@#$!*+-_?. "
										;
	
	private Random random = new Random();
	
	private Map<String, Peers> database = new HashMap<>();
	
	
	@Override
	public String setSource(User source) {
		// TODO Auto-generated method stub
		String url = generateRandom(7);
		while (database.containsKey(url)) {
			url = generateRandom(7);
		}
		Peers peers = new Peers();
		peers.setSource(source);
		database.put(url, peers);
		return url;
	}
	
	
		
	private String generateRandom(int length) {
		StringBuilder sb = new StringBuilder(length);
		for( int i=0; i<=length; i++ ) {
			int index = random.nextInt(RANGE.length());
			sb.append(RANGE.charAt(index));
		}
		return sb.toString();
	}

	@Override
	public boolean isValidId(String id) {
		return database.containsKey(id);
	}

	@Override
	public Peers getPeersById(String id) throws Exception {
		if (database.containsKey(id)) {
			Peers peer = database.get(id);
			return peer;
		}
		throw new Exception("No such session in database by getPeersById()");
	}

	@Override
	public Peers setDestination(String id, User destination) throws Exception {
		if (database.containsKey(id)) {
			Peers peer = database.get(id);
			peer.setDestination(destination);
			return peer;
		}
		throw new Exception("No such session in database");
	}

}
