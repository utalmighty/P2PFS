package com.Huduk.P2PFS.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Repository;

import com.Huduk.P2PFS.Models.Connection;
import com.Huduk.P2PFS.Models.FileMetaData;
import com.Huduk.P2PFS.Models.Peers;
import com.Huduk.P2PFS.Models.User;

@Repository
public class FileRepoImpl implements FileRepo {
	
	private static final String SPACE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
										//+"abcdefghijklmnopqrstuvwxyz"
										//+"01234567890"
										//+"@#$!*+-_?. "
										;
	
	private Random random = new Random();
	
	private Map<String, Connection> database = new HashMap<>();
	
	
	@Override
	public String setSource(User source, FileMetaData file) {
		// TODO Auto-generated method stub
		int size = 12;
		String url = generateRandom(size);
		while (database.containsKey(url)) {
			url = generateRandom(size);
		}
		Peers peers = new Peers();
		peers.setSource(source);
		Connection conn = new Connection(file, peers);
		database.put(url, conn);
		return url;
	}
	
	
		
	private String generateRandom(int length) {
		StringBuilder sb = new StringBuilder(length);
		for( int i=0; i<=length; i++ ) {
			int index = random.nextInt(SPACE.length());
			sb.append(SPACE.charAt(index));
		}
		return sb.toString();
	}

	@Override
	public boolean isValidId(String id) {
		return database.containsKey(id);
	}

	@Override
	public Connection getConnectionById(String id) throws Exception {
		if (database.containsKey(id)) {
			Connection conn = database.get(id);
			return conn;
		}
		throw new Exception("No such session in database by getPeersById()");
	}

	@Override
	public Peers setDestination(String id, User destination) throws Exception {
		if (database.containsKey(id)) {
			Peers peer = database.get(id).getPeers();
			peer.setDestination(destination);
			return peer;
		}
		throw new Exception("No such peer in database");
	}

}
