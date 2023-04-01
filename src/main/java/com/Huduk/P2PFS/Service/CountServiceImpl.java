package com.Huduk.P2PFS.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import com.Huduk.P2PFS.ExceptionHandler.ExceptionController;
import com.Huduk.P2PFS.Models.Count;
import com.Huduk.P2PFS.Repository.CountRepo;


@Service
public class CountServiceImpl implements CountService {
	
	private static Logger logger = LoggerFactory.getLogger(CountServiceImpl.class);
	
	@Autowired
	CountRepo countRepo;
	
	@Autowired
	FileService fileService;
	
	@Override
	public Count getCurrentCount() {
		long currentCount = 0;
		try {
			currentCount = countRepo.getCurrentCount();
		}catch(DataAccessException e) {
			logger.error("Database Exception while fetching current count: {}", e.getLocalizedMessage());
		}
		return getCountObject(currentCount);
	}


	@Override
	public Count updateAndGetNewCount(LinkedMultiValueMap<String, String> updateRequest) {
		List<String> requestData = updateRequest.get("id");
		if (requestData != null) {
			String id = requestData.get(0);
			if (fileService.isValidCountIncrementRequest(id))
				try {
					countRepo.incrementCurrentCount();
				}catch(DataAccessException e) {
					logger.error("Database Exception while updating count: {}", e.getLocalizedMessage());
					return getCountObject(0);
				}
			else {
				ExceptionController.sendPrivateException(id, "Invalid id to update count request.", "", false);
			}
		}
		else {
			logger.debug("Unable to fetch id from request for update count request.");
		}
		return getCurrentCount();
	}
	
	private Count getCountObject(long count) {
		LocalDateTime timestamp = LocalDateTime.now();
				
		Count countResp = new Count.Builder()
				.withCount(count)
				.withTimestamp(timestamp)
				.build();
		return countResp;
	}
		
}
