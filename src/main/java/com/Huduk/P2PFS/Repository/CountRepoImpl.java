package com.Huduk.P2PFS.Repository;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CountRepoImpl implements CountRepo {
	
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
		// watch out for Race Condition
		synchronized(this)  { 
			this.localCount += count;
		}
	}
	
	private void updateInDatabase() {
		System.out.println("Syncing count with db");
		String time = LocalDateTime.now().toString();
		time.replace("T", " ");
		String sql = "UPDATE \"" + schema + "\"." + table +" "
				+ "SET count=((select count from \"" + schema + "\"." + table +" c where c.\"Name\"='"+ counterId +"')+"+ localCount +"), \"timestamp\"='"+ time +"'\r\n"
				+ "WHERE \"Name\"='"+ counterId +"';";
		try {
			int rowsEffected = template.update(sql);
			if (rowsEffected == 1) {
				//Only one row should be effected
				synchronized(this)  {
					this.localCount = 0;
				}
				System.out.println("Count updated with db");
				this.dbCount = getCountFromDB();
			}
		}catch (Exception e) {
			System.err.println("Database Exception while upodating => " + e.getLocalizedMessage() + "Count value is still: "+ localCount);
		}
	}
	
	private long getCountFromDB() {
		System.out.println("Fetching value from db");
		try {
			String sql = "SELECT count FROM \""+ schema +"\"." + table +" WHERE \"Name\"='" + counterId + "';";
			Map<String, Object> record = template.queryForMap(sql);
			return (long) record.get("count");
		}catch (Exception e) {
			System.err.println("Database Exception while syncing => " + e.getLocalizedMessage());
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
		if (this.localCount > 0 && this.localCount%persistAfter == 0) {
			// Eventual Consistency ie after every 'persistAfter' database value will be updated
			// TODO: Also persist value after some interval.
			updateInDatabase();
		}
	}
}
