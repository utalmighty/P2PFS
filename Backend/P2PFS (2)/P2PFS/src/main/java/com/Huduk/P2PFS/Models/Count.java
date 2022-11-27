package com.Huduk.P2PFS.Models;

import java.time.LocalDateTime;

public class Count {
	
	private long count;
	private LocalDateTime timestamp;
	
	public long getCount() {
		return count;
	}
	private void setCount(long count) {
		this.count = count;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	private void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "Count [count=" + count + ", timestamp=" + timestamp + "]";
	}
	
	public static class Builder {
		private long count;
		private LocalDateTime timestamp;
		private Count object;
		
		public Builder withCount(long count) {
			this.count = count;
			return this;
		}
		public Builder withTimestamp(LocalDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}
		
		public Count build() {
			object = new Count();
			object.setCount(count);
			object.setTimestamp(timestamp);
			return object;
		}
		
		public Count getObject() {
			return object;
		}
		
	}
	
}
