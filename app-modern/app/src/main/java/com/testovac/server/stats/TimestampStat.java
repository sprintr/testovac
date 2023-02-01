package com.testovac.server.stats;

import java.io.Serializable;

/**
 * Created by Janco1 on 3. 6. 2015.
 */
public class TimestampStat implements Serializable {

	private String timestamp;
	private int[] statistics;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int[] getStatistics() {
		return statistics;
	}

	public void setStatistics(int[] statistics) {
		this.statistics = statistics;
	}
}
