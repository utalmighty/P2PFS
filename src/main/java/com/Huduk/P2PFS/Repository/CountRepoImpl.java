package com.Huduk.P2PFS.Repository;

import org.springframework.stereotype.Repository;

@Repository
public class CountRepoImpl implements CountRepo {
	
	private long count;
	
	private void incrementCurrentCount(long count) {
		this.count += count;
	}
	
	@Override
	public long getCurrentCount() {
		return count;
	}

	@Override
	public void incrementCurrentCount() {
		incrementCurrentCount(1);
	}

}
