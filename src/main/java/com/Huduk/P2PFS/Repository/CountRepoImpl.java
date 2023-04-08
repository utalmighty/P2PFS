package com.Huduk.P2PFS.Repository;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CountRepoImpl implements CountRepo {

	private static Logger logger = LoggerFactory.getLogger(CountRepoImpl.class);
	
	@Value("${persistCount}")
	private int persistAfter;
	
	@Value("${schema}")
	private String schema;
	
	@Value("${counterId}")
	private String counterId;
	
	@Value("${tableName}")
	private String table;
	
	@Autowired
	JdbcTemplate template;
	
	private long dbCount;
	
	private long localCount;
	
	private void incrementCurrentCount(long count) {
		synchronized(this)  { 
			localCount += count;
		}
	}
	
	private void updateInDatabase() {
		logger.debug("Syncing count with db");
		String time = LocalDateTime.now().toString();
		time.replace("T", " ");
		String sql = "UPDATE \"" + schema + "\"." + table +" "
				+ "SET count=((select count from \"" + schema + "\"." + table +" c where c.\"Name\"='"+ counterId +"')+"+ localCount +"), \"timestamp\"='"+ time +"'\r\n"
				+ "WHERE \"Name\"='"+ counterId +"';";
		try {
			int rowsEffected = template.update(sql);
			if (rowsEffected == 1) {
				synchronized(this)  {
					localCount = 0;
				}
				logger.debug("Count updated in database");
				dbCount = getCountFromDB();
			}
		}catch (Exception e) {
			logger.error("Database Exception while updating the count: {}, {} {}" + e.getLocalizedMessage(), "Count value is still:", localCount);
		}
	}
	
	private long getCountFromDB() {
		logger.debug("Fetching value from database");
		try {
			String sql = "SELECT count FROM \""+ schema +"\"." + table +" WHERE \"Name\"='" + counterId + "';";
			Map<String, Object> record = template.queryForMap(sql);
			return (long) record.get("count");
		}catch (Exception e) {
			logger.error("Database Exception while getting count: {}", e.getLocalizedMessage());
			return 0;
		}
	}
	
	
	@Override
	public long getCurrentCount() {
		if (dbCount == 0) {
			dbCount = getCountFromDB();
			return (dbCount>localCount) ? dbCount : localCount;
		}
		return dbCount + localCount;
	}

	@Override
	public void incrementCurrentCount() {
		incrementCurrentCount(1);
		if (localCount > 0 && localCount%persistAfter == 0) {
			// Eventual Consistency ie after every 'persistAfter' database value will be updated
			// TODO: Also persist value after some interval.
			updateInDatabase();
		}
	}
}
