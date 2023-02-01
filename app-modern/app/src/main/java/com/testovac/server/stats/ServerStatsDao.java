package com.testovac.server.stats;

import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ServerStatsDao {
	public static final String DEFAULT_SERVICE_URL = "http://www.jmurin.sk/testovac/get-stats.php";

	private URL serviceUrl;

	public ServerStatsDao() {
		try {
			this.serviceUrl = new URL(DEFAULT_SERVICE_URL);
		} catch (MalformedURLException e) {
			// URL is hardwired and well-formed
		}
	}

	public List<ServerStat> loadServerStats() {
		System.out.println("ServerStatsDao.loadServerStats()");
		InputStream in = null;
		try {
//			in = this.serviceUrl.openStream();
//			String json = toString(in);

//			JSONArray serverStatsJSONarray = new JSONArray(json);
			List<ServerStat> serverStats = new ArrayList<ServerStat>();
//            for (int i = 0; i < serverStatsJSONarray.length(); i++) {
//                JSONObject objekt = (JSONObject) serverStatsJSONarray.get(i);
//                ServerStat ss = new ServerStat();
//                ss.setHostname(objekt.getString("hostname"));
//
//                JSONArray ja = objekt.getJSONArray("stats");
//                List<String> stats = new ArrayList<>();
//                for (int j = 0; j < ja.length(); j++) {
//                    //System.out.println(ja.get(j));
//                    stats.add((String) ja.get(j));
//                }
//                ss.setStats(stats);
//                serverStats.add(ss);
//            }

			return serverStats;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(getClass().getName(), "I/O Exception while loading users", e);
			return Collections.EMPTY_LIST;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.w(getClass().getName(), "Unable to close input stream", e);
				}
			}
		}
	}

	private String toString(InputStream in) {
		Scanner scanner = new Scanner(in, "utf-8");
		StringBuilder sb = new StringBuilder();
		while (scanner.hasNextLine()) {
			sb.append(scanner.nextLine());
		}
		return sb.toString();
	}
}
