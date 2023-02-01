package com.testovac;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.testovac.provider.BiologiaContentProvider;
import com.testovac.provider.Defaults;
import com.testovac.provider.MapaContentProvider;
import com.testovac.provider.Provider;
import com.testovac.provider.StatistikaContentProvider;
import com.testovac.server.stats.ServerStat;
import com.testovac.server.stats.ServerStatsLoader;

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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PropertyChangeListener {

	public static final String STATS_ARRAY_KEY = "statsArray";
	private ArrayList<String> neodoslaneStatistiky;

	private class LocalStatsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
		MainActivity parent;

		public LocalStatsCursorLoader(MainActivity main) {
			this.parent = main;
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
			if (id == LOCAL_STATS_LOADER_ID) {
				System.out.println("creating stats loader");
				CursorLoader loader = new CursorLoader(parent);
				Uri uri = StatistikaContentProvider.CONTENT_URI
						.buildUpon()// znamena ze zoberie zaklad sk.ics.upjs... a na nom nalepuje, nemusim uvazovat lomky
						.build();
				loader.setUri(uri);
				return loader;
			}
			if (id == OTAZKY_LOADER_ID) {
				System.out.println("creating otazky loader");
				CursorLoader loader = new CursorLoader(parent);
				Uri uri = BiologiaContentProvider.CONTENT_URI
						.buildUpon()// znamena ze zoberie zaklad sk.ics.upjs... a na nom nalepuje, nemusim uvazovat lomky
						.build();
				loader.setUri(uri);
				return loader;
			}
			if (id == MAPA_LOADER) {
				System.out.println("creating mapa loader");
				CursorLoader loader = new CursorLoader(parent);
				Uri uri = MapaContentProvider.CONTENT_URI
						.buildUpon()// znamena ze zoberie zaklad sk.ics.upjs... a na nom nalepuje, nemusim uvazovat lomky
						.build();
				loader.setUri(uri);
				return loader;
			}
			return null;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			if (loader.getId() == LOCAL_STATS_LOADER_ID && cursor != null) {
				System.out.println("stats loader finished");
				stats = getStatsFrom(cursor);
				// nemusi byt este vygenerovany imageview
				statsLoaded = true;
				if (statsImageLoaded) {
					refreshStatsImageView();
				}
				System.out.println("stats loader finished, restartujem mapa loader");
				getLoaderManager().restartLoader(MAPA_LOADER, Bundle.EMPTY, localCursorLoader);
			}

			if (loader.getId() == OTAZKY_LOADER_ID && cursor != null) {
				System.out.println("otazky loader finished");
				otazkyList = getOtazkyFrom(cursor);
			}

			if (loader.getId() == MAPA_LOADER && cursor != null) {
				System.out.println("mapa loader finished");
				mapaList = getMapaFrom(cursor);
				spracujMapaList();
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {

		}

	}

	private void spracujMapaList() {
		neodoslaneStatistiky = new ArrayList<String>();
		for (MapaItem mi : mapaList) {
			switch (mi.key) {
				case STATS_ARRAY_KEY:
					System.out.println("naslo statsArray v mape: " + mi.value);
					neodoslaneStatistiky.add(mi.value);
					break;
				case BIELA_BUNDLE_KEY:
					System.out.println("naslo " + BIELA_BUNDLE_KEY + " v mape: " + mi.value);
					bielaCheckBox.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case CERVENA_BUNDLE_KEY:
					System.out.println("naslo " + CERVENA_BUNDLE_KEY + " v mape: " + mi.value);
					cervenaCheckBox.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case ZELENA_BUNDLE_KEY:
					System.out.println("naslo " + ZELENA_BUNDLE_KEY + " v mape: " + mi.value);
					zelenaCheckBox.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case ZLTA_BUNDLE_KEY:
					System.out.println("naslo " + ZLTA_BUNDLE_KEY + " v mape: " + mi.value);
					zltaCheckBox.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case ORANZOVA_BUNDLE_KEY:
					System.out.println("naslo " + ORANZOVA_BUNDLE_KEY + " v mape: " + mi.value);
					oranzovaCheckBox.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case UCENIE_BUNDLE_KEY:
					System.out.println("naslo " + UCENIE_BUNDLE_KEY + " v mape: " + mi.value);
					learningRadioButton.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case POMIESAT_BUNDLE_KEY:
					System.out.println("naslo " + POMIESAT_BUNDLE_KEY + " v mape: " + mi.value);
					pomiesatCheckBox.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case TRENING_BUNDLE_KEY:
					System.out.println("naslo " + TRENING_BUNDLE_KEY + " v mape: " + mi.value);
					trainingRadioButton.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case TEST_BUNDLE_KEY:
					System.out.println("naslo " + TEST_BUNDLE_KEY + " v mape: " + mi.value);
					testRadioButton.setChecked(Boolean.parseBoolean(mi.value));
					break;
				case MAX_BUNDLE_KEY:
					System.out.println("naslo " + MAX_BUNDLE_KEY + " v mape: " + mi.value);
					maxEditText.setText(mi.value);
					break;
				case MIN_BUNDLE_KEY:
					System.out.println("naslo " + MIN_BUNDLE_KEY + " v mape: " + mi.value);
					minEditText.setText(mi.value);
					break;

				default:
					System.out.println("neznamy mapa key: " + mi.key + " value: " + mi.value);
					break;
			}
		}
		if (neodoslaneStatistiky.size() > 0) {
			odosliStatistikuButton.setEnabled(true);
			odosliStatistikuButton.setText("Odošli štatistiku (" + neodoslaneStatistiky.size() + ")");
		} else {
			odosliStatistikuButton.setEnabled(false);
			odosliStatistikuButton.setText("Odošli štatistiku (0)");
		}
	}

	private List<MapaItem> getMapaFrom(Cursor cursor) {
		System.out.println("getMapaFrom actionPerformed");
		List<MapaItem> mapaItems = new ArrayList<>();
		while (cursor.moveToNext()) {
			MapaItem mi = new MapaItem();
			mi.id = cursor.getInt(cursor.getColumnIndex(Provider.Biologia._ID));
			mi.key = cursor.getString(cursor.getColumnIndex(Provider.Mapa.KEY));
			mi.value = cursor.getString(cursor.getColumnIndex(Provider.Mapa.VALUE));
			mapaItems.add(mi);
		}
		return mapaItems;
	}

	private class ServerStatsCursorLoader implements LoaderManager.LoaderCallbacks<List<ServerStat>> {
		MainActivity parent;

		public ServerStatsCursorLoader(MainActivity main) {
			this.parent = main;
		}

		@Override
		public Loader<List<ServerStat>> onCreateLoader(int i, Bundle bundle) {
			return new ServerStatsLoader(parent);
		}

		@Override
		public void onLoadFinished(Loader<List<ServerStat>> loader, List<ServerStat> serverStats) {
			parent.serverStats = serverStats;
			System.out.println("serverstats size: " + serverStats.size());
			parent.zmenProfilButton.setEnabled(true);
		}

		@Override
		public void onLoaderReset(Loader<List<ServerStat>> loader) {

		}
	}

	// LOADERY
	private LocalStatsCursorLoader localCursorLoader = new LocalStatsCursorLoader(this);
	private ServerStatsCursorLoader serverStatsLoader = new ServerStatsCursorLoader(this);
	private static final int OTAZKY_LOADER_ID = 1;
	private static final int LOCAL_STATS_LOADER_ID = 2;
	public static final int SERVER_STATS_LOADER = 3;
	public static final int MAPA_LOADER = 4;
	// KLUCE
	private static final String OTAZKY_BUNDLE_KEY = "otazkyList";
	public static final String UCENIE_BUNDLE_KEY = "ucenie";
	public static final String TRENING_BUNDLE_KEY = "trening";
	public static final String TEST_BUNDLE_KEY = "test";
	public static final String MIN_BUNDLE_KEY = "min";
	public static final String MAX_BUNDLE_KEY = "max";
	public static final String POMIESAT_BUNDLE_KEY = "pomiesat";
	public static final String CERVENA_BUNDLE_KEY = "cervena";
	public static final String BIELA_BUNDLE_KEY = "biela";
	public static final String ZLTA_BUNDLE_KEY = "zlta";
	public static final String ORANZOVA_BUNDLE_KEY = "oranzova";
	public static final String ZELENA_BUNDLE_KEY = "zelena";
	public static final String SERVER_STATS_BUNDLE_KEY = "serverStats";
	// komponenty
	private Button zacniTestButton;
	private Button odosliStatistikuButton;
	private Button zmenProfilButton;
	private RadioButton learningRadioButton;
	private RadioButton trainingRadioButton;
	private RadioButton testRadioButton;
	private EditText minEditText;
	private EditText maxEditText;
	private CheckBox pomiesatCheckBox;
	private CheckBox cervenaCheckBox;
	private CheckBox bielaCheckBox;
	private CheckBox zltaCheckBox;
	private CheckBox oranzovaCheckBox;
	private CheckBox zelenaCheckBox;
	public static float FONT_SIZE = 20;
	private TextView filtreTextView;
	private TextView rozsahTextView;
	private ImageView statImageView;
	// potrebne data
	private int[] stats;
	public List<ServerStat> serverStats;
	private List<Otazka> otazkyList;
	private List<MapaItem> mapaList;
	private boolean statsLoaded = false;
	private boolean statsImageLoaded = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
		float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		FONT_SIZE = dpHeight * dpWidth / 7500;
		System.out.println("width: " + dpWidth + " height: " + dpHeight + " font size: " + FONT_SIZE);
		setContentView(R.layout.activity_main);
/*TODO:
 - aby sa obrazovka nevypinala ked sa nic nedeje    OK
 - rychlejsie nacitavanie profilu
 - doriesenie internetu a synchronizacie
 - pismenka sa niekedy nezobrazia                   OK
  */
		initKomponenty();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (savedInstanceState != null) {
			System.out.println("STARA INSTANCIA");
			learningRadioButton.setSelected(savedInstanceState.getBoolean(UCENIE_BUNDLE_KEY));
			trainingRadioButton.setSelected(savedInstanceState.getBoolean(TRENING_BUNDLE_KEY));
			testRadioButton.setSelected(savedInstanceState.getBoolean(TEST_BUNDLE_KEY));
			pomiesatCheckBox.setSelected(savedInstanceState.getBoolean(POMIESAT_BUNDLE_KEY));
			cervenaCheckBox.setSelected(savedInstanceState.getBoolean(CERVENA_BUNDLE_KEY));
			bielaCheckBox.setSelected(savedInstanceState.getBoolean(BIELA_BUNDLE_KEY));
			zltaCheckBox.setSelected(savedInstanceState.getBoolean(ZLTA_BUNDLE_KEY));
			oranzovaCheckBox.setSelected(savedInstanceState.getBoolean(ORANZOVA_BUNDLE_KEY));
			zelenaCheckBox.setSelected(savedInstanceState.getBoolean(ZELENA_BUNDLE_KEY));
			minEditText.setText(savedInstanceState.getString(MIN_BUNDLE_KEY));
			maxEditText.setText(savedInstanceState.getString(MAX_BUNDLE_KEY));
			serverStats = (List<ServerStat>) savedInstanceState.getSerializable(SERVER_STATS_BUNDLE_KEY);
			otazkyList = (List<Otazka>) savedInstanceState.getSerializable(OTAZKY_BUNDLE_KEY);
			// vzdy treba aktualne statistiky nacitavat
			getLoaderManager().restartLoader(LOCAL_STATS_LOADER_ID, Bundle.EMPTY, localCursorLoader);
			zacniTestButton.setEnabled(true);
			zmenProfilButton.setEnabled(true);
		} else {
			System.out.println("NOVA INSTANCIA");
			// nova instancia, nacitame otazky z databazy a statistiky
			getLoaderManager().initLoader(OTAZKY_LOADER_ID, Bundle.EMPTY, localCursorLoader);
			getLoaderManager().initLoader(LOCAL_STATS_LOADER_ID, Bundle.EMPTY, localCursorLoader);
			//getLoaderManager().initLoader(MAPA_LOADER, Bundle.EMPTY, localCursorLoader);
			//osetrit ked nemame internet

			getLoaderManager().initLoader(SERVER_STATS_LOADER, Bundle.EMPTY, serverStatsLoader);

			// no action
//                System.out.println("ZIADEN INTERNET");
//                final TextView textView = new TextView(this);
//                textView.setText("Nie je pripojenie na internet!\nPripojte sa na internet a znova spustite aplikaciu.");
//                textView.setTextColor(Color.BLACK);
//                textView.setPadding(5, 5, 5, 5);
//                new AlertDialog.Builder(this)
//                        .setTitle("Upozornenie")
//                        .setView(textView)
//                        .setPositiveButton("OK", Defaults.DISMISS_ACTION)
//                        .setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
//                        .show();

		}
		// ak nemame serverstats, tak ani nemozeme menit profil
		zmenProfilButton.setEnabled(serverStats != null);


		ViewTreeObserver observer = statImageView.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				statImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// Do what you need with yourView here...
				System.out.println("ViewTreeObserver: statImageView.getHeight() == " + statImageView.getHeight());
				statsImageLoaded = true;
				// nemusi byt statistika nacitana este
				if (statsLoaded) {
					refreshStatsImageView();
				}
			}
		});
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//treba restartovat ked sa vratime spet do hlavnej aktivity, ktora bola pozastavena a zmenili sa jej data
		System.out.println("RESTART LOADER IN onRestart()");
		getLoaderManager().restartLoader(LOCAL_STATS_LOADER_ID, Bundle.EMPTY, localCursorLoader);
		getLoaderManager().restartLoader(MAPA_LOADER, Bundle.EMPTY, localCursorLoader);
	}

	private void refreshStatsImageView() {
		int min = 1,
				max = 5;

		String minText = minEditText.getText().toString(),
				maxText = maxEditText.getText().toString();

		if (minText.length() != 0) {
			min = Integer.parseInt(minText);
		} else {
			min = 1;
		}

		if (maxText.length() != 0) {
			max = Integer.parseInt(maxText);
		} else {
			max = 5;
		}

		Bitmap bmp = ObrazokGenerator.nakresliObrazok(
				stats,
				statImageView,
				min,
				max);
		if (bmp != null) {
			statImageView.setImageBitmap(bmp);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("sendStatsFailed".equals(evt.getPropertyName())) {
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
		}
		if ("sendStatsSucceeded".equals(evt.getPropertyName())) {
			System.out.println("uspesny upload");
			Uri deleteUri = MapaContentProvider.CONTENT_URI
					.buildUpon()
					.appendPath("statsArrays")
					.build();
			AsyncQueryHandler deleteHandler = new AsyncQueryHandler(getContentResolver()) {
				@Override
				protected void onDeleteComplete(int token, Object cookie, int result) {
				}
			};
			deleteHandler.startDelete(1, null, deleteUri, Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);
			final TextView textView = new TextView(this);
			textView.setText("Štatistika bola odoslaná.");
			textView.setTextColor(Color.BLACK);
			textView.setPadding(5, 5, 5, 5);
			new AlertDialog.Builder(this)
					.setTitle("Oznam")
					.setView(textView)
					.setPositiveButton("OK", Defaults.DISMISS_ACTION)
					.setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
					.show();
		}
	}

	public void updatniKeyVMape(int id, String key, String value) {
		System.out.println("updatniKeyVMape");
		Uri deleteUri = MapaContentProvider.CONTENT_URI
				.buildUpon()
				.appendPath(Integer.toString(id))
				.build();
		AsyncQueryHandler updateHAndler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onDeleteComplete(int token, Object cookie, int result) {
			}
		};
		ContentValues values = new ContentValues();
		values.put(Provider.Mapa.KEY, key);
		values.put(Provider.Mapa.VALUE, value);
		updateHAndler.startUpdate(1, null, deleteUri, values, Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);
		updateHAndler.startUpdate(1, null, deleteUri, values, Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);
	}

	private class SendStatsAsyncTask extends AsyncTask<String, Void, Boolean> {

		PropertyChangeSupport changes = new PropertyChangeSupport(this);
		String statistika;
		int pocet;

		public SendStatsAsyncTask(String statistika, int i) {
			this.statistika = statistika;
			pocet = i;
		}

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
			String stats = statistika;
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
			// iba pri poslednom odoslani riesime uspech alebo neuspech
			if (pocet == 0) {
				if (!sendSuccessful) {
					changes.firePropertyChange("sendStatsFailed", false, statistika);
				} else {
					changes.firePropertyChange("sendStatsSucceeded", false, statistika);
				}
			}
		}
	}

	public void odosliStatistikuButtonClicked(View view) {
		System.out.println("odosliStatistikuButtonClicked");
		System.out.println("sme ONLINE");
		for (int i = neodoslaneStatistiky.size() - 1; i >= 0; i--) {
			// chceme aby posledna odosielana statistika bola rozhodujuca ci je odoslanie uspesne alebo nie
			String statistika = neodoslaneStatistiky.get(neodoslaneStatistiky.size() - i - 1);
			SendStatsAsyncTask sendStatsAsyncTask = new SendStatsAsyncTask(statistika, i);
			sendStatsAsyncTask.addPropertyChangeListener(this);
			sendStatsAsyncTask.execute();
		}


//            // no action
//            System.out.println("ZIADEN INTERNET");
//            final TextView textView = new TextView(this);
//            textView.setText("Nie je pripojenie na internet!\nPripojte sa na internet a znova spustite akciu.");
//            textView.setTextColor(Color.BLACK);
//            textView.setPadding(5, 5, 5, 5);
//            new AlertDialog.Builder(this)
//                    .setTitle("Upozornenie")
//                    .setView(textView)
//                    .setPositiveButton("OK", Defaults.DISMISS_ACTION)
//                    .setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
//                    .show();
//        }

	}

	private void initKomponenty() {
		zacniTestButton = (Button) findViewById(R.id.zacniTestButton);
		zmenProfilButton = (Button) findViewById(R.id.zmenProfilButton);
		odosliStatistikuButton = (Button) findViewById(R.id.odosliStatistikuButton);
		learningRadioButton = (RadioButton) findViewById(R.id.ucenieRadioButton);
		trainingRadioButton = (RadioButton) findViewById(R.id.treningRadioButton);
		testRadioButton = (RadioButton) findViewById(R.id.testRadioButton);
		minEditText = (EditText) findViewById(R.id.minEditText);
		minEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (minEditText.getText().length() > 0) {
					refreshStatsImageView();
				}
			}
		});
		maxEditText = (EditText) findViewById(R.id.maxEditText);
		maxEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (maxEditText.getText().length() > 0) {
					refreshStatsImageView();

				}
			}
		});
		pomiesatCheckBox = (CheckBox) findViewById(R.id.pomiesatCheckBox);
		cervenaCheckBox = (CheckBox) findViewById(R.id.cervenaCheckBox);
		bielaCheckBox = (CheckBox) findViewById(R.id.bielaCheckBox);
		zltaCheckBox = (CheckBox) findViewById(R.id.zltaCheckBox);
		oranzovaCheckBox = (CheckBox) findViewById(R.id.oranzovaCheckBox);
		zelenaCheckBox = (CheckBox) findViewById(R.id.zelenaCheckBox);
		filtreTextView = (TextView) findViewById(R.id.filtreTextView);
		rozsahTextView = (TextView) findViewById(R.id.rozsahTextView);
		statImageView = (ImageView) findViewById(R.id.statImageView);

		zacniTestButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		learningRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		trainingRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		testRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		minEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		maxEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		pomiesatCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		cervenaCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		bielaCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		zltaCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		oranzovaCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		zelenaCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		filtreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
		rozsahTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
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

	public void zacniTestOnItemClick(View view) {
//1. LOAD OTAZKY A STATISTIKU
// 2. VYBER OTAZKY PODLA ZADANYCH KRITERII A POSLI ICH DO AKTIVITY
		int min = 1;
		int max = 5;

		String minText = minEditText.getText().toString();
		if (minText.length() != 0) {
			min = Integer.parseInt(minText);
		} else {
			min = 1;
		}

		String maxText = maxEditText.getText().toString();
		if (maxText.length() != 0) {
			max = Integer.parseInt(maxText);
		} else {
			max = 5;
		}

		//zabezpecit aby bol rozsah spravny
		if (min < 1) {
			min = 1;
			minEditText.setText("" + min);
		}
		if (min > 1500) {
			min = 1500;
			minEditText.setText("" + min);
		}
		if (max < 1) {
			max = 1;
			maxEditText.setText("" + max);
		}
		if (max > 1500) {
			max = 1500;
			maxEditText.setText("" + max);
		}
		if (min > max) {
			minEditText.setText("" + max);
			maxEditText.setText("" + min);
			int pom = min;
			min = max;
			max = pom;
		}

		List<Otazka> vybraneOtazky = new ArrayList<>();
		Otazka o;
		for (int i = min; i <= max; i++) {
			o = otazkyList.get(i);
			if (vyhovujeCheckboxomUloha(o.id, stats)) {
				vybraneOtazky.add(o);
			}
		}

		System.out.println("Checkboxom vyhovuje: " + vybraneOtazky.size());
		if (vybraneOtazky.size() > 0) {
			if (pomiesatCheckBox.isChecked()) {
				Collections.shuffle(vybraneOtazky);
			}
			InstanciaTestu it = new InstanciaTestu(vybraneOtazky, stats);
			it.testSelected = testRadioButton.isChecked();
			it.treningSelected = trainingRadioButton.isChecked();
			it.setUcenieSelected(learningRadioButton.isChecked());

			// ulozime si nastavenia
			updatniNastavenia(UCENIE_BUNDLE_KEY, String.valueOf(learningRadioButton.isChecked()));
			updatniNastavenia(TRENING_BUNDLE_KEY, String.valueOf(trainingRadioButton.isChecked()));
			updatniNastavenia(TEST_BUNDLE_KEY, String.valueOf(testRadioButton.isChecked()));
			updatniNastavenia(POMIESAT_BUNDLE_KEY, String.valueOf(pomiesatCheckBox.isChecked()));
			updatniNastavenia(CERVENA_BUNDLE_KEY, String.valueOf(cervenaCheckBox.isChecked()));
			updatniNastavenia(BIELA_BUNDLE_KEY, String.valueOf(bielaCheckBox.isChecked()));
			updatniNastavenia(ZLTA_BUNDLE_KEY, String.valueOf(zltaCheckBox.isChecked()));
			updatniNastavenia(ORANZOVA_BUNDLE_KEY, String.valueOf(oranzovaCheckBox.isChecked()));
			updatniNastavenia(ZELENA_BUNDLE_KEY, String.valueOf(zelenaCheckBox.isChecked()));
			updatniNastavenia(MIN_BUNDLE_KEY, String.valueOf(minEditText.getText()));
			updatniNastavenia(MAX_BUNDLE_KEY, String.valueOf(maxEditText.getText()));

			Intent test = new Intent(this, OtazkaActivity.class);
			test.putExtra(OtazkaActivity.TEST_INSTANCIA_BUNDLE_KEY, it);
			startActivity(test);
		} else {
			//alert dialog ze ziadne otazky niesu
			System.out.println("ZIADNE OTAZKY NEVYHOVUJU");
			final TextView textView = new TextView(this);
			textView.setText("Zadaným kritériám nevyhovuje žiadna otázka!");
			textView.setTextColor(Color.BLACK);
			textView.setPadding(5, 5, 5, 5);
			new AlertDialog.Builder(this)
					.setTitle("Upozornenie")
					.setView(textView)
					.setPositiveButton("OK", Defaults.DISMISS_ACTION)
					.setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
					.show();
		}
	}

	private void updatniNastavenia(String bundleKey, String value) {
		if (bundleKey.equals("null")) {
			System.out.println(bundleKey + "=" + value);
			final TextView textView = new TextView(this);
			textView.setText("Bundle key = null!");
			textView.setTextColor(Color.BLACK);
			textView.setPadding(5, 5, 5, 5);
			new AlertDialog.Builder(this)
					.setTitle("Upozornenie")
					.setView(textView)
					.setPositiveButton("OK", Defaults.DISMISS_ACTION)
					.setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
					.show();
			// throw new RuntimeException();
		}
		MapaItem mi = getMapaItem(mapaList, bundleKey);
		if (mi == null) {
			System.out.println("nove nastavenie do mapy: " + bundleKey + "=" + value);
			insertKeyDoMapy(bundleKey, value);
		} else {
			if (!mi.value.equals(value)) {
				mi.value = value;
				System.out.println("update nastavenie do mapy: " + bundleKey + "=" + value);
				updatniKeyVMape(mi.id, mi.key, mi.value);
			}
		}
	}

	private void insertKeyDoMapy(String key, String value) {
		Uri uri = MapaContentProvider.CONTENT_URI;
		ContentValues values = new ContentValues();
		values.put(Provider.Mapa.KEY, key);
		values.put(Provider.Mapa.VALUE, value);

		AsyncQueryHandler insertHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onInsertComplete(int token, Object cookie, Uri uri) {
			}
		};

		insertHandler.startInsert(1, Defaults.NO_COOKIE, uri, values);
	}

	private MapaItem getMapaItem(List<MapaItem> mapaList, String bundleKey) {
		for (MapaItem mi : mapaList) {
			if (mi.key.equals(bundleKey)) {
				return mi;
			}
		}
		return null;
	}

	private boolean vyhovujeCheckboxomUloha(int i, int[] stats) {
		//System.out.println("checking id:"+i+" against: ["+stats[i]+"] bielaCheckbox: "+bielaCheckBox.isSelected());
		if (stats[i] == 0 && bielaCheckBox.isChecked()) {
			return true;
		}
		if (stats[i] == 1 && zltaCheckBox.isChecked()) {
			return true;
		}
		if (stats[i] == 2 && oranzovaCheckBox.isChecked()) {
			return true;
		}
		if (stats[i] < 0 && cervenaCheckBox.isChecked()) {
			return true;
		}
		if (stats[i] > 2 && zelenaCheckBox.isChecked()) {
			return true;
		}

		return false;
	}

	private boolean getBoolean(int cislo) {
		if (cislo == 0) {
			return false;
		}
		return true;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(UCENIE_BUNDLE_KEY, learningRadioButton.isChecked());
		outState.putSerializable(TRENING_BUNDLE_KEY, trainingRadioButton.isChecked());
		outState.putSerializable(TEST_BUNDLE_KEY, testRadioButton.isChecked());
		outState.putSerializable(POMIESAT_BUNDLE_KEY, pomiesatCheckBox.isChecked());
		outState.putSerializable(CERVENA_BUNDLE_KEY, cervenaCheckBox.isChecked());
		outState.putSerializable(BIELA_BUNDLE_KEY, bielaCheckBox.isChecked());
		outState.putSerializable(ZLTA_BUNDLE_KEY, zltaCheckBox.isChecked());
		outState.putSerializable(ORANZOVA_BUNDLE_KEY, oranzovaCheckBox.isChecked());
		outState.putSerializable(ZELENA_BUNDLE_KEY, zelenaCheckBox.isChecked());
		outState.putSerializable(MIN_BUNDLE_KEY, String.valueOf(minEditText.getText()));
		outState.putSerializable(MAX_BUNDLE_KEY, String.valueOf(maxEditText.getText()));
//		outState.putSerializable(SERVER_STATS_BUNDLE_KEY, (Serializable) serverStats);
//		outState.putSerializable(OTAZKY_BUNDLE_KEY, (Serializable) otazkyList);
		System.out.println("main activity instancia ulozena");
	}


	public void zmenitProfilButtonClicked(View view) {
		//zobrazit dialog upozornenie ked nemame internet a server stats
//        if (!isOnline()) {
//            final TextView textView = new TextView(this);
//            textView.setText("Nepodarilo sa pripojiť k internetu!");
//            textView.setTextColor(Color.BLACK);
//            textView.setPadding(5, 5, 5, 5);
//            new AlertDialog.Builder(this)
//                    .setTitle("Upozornenie")
//                    .setView(textView)
//                    .setPositiveButton("OK", Defaults.DISMISS_ACTION)
//                    .setNegativeButton("Cancel", Defaults.DISMISS_ACTION)
//                    .show();
//            return;
//        }
		Intent novy = new Intent(MainActivity.this, ServerStatSlideActivity.class);
		novy.putExtra("serverStats", (Serializable) serverStats);
		startActivity(novy);
	}

	public void statsImageViewClicked(View view) {
		// aby sa vykreslil zvoleny rozsah
		refreshStatsImageView();
	}

	private int[] getStatsFrom(Cursor cursor) {
		System.out.println("getStatsFrom actionPerformed");
		int[] stats = new int[1501];
		int idx = 1;
		while (cursor.moveToNext()) {
			stats[idx] = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Provider.Statistika.STATS)));
			//System.out.print(idx + "=" + stats[idx] + " ");
			idx++;
		}
		System.out.println();
		if (idx != 1501) {
			System.out.println("stats size: " + idx);
			throw new RuntimeException("stats size: " + idx);
		}
		cursor.close();
		return stats;
	}

	private List<Otazka> getOtazkyFrom(Cursor cursor) {
		if (cursor == null) {
			Log.d(MainActivity.class.getName(), "cursor is null");
			return null;
		}

		System.out.println("getOtazkyFrom actionPerformed");
		List<Otazka> otazkyList = new ArrayList<>();
		while (cursor.moveToNext()) {
			Otazka o = new Otazka();
			o.id = cursor.getInt(cursor.getColumnIndex(Provider.Biologia._ID));
			o.otazka = cursor.getString(cursor.getColumnIndex(Provider.Biologia.OTAZKA));
			o.odpovede[0] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_1));
			o.odpovede[1] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_2));
			o.odpovede[2] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_3));
			o.odpovede[3] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_4));
			o.odpovede[4] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_5));
			o.odpovede[5] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_6));
			o.odpovede[6] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_7));
			o.odpovede[7] = cursor.getString(cursor.getColumnIndex(Provider.Biologia.ODPOVED_8));
			o.jeSpravne[0] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_1)));
			o.jeSpravne[1] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_2)));
			o.jeSpravne[2] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_3)));
			o.jeSpravne[3] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_4)));
			o.jeSpravne[4] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_5)));
			o.jeSpravne[5] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_6)));
			o.jeSpravne[6] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_7)));
			o.jeSpravne[7] = getBoolean(cursor.getInt(cursor.getColumnIndex(Provider.Biologia.JESPRAVNA_8)));
			otazkyList.add(o);
		}
		// mame otazky, vieme si generovat testy
		zacniTestButton.setEnabled(true);
		System.out.println("otazky size: " + otazkyList.size());
		if (otazkyList.size() != 1501) {
			throw new RuntimeException("getOtazkyFrom: OTAZKY SIZE != 1501");
		}
		return otazkyList;
	}

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
