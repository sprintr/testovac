package sk.jmurin.android.testovac.server.stats;

import android.graphics.Bitmap;

import java.io.Serializable;

import sk.jmurin.android.testovac.ObrazokGenerator;

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
