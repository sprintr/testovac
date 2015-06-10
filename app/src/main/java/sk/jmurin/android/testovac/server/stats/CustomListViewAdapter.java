package sk.jmurin.android.testovac.server.stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sk.jmurin.android.testovac.R;

/**
 * Created by Janco1 on 3. 6. 2015.
 */
public class CustomListViewAdapter extends BaseAdapter{

    Context context;
    ServerStat serverStat;
    private static LayoutInflater inflater = null;

    public CustomListViewAdapter(Context context, ServerStat data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.serverStat = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return serverStat.getTimestampStats().size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return serverStat.getTimestampStats().get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.list_item, null);
        TextView text = (TextView) vi.findViewById(R.id.statViewItem);
        text.setText(serverStat.getTimestampStats().get(position).getTimestamp());
        ImageView statImageView = (ImageView) vi.findViewById(R.id.statImageView);
        statImageView.setImageBitmap(ServerStatsObrazkyResource.hostnames.get(serverStat.getHostname()).get(position));
        return vi;
    }


}
