package com.Huduk.P2PFS.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Repository.CountRepo;

@Service
public class CountServiceImpl implements CountService {
	
	@Autowired
	CountRepo countRepo;
	
	@Override
	public Count getCurrentCount() {
		long currentCount = countRepo.getCurrentCount();
		LocalDateTime timestamp = LocalDateTime.now();
		
		Count count = new Count.Builder()
				.withCount(currentCount)
				.withTimestamp(timestamp)
				.build();
		return count;
	}

	@Override
	public void updateCurrentCount() {
		countRepo.incrementCurrentCount();
	}

}
