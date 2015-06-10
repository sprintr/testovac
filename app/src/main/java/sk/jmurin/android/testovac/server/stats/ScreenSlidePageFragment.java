/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.jmurin.android.testovac.server.stats;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.jmurin.android.testovac.R;
import sk.jmurin.android.testovac.ServerStatSlideActivity;
import sk.jmurin.android.testovac.provider.Defaults;
import sk.jmurin.android.testovac.provider.Provider;
import sk.jmurin.android.testovac.provider.StatistikaContentProvider;

import static sk.jmurin.android.testovac.provider.Defaults.NO_COOKIE;
import static sk.jmurin.android.testovac.provider.Defaults.NO_SELECTION;
import static sk.jmurin.android.testovac.provider.Defaults.NO_SELECTION_ARGS;


public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";
    public static final String SERVER_STAT = "serverStat";
    private SimpleCursorAdapter adapter;

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    private ServerStat serverStat;
    public static final Bundle NO_BUNDLE = null;
    public static final Cursor NO_CURSOR = null;
    public static final int NO_FLAGS = 0;
    private ListView tasksListView;
    private ListView serverStatsListView;
    private ViewGroup rootView;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber, ServerStat serverStat) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        args.putSerializable(SERVER_STAT, serverStat);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        serverStat = (ServerStat) getArguments().getSerializable(SERVER_STAT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page, container, false);

        // Set the title view to show the page number.
        ((TextView) rootView.findViewById(android.R.id.text1)).setText("Profil: " + serverStat.getHostname());
        serverStatsListView = (ListView) rootView.findViewById(R.id.serverStatsListView);
        serverStatsListView.setAdapter(new CustomListViewAdapter(getActivity().getApplicationContext(), serverStat));
        serverStatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TextView textView = new TextView(getActivity());
                textView.setText("Zvolený profil: " + serverStat.getTimestampStats().get(position).getTimestamp());
                textView.setTextColor(Color.BLACK);
                textView.setPadding(5, 5, 5, 5);
                final int pos=position;
                new AlertDialog.Builder(getActivity())
                        .setTitle("Chcete načítať zvolený profil?")
                        .setView(textView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                zmenitProfil3(pos);
                            }
                        })
                        .setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
                        .show();
            }
        });
        return rootView;
    }

    public void zmenitProfil3(final int position) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog dialog = new ProgressDialog(getActivity());

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Načítavam profil...");
                dialog.setProgressStyle(dialog.STYLE_HORIZONTAL);
                dialog.setProgress(0);
                dialog.setMax(1500);
                dialog.show();
            }

            @Override
            protected void onPostExecute(Void result) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                final TextView textView = new TextView(getActivity());
                textView.setText("Profil úspešne načítaný!");
                textView.setTextColor(Color.BLACK);
                textView.setPadding(5, 5, 5, 5);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Oznam")
                        .setView(textView)
                        .setPositiveButton("OK", Defaults.DISMISS_ACTION)
                        .show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                int[] stats = serverStat.getTimestampStats().get(position).getStatistics();
                final int hotovych = 0;
                for (int i = 1; i < 1501; i++) {
                    // updatneme statistiku
                    Uri uri = StatistikaContentProvider.CONTENT_URI
                            .buildUpon()
                            .appendPath(String.valueOf("noNotify"))
                            .appendPath(String.valueOf(i))
                            .build();
                    if (i == 1500) {
                        uri = StatistikaContentProvider.CONTENT_URI
                                .buildUpon()
                                .appendPath(String.valueOf(i))
                                .build();
                    }
                    ContentValues values = new ContentValues();
                    values.put(Provider.Statistika.STATS, stats[i]);
                    getActivity().getContentResolver().update(uri, values, NO_SELECTION, NO_SELECTION_ARGS);
                    dialog.setProgress(i);
                }
                return null;
            }
        }.execute();

    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
}
