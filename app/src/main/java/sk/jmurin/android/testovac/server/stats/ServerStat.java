package sk.jmurin.android.testovac.server.stats;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sk.jmurin.android.testovac.ObrazokGenerator;

/**
 * Created by Janco1 on 3. 6. 2015.
 */
public class ServerStat implements Serializable {

    private String hostname;
    private List<TimestampStat> timestampStats = new ArrayList<>();

    public ServerStat() {
        super();
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<TimestampStat> getTimestampStats() {
        return timestampStats;
    }

    public void setStats(List<String> stats) {
        for (String zaznam : stats) {
            TimestampStat ts = new TimestampStat();
            ts.setTimestamp(zaznam.split("_")[0]);
            zaznam = zaznam.split("_")[1];
            zaznam = zaznam.substring(1, zaznam.length() - 1);
            //System.out.println("zaznam: "+zaznam);
            String[] zlozky = zaznam.split(",");
            int[] statistics = new int[1501];
            if (zlozky.length != 1501) {
                throw new RuntimeException("ServerStat.setStats: zlozky.length!=1501");
            }
            for (int i = 0; i < statistics.length; i++) {
                statistics[i] = Integer.parseInt(zlozky[i].trim());
            }
            ts.setStatistics(statistics);
            timestampStats.add(ts);
        }
        // vygenerujeme obrazky a ulozime ich bokom
        ServerStatsObrazkyResource.hostnames.put(hostname, new ArrayList<Bitmap>());
        for (int i = 0; i < timestampStats.size(); i++) {
            ServerStatsObrazkyResource.hostnames.get(hostname).add(ObrazokGenerator.getDefaultObrazok(timestampStats.get(i).getStatistics()));
        }
    }
}
