package sk.jmurin.android.testovac.server.stats;


import android.content.Context;

import java.util.List;

public class ServerStatsLoader extends AbstractObjectLoader<List<ServerStat>> {
    private final ServerStatsDao serverStatsDao;

    public ServerStatsLoader(Context context) {
        super(context);
        this.serverStatsDao = new ServerStatsDao();
    }

    @Override
    public List<ServerStat> loadInBackground() {
        return this.serverStatsDao.loadServerStats();
    }
}
