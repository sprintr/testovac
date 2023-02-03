package com.testovac;

import static com.testovac.provider.Defaults.NO_COOKIE;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.testovac.provider.Defaults;
import com.testovac.provider.MapaContentProvider;
import com.testovac.provider.Provider;
import com.testovac.provider.StatistikaContentProvider;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class StatistikaActivity extends AppCompatActivity implements PropertyChangeListener {

	public static final String STATISTIKA_BUNDLE_KEY = "statistika";
	private Statistika statistika;
	private TextView vyriesenychLabel;
	private TextView uspesnychLabel;
	private TextView uspesnostLabel;
	private TextView minusBodovLabel;
	private TextView cervenychTextView;
	private TextView bielychTextView;
	private TextView zltychTextView;
	private TextView oranzovychTextView;
	private TextView zelenychTextView;
	public static float FONT_SIZE = 20;
	private Button zdielatButton;
	private Button znovaPrejstButton;
	private Button zatvoritButton;
	private int[] stats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
		float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		FONT_SIZE = dpHeight * dpWidth / 5000;
		System.out.println("width: " + dpWidth + " height: " + dpHeight + " font size: " + FONT_SIZE);
		setContentView(R.layout.activity_statistika);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().hide();

		vyriesenychLabel = (TextView) findViewById(R.id.vyriesenychLabel);
		uspesnychLabel = (TextView) findViewById(R.id.uspesnychLabel);
		uspesnostLabel = (TextView) findViewById(R.id.uspesnostLabel);
		minusBodovLabel = (TextView) findViewById(R.id.minusBodovLabel);
		cervenychTextView = (TextView) findViewById(R.id.cervenychTextView);
		bielychTextView = (TextView) findViewById(R.id.bielychTextView);
		zltychTextView = (TextView) findViewById(R.id.zltychTextView);
		oranzovychTextView = (TextView) findViewById(R.id.oranzovychTextView);
		zelenychTextView = (TextView) findViewById(R.id.zelenychTextView);
		zdielatButton = (Button) findViewById(R.id.zdielatButton);
		znovaPrejstButton = (Button) findViewById(R.id.znovaPrejstButton);
		zatvoritButton = (Button) findViewById(R.id.zatvoritButton);

		if (savedInstanceState != null) {
			statistika = (Statistika) savedInstanceState.get(STATISTIKA_BUNDLE_KEY);
		} else {
			statistika = (Statistika) getIntent().getSerializableExtra(STATISTIKA_BUNDLE_KEY);
		}

		nastavOdpovedText(vyriesenychLabel, "Vyriešených otázok: " + statistika.vyriesenych);
		nastavOdpovedText(uspesnychLabel, "Úspešných: otázok: " + statistika.uspesnych);
		nastavOdpovedText(uspesnostLabel, "Úspešnosť: " + statistika.uspesnost + " %");
		nastavOdpovedText(minusBodovLabel, "Mínus bodov: " + statistika.minusBodov);
		nastavOdpovedText(cervenychTextView, String.format("%+d", statistika.pribudlo[0]));
		nastavOdpovedText(bielychTextView, String.format("%+d", statistika.pribudlo[1]));
		nastavOdpovedText(zltychTextView, String.format("%+d", statistika.pribudlo[2]));
		nastavOdpovedText(oranzovychTextView, String.format("%+d", statistika.pribudlo[3]));
		nastavOdpovedText(zelenychTextView, String.format("%+d", statistika.pribudlo[4]));
		zdielatButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		znovaPrejstButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		zatvoritButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		znovaPrejstButton.setText("Zopakovať (" + statistika.zleZodpovedane.length + ")");
		if (statistika.zleZodpovedane.length == 0) {
			znovaPrejstButton.setEnabled(false);
		}
		odosliStatistikuNaServer();
	}


	private void nastavOdpovedText(TextView odpovedLabel, String odpoved) {
		odpovedLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		odpovedLabel.setTextColor(Color.BLACK);
		odpovedLabel.setText(odpoved);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_statistika, menu);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(STATISTIKA_BUNDLE_KEY, statistika);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void znovaPrejstButtonActionPerformed(View view) {
		//Toast.makeText(this, "znova prejdeme zle zodpovedane", Toast.LENGTH_SHORT).show();
		List<Otazka> otazky = new ArrayList<>();
		for (int i = 0; i < statistika.zleZodpovedane.length; i++) {
			Otazka nova = new Otazka();
			nova.id = statistika.zleZodpovedane[i];
			otazky.add(nova);
		}
		Collections.shuffle(otazky);
		InstanciaTestu it;
		if (stats != null) {
			it = new InstanciaTestu(otazky, stats);
		} else {
			// pre krajny pripad ked nenacitalo statistiky, tak ich nacita teraz z DB
			it = new InstanciaTestu(otazky, getStats());
		}
		it.testSelected = statistika.test;
		it.treningSelected = statistika.trening;
		it.setUcenieSelected(statistika.ucenie);


		Intent test = new Intent(this, OtazkaActivity.class);
		test.putExtra(OtazkaActivity.TEST_INSTANCIA_BUNDLE_KEY, it);
		test.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(test);
		//finish();
	}

	private void odosliStatistikuNaServer() {
		System.out.println("odosielam statistiku na server");
//        if (isOnline()) {
		System.out.println("sme ONLINE");
		SendStatsAsyncTask sendStatsAsyncTask = new SendStatsAsyncTask();
		sendStatsAsyncTask.addPropertyChangeListener(this);
		sendStatsAsyncTask.execute();
//        } else {
//
//        }
	}

	private void insertIntoContentProvider(String statsArray) {
		Uri uri = MapaContentProvider.CONTENT_URI;
		ContentValues values = new ContentValues();
		values.put(Provider.Mapa.KEY, MainActivity.STATS_ARRAY_KEY);
		values.put(Provider.Mapa.VALUE, statsArray);

		AsyncQueryHandler insertHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onInsertComplete(int token, Object cookie, Uri uri) {
			}
		};

		insertHandler.startInsert(1, NO_COOKIE, uri, values);
	}

//    public boolean isOnline() {
//        boolean haveConnectedWifi = false;
//        boolean haveConnectedMobile = false;
//
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
//        for (NetworkInfo ni : netInfo) {
//            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
//                if (ni.isConnected())
//                    haveConnectedWifi = true;
//            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
//                if (ni.isConnected())
//                    haveConnectedMobile = true;
//        }
//        return haveConnectedWifi || haveConnectedMobile;
//    }

	public void zdielatButtonActionPerformed(View view) {
		Toast.makeText(this, "zdielame", Toast.LENGTH_SHORT).show();
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "Úspešne som vyriešil " + statistika.uspesnych + " otázok zo " + statistika.vyriesenych + " v aplikácii testovač :).");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	public void zatvoritButtonActionPerformed(View view) {
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("sendStatsFailed".equals(evt.getPropertyName()) && !isFinishing()) {
			System.out.println("ZIADEN INTERNET");
			final TextView textView = new TextView(this);
			textView.setText("Nie je pripojenie na internet!\nŠtatistiku z tohto testu odošlite neskôr z hlavného menu.");
			textView.setTextColor(Color.BLACK);
			textView.setPadding(5, 5, 5, 5);
			new AlertDialog.Builder(this)
					.setTitle("Upozornenie")
					.setView(textView)
					.setPositiveButton("OK", Defaults.DISMISS_ACTION)
					.setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
					.show();
			insertIntoContentProvider((String) evt.getNewValue());
		}
	}

	private class SendStatsAsyncTask extends AsyncTask<String, Void, Boolean> {

		PropertyChangeSupport changes = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changes.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changes.removePropertyChangeListener(listener);
		}

		private boolean sendSuccessful = true;

		@Override
		protected Boolean doInBackground(String... params) {

			HttpURLConnection connection = null;
			String stats = Arrays.toString(getStats());
			try {

				String hostname = android.os.Build.MODEL;
				String urlString = "http://www.jmurin.sk/testovac/insert_stats.php";
				URL url = new URL(urlString);
				connection = (HttpURLConnection) url.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(15000);
				connection.setRequestMethod("POST");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				List<NameValuePair> paras = new ArrayList<NameValuePair>();
				paras.add(new BasicNameValuePair("hostname", hostname));
				paras.add(new BasicNameValuePair("stats", stats));

				OutputStream os = connection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(paras));
				writer.flush();
				writer.close();
				os.close();

				// connection.setRequestMethod("POST");
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return false;
				}
				InputStream in = new BufferedInputStream(connection.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder result = new StringBuilder();
				String line;
				System.out.println("server response: ");
				while ((line = reader.readLine()) != null) {
					result.append(line + "\n");
				}
				System.out.println(result.toString());
				System.out.println();

				return true;
			} catch (IOException e) {
				Log.e(getClass().getName(), "Unable to send stats", e);
				sendSuccessful = false;
				return false;
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			System.out.println("SendStatsAsyncTask finished");
			if (!sendSuccessful) {
				changes.firePropertyChange("sendStatsFailed", false, Arrays.toString(getStats()));
			}
		}
	}

	private int[] getStats() {
		System.out.println("getting stats");
		Uri statikaUri = StatistikaContentProvider.CONTENT_URI
				.buildUpon()// znamena ze zoberie zaklad sk.ics.upjs... a na nom nalepuje, nemusim uvazovat lomky
				.build();
		Cursor cursor = getContentResolver().query(
				statikaUri,
				null,
				null,
				null,
				null);
		int[] stats = new int[1501];
		int idx = 1;
		while (cursor.moveToNext()) {
			stats[idx] = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Provider.Statistika.STATS)));
			System.out.print(idx + "=" + stats[idx] + " ");
			idx++;
		}
		System.out.println();
		if (idx != 1501) {
			System.out.println("stats size: " + idx);
			throw new RuntimeException("stats size: " + idx);
		}
		cursor.close();
		this.stats = stats;
		return stats;
	}

	// http://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}
