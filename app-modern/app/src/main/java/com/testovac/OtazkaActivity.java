package com.testovac;

import static com.testovac.provider.Defaults.NO_COOKIE;
import static com.testovac.provider.Defaults.NO_SELECTION;
import static com.testovac.provider.Defaults.NO_SELECTION_ARGS;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.testovac.provider.BiologiaContentProvider;
import com.testovac.provider.Defaults;
import com.testovac.provider.Provider;
import com.testovac.provider.StatistikaContentProvider;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OtazkaActivity extends AppCompatActivity {

	public static final int STATISTIKA_UPDATE_TOKEN = 1;
	private Otazka aktualnaOtazka;
	private TextView otazkaTextView;
	private TextView[] odpovedeTextView = new TextView[8];
	private Button dalejButton;
	private Button predButton;
	private InstanciaTestu test;
	public static final String TEST_INSTANCIA_BUNDLE_KEY = "instancia";
	public static float FONT_SIZE = 20;
	private ArrayList<Otazka> otazkyList;
	private boolean layoutLoaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
		float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		FONT_SIZE = dpHeight * dpWidth / 7500;
		System.out.println("width: " + dpWidth + " height: " + dpHeight + " font size: " + FONT_SIZE);
		setContentView(R.layout.activity_otazka);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		otazkaTextView = (TextView) findViewById(R.id.otazkaTextView);
		odpovedeTextView[0] = (TextView) findViewById(R.id.odpoved1TextView);
		odpovedeTextView[1] = (TextView) findViewById(R.id.odpoved2TextView);
		odpovedeTextView[2] = (TextView) findViewById(R.id.odpoved3TextView);
		odpovedeTextView[3] = (TextView) findViewById(R.id.odpoved4TextView);
		odpovedeTextView[4] = (TextView) findViewById(R.id.odpoved5TextView);
		odpovedeTextView[5] = (TextView) findViewById(R.id.odpoved6TextView);
		odpovedeTextView[6] = (TextView) findViewById(R.id.odpoved7TextView);
		odpovedeTextView[7] = (TextView) findViewById(R.id.odpoved8TextView);


		dalejButton = (Button) findViewById(R.id.dalejButton);
		predButton = (Button) findViewById(R.id.predButton);
//        dalejButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
//        predButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,FONT_SIZE);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);


		if (savedInstanceState != null) {
			test = (InstanciaTestu) savedInstanceState.get(TEST_INSTANCIA_BUNDLE_KEY);
		} else {
			test = (InstanciaTestu) getIntent().getSerializableExtra(TEST_INSTANCIA_BUNDLE_KEY);
		}
		loadOtazkyList();

		System.out.println("Startujem test, mode: " + test.isUcenieSelected() + "-" + test.treningSelected + "-" + test.testSelected);

		ViewTreeObserver observer = odpovedeTextView[7].getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				odpovedeTextView[7].getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// Do what you need with yourView here...
				System.out.println("ViewTreeObserver: statImageView.getHeight() == " + odpovedeTextView[7].getHeight());
				// ak sa nacita layout a nesu este otazky, tak sa obnovi po nacitani otazok
				// ak sa skor nacitaju otazky ako layout tak tu sa to spravi
				layoutLoaded = true;
				if (otazkyList.size() > 0) {
					refreshOtazkaLabels();
					if (test.isUcenieSelected()) {
						skontrolujOdpovedeAVykresli();
						setClickableOdpovede(false);
					}
				}
			}
		});

	}

	private void loadOtazkyList() {
		otazkyList = new ArrayList<Otazka>();
		Uri uri = BiologiaContentProvider.CONTENT_URI
				.buildUpon()
				.appendPath("vybrane")
				.build();
		StringBuilder sql = new StringBuilder(Provider.Biologia._ID + " IN(");
		for (int i = 0; i < test.idckaUloh.length - 1; i++) {
			sql.append(test.idckaUloh[i][0] + ",");
		}
		sql.append(test.idckaUloh[test.idckaUloh.length - 1][0] + ")");
		System.out.println("selection: " + sql);
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				super.onQueryComplete(token, cookie, cursor);
				otazkyList = (ArrayList<Otazka>) getOtazkyFrom(cursor);
				cursor.close();
				aktualnaOtazka = otazkyList.get(0);
				// ak este neni layout nacitany tak po layoute sa nacita prva otazka
				if (layoutLoaded) {
					refreshOtazkaLabels();
					if (test.isUcenieSelected()) {
						skontrolujOdpovedeAVykresli();
						setClickableOdpovede(false);
					}
				}
			}
		};
		queryHandler.startQuery(STATISTIKA_UPDATE_TOKEN, NO_COOKIE, uri, null, sql.toString(), NO_SELECTION_ARGS, null);
	}

	private List<Otazka> getOtazkyFrom(Cursor cursor) {
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
		System.out.println("otazky size: " + otazkyList.size());
		return otazkyList;
	}

	private void nastavOdpovedText(TextView odpovedLabel, String odpoved) {
		odpovedLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, FONT_SIZE);
// Get the text view's paint object
		TextPaint textPaint = odpovedLabel.getPaint();
		// Store the current text size
		float oldTextSize = textPaint.getTextSize();
		// Get the required text height
		int textHeight = getTextHeight(odpoved, textPaint, odpovedLabel.getWidth(), odpoved.length());
		float targetTextSize = FONT_SIZE;
		float mMinTextSize = FONT_SIZE / 2;
		// Until we either fit within our text view or we had reached our min text size, incrementally try smaller sizes
		while (textHeight > odpovedLabel.getHeight() && targetTextSize > mMinTextSize) {
			targetTextSize = Math.max(targetTextSize - 2, mMinTextSize);
			textHeight = getTextHeight(odpoved, textPaint, odpovedLabel.getWidth(), targetTextSize);
		}
		odpovedLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
		odpovedLabel.setTextColor(Color.BLACK);
		odpovedLabel.setText(odpoved);
		System.out.println("pocet riadkov v textview: " + odpovedLabel.getLineCount() + " rozmery: " + odpovedLabel.getWidth() + "," + odpovedLabel.getHeight() + " textsize: " + targetTextSize);
	}

	// Set the text size of the text paint object and use a static layout to render text off screen before measuring
	private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
		// modified: make a copy of the original TextPaint object for measuring
		// (apparently the object gets modified while measuring, see also the
		// docs for TextView.getPaint() (which states to access it read-only)
		TextPaint paintCopy = new TextPaint(paint);
		// Update the text paint object
		paintCopy.setTextSize(textSize);
		// Measure using a static layout
		StaticLayout layout = new StaticLayout(source, paintCopy, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
		return layout.getHeight();
	}

	private void refreshOtazkaLabels() {
		System.out.println("refreshOtazkaLabels");
		//otazkaTextView.setText(aktualnaOtazka.otazka);
		nastavOdpovedText(otazkaTextView, aktualnaOtazka.otazka);
		for (int i = 0; i < 8; i++) {
			//odpovedeTextView[i].setText(aktualnaOtazka.odpovede[test.odpovedeOrder[test.aktUlohaIdx][i]]);
			nastavOdpovedText(odpovedeTextView[i], aktualnaOtazka.odpovede[test.odpovedeOrder[test.aktUlohaIdx][i]]);
			odpovedeTextView[i].setBackgroundColor(Color.WHITE);
		}
		//statusTextView.setText("Otazok: " + (test.aktUlohaIdx + 1) + "/" + test.idckaUloh.length);
		setTitle("Otazka: " + (test.aktUlohaIdx + 1) + "/" + test.idckaUloh.length);
	}

	private void loadAktualnaOtazka() {
		System.out.println("ziskavam otazku s id: " + test.idckaUloh[test.aktUlohaIdx][0]);
		aktualnaOtazka = getOtazkaPodlaID(test.idckaUloh[test.aktUlohaIdx][0]);
	}

	private Otazka getOtazkaPodlaID(int id) {
		for (Otazka o : otazkyList) {
			if (o.id == id) {
				return o;
			}
		}
		final TextView textView = new TextView(getApplicationContext());
		textView.setText("Nenaslo otazku v DB!");
		textView.setTextColor(Color.BLACK);
		textView.setPadding(5, 5, 5, 5);
		new AlertDialog.Builder(getApplicationContext())
				.setTitle("Oznam")
				.setView(textView)
				.setPositiveButton("OK", Defaults.DISMISS_ACTION)
				.show();
		return null;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_otazka, menu);
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
		if (id == R.id.zobrazStatistiku) {
			Toast.makeText(this, "statistika zobrazena", Toast.LENGTH_SHORT).show();
//            Intent intent=new Intent(this,TaskDetailActivity.class);
//            startActivity(intent);
			return true;
		}
		if (id == R.id.home) {
			Log.i(getClass().getName(), "home was clicked");
			System.out.println("home was clicked");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// saveTask();
		outState.putSerializable(TEST_INSTANCIA_BUNDLE_KEY, test);
	}

	public void dalejButtonActionPerformed(View view) {
		System.out.println("dalejButtonActionPerformed");
		// ak je otazka uz ohodnotena, tak sa rovno vykreslia spravne odpovede
		if (test.ohodnotene[test.aktUlohaIdx]) {
			test.aktUlohaIdx++;
			if (test.aktUlohaIdx == test.idckaUloh.length) {
				// iba ak sme v uciacom mode mozeme prechadzat takto
				if (test.isUcenieSelected()) {
					test.aktUlohaIdx = 0;
				} else {
					test.aktUlohaIdx--;
					// ake sme tu tak urcite su vsetky uloh ohodnotene a mozeme zobrazit statistiku
					// sme na poslednej ulohe a klikli sme na dalej
					Statistika statistika = getStatistika();
					zobrazStatistikaActivity(statistika);
					//odosliStatistikuNaServer();
					return;
				}
			} else {
				test.aktUlohaIdx %= test.idckaUloh.length;
			}
			loadAktualnaOtazka();
			refreshOtazkaLabels();
			if (test.ohodnotene[test.aktUlohaIdx]) {
				skontrolujOdpovedeAVykresli();
			} else {
				// dalsia otazka nie je ohodnotena, takze potrebujeme vediet klikat
				setClickableOdpovede(true);
			}
		} else {
			// neni este ohodnotena, tak sa musi ohodnotit a az po dalsom stlaceni sa posunie dalej
			boolean uspesne = skontrolujOdpovedeAVykresli();
			if (uspesne) {
				test.uspesnych++;
			}
			System.out.println("uspesna odpoved= " + uspesne);
			test.ohodnotene[test.aktUlohaIdx] = true;
			setClickableOdpovede(false);
			updateStatistika(uspesne);

		}
	}


	private void zobrazStatistikaActivity(Statistika statistika) {
		System.out.println("zobrazStatistikaActivity");
		Intent test = new Intent(this, StatistikaActivity.class);
		test.putExtra(StatistikaActivity.STATISTIKA_BUNDLE_KEY, statistika);
		startActivity(test);
	}

	private Statistika getStatistika() {
		Statistika st = new Statistika();
		st.minusBodov = test.pocetMinusBodov;
		DecimalFormat df = new DecimalFormat("##.##");
		st.uspesnost = Double.parseDouble(df.format(test.uspesnych / (double) test.getOhodnotenych() * 100));
		st.uspesnych = test.uspesnych;
		st.vyriesenych = test.getOhodnotenych();
		st.zleZodpovedane = test.getZleZodpovedane();
		st.pribudlo = test.getPribudlo();
		st.ucenie = test.isUcenieSelected();
		st.trening = test.treningSelected;
		st.test = test.testSelected;
		return st;
	}

	private void setClickableOdpovede(boolean value) {
		for (int i = 0; i < odpovedeTextView.length; i++) {
			odpovedeTextView[i].setClickable(value);
		}
	}

	public void predButtonActionPerformed(View view) {
		System.out.println("predButtonActionPerformed");
		// ak je otazka uz ohodnotena, tak sa rovno vykreslia spravne odpovede
		if (test.ohodnotene[test.aktUlohaIdx]) {
			test.aktUlohaIdx--;
			if (test.aktUlohaIdx < 0) {
				// iba ak sme v uciacom mode mozeme prechadzat takto
				if (test.isUcenieSelected()) {
					test.aktUlohaIdx = test.idckaUloh.length - 1;
				} else {
					test.aktUlohaIdx++;
					return;
				}
			} else {
				test.aktUlohaIdx %= test.idckaUloh.length;
			}
			loadAktualnaOtazka();
			refreshOtazkaLabels();
			if (test.ohodnotene[test.aktUlohaIdx]) {
				skontrolujOdpovedeAVykresli();
			} else {
				// dalsia otazka nie je ohodnotena, takze potrebujeme vediet klikat
				// ale tu by to nemalo nikdy nastat
				setClickableOdpovede(true);
			}
		} else {
			// neni este ohodnotena, tak sa musi ohodnotit a az po dalsom stlaceni sa posunie DOZADU
			boolean uspesne = skontrolujOdpovedeAVykresli();
			if (uspesne) {
				test.uspesnych++;
			}
			test.ohodnotene[test.aktUlohaIdx] = true;
			setClickableOdpovede(false);
			updateStatistika(uspesne);
		}
	}

	private void updateStatistika(boolean uspesne) {
		if (test.idckaUloh[test.aktUlohaIdx][1] >= 0) { // pytame sa na povodny stav
			if (uspesne) {
				if (test.idckaUloh[test.aktUlohaIdx][1] < 3) {// pytame sa na povodny stav
					// sme uspesni a boli sme oranzovi, tak zmenime na zelenu
					test.idckaUloh[test.aktUlohaIdx][2] = test.idckaUloh[test.aktUlohaIdx][1] + 1;  // zapisujeme do aktualneho stavu
				} else {
					// sme uspesni a boli sme zeleny tak ostaneme zeleny s 3kou
				}
			} else {
				test.idckaUloh[test.aktUlohaIdx][2] = test.idckaUloh[test.aktUlohaIdx][1] - 1;     // zapisujeme do aktualneho stavu
			}
		} else {
			if (uspesne) {
				// bolo -1 a uhadli sme, tak davame hned na 1
				System.out.println("bolo -1 a uhadli sme, tak davame hned na 1");
				test.idckaUloh[test.aktUlohaIdx][2] = 1;     // zapisujeme do aktualneho stavu
			} else {
				// nie sme uspesni takze do aktualneho stavu musime zapisat -1
				test.idckaUloh[test.aktUlohaIdx][2] = -1;
			}
		}
		for (int i = 0; i < test.idckaUloh.length; i++) {
			System.out.print(String.format("%3s", Integer.toString(test.idckaUloh[i][1])));
		}
		System.out.println();
		for (int i = 0; i < test.idckaUloh.length; i++) {
			System.out.print(String.format("%3s", Integer.toString(test.idckaUloh[i][2])));
		}
		System.out.println();

		// updatneme statistiku
		Uri uri = StatistikaContentProvider.CONTENT_URI
				.buildUpon()// znamena ze zoberie zaklad sk.ics.upjs... a na nom nalepuje, nemusim uvazovat lomky
				.appendPath(String.valueOf(test.idckaUloh[test.aktUlohaIdx][0]))
				.build();
		ContentValues values = new ContentValues();
		values.put(Provider.Statistika.STATS, test.idckaUloh[test.aktUlohaIdx][2]);
		System.out.println("updating db with " + test.idckaUloh[test.aktUlohaIdx][2] + " for id " + test.aktUlohaIdx);

		AsyncQueryHandler updateHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				super.onUpdateComplete(token, cookie, result);
			}
		};
		updateHandler.startUpdate(STATISTIKA_UPDATE_TOKEN, NO_COOKIE, uri, values, NO_SELECTION, NO_SELECTION_ARGS);
	}

	private boolean skontrolujOdpovedeAVykresli() {
		int nespravnaOdpovedFarba = Color.RED;
		int spravnaOdpovedFarba = Color.GREEN;
		int spravnaNeoznacenaFarba = Color.MAGENTA;
		boolean uspesne = true;

		if (test.isUcenieSelected()) {
			nespravnaOdpovedFarba = spravnaOdpovedFarba;
			spravnaNeoznacenaFarba = spravnaOdpovedFarba;
		}
		//System.out.print("zaskrtnute: ");
		for (int i = 0; i < 8; i++) {
			//System.out.print(String.format("%3s", test.zaskrtnute[test.aktUlohaIdx][i]));
			// vyhodnotime itu odpoved, pozerame sa na odpovedeOrder lebo su v pomiesanom poradi
			if (test.zaskrtnute[test.aktUlohaIdx][i] == 1) {
				if (aktualnaOtazka.jeSpravne[test.odpovedeOrder[test.aktUlohaIdx][i]]) {
					// je oznacena spravna odpoved
					odpovedeTextView[i].setBackgroundColor(spravnaOdpovedFarba);
				} else {
					// je oznacena nespravna odpoved
					test.pocetMinusBodov--;
					uspesne = false;
					odpovedeTextView[i].setBackgroundColor(nespravnaOdpovedFarba);
				}
			} else {
				if (aktualnaOtazka.jeSpravne[test.odpovedeOrder[test.aktUlohaIdx][i]]) {
					// nie je oznacena spravna odpoved
					test.pocetMinusBodov--;
					uspesne = false;
					odpovedeTextView[i].setBackgroundColor(spravnaNeoznacenaFarba);
				}
			}
		}
		//System.out.println();
		return uspesne;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//saveTask();
	}

	public void odpoved1Clicked(View view) {
		System.out.println("odpoved1Clicked");
		test.zaskrtnute[test.aktUlohaIdx][0] = 1 - test.zaskrtnute[test.aktUlohaIdx][0];
		if (test.zaskrtnute[test.aktUlohaIdx][0] == 1) {
			odpovedeTextView[0].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[0].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved2Clicked(View view) {
		System.out.println("odpoved2Clicked");
		test.zaskrtnute[test.aktUlohaIdx][1] = 1 - test.zaskrtnute[test.aktUlohaIdx][1];
		if (test.zaskrtnute[test.aktUlohaIdx][1] == 1) {
			odpovedeTextView[1].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[1].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved3Clicked(View view) {
		System.out.println("odpoved3Clicked");
		test.zaskrtnute[test.aktUlohaIdx][2] = 1 - test.zaskrtnute[test.aktUlohaIdx][2];
		if (test.zaskrtnute[test.aktUlohaIdx][2] == 1) {
			odpovedeTextView[2].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[2].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved4Clicked(View view) {
		System.out.println("odpoved4Clicked");
		test.zaskrtnute[test.aktUlohaIdx][3] = 1 - test.zaskrtnute[test.aktUlohaIdx][3];
		if (test.zaskrtnute[test.aktUlohaIdx][3] == 1) {
			odpovedeTextView[3].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[3].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved5Clicked(View view) {
		System.out.println("odpoved5Clicked");
		test.zaskrtnute[test.aktUlohaIdx][4] = 1 - test.zaskrtnute[test.aktUlohaIdx][4];
		if (test.zaskrtnute[test.aktUlohaIdx][4] == 1) {
			odpovedeTextView[4].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[4].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved6Clicked(View view) {
		System.out.println("odpoved6Clicked");
		test.zaskrtnute[test.aktUlohaIdx][5] = 1 - test.zaskrtnute[test.aktUlohaIdx][5];
		if (test.zaskrtnute[test.aktUlohaIdx][5] == 1) {
			odpovedeTextView[5].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[5].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved7Clicked(View view) {
		System.out.println("odpoved7Clicked");
		test.zaskrtnute[test.aktUlohaIdx][6] = 1 - test.zaskrtnute[test.aktUlohaIdx][6];
		if (test.zaskrtnute[test.aktUlohaIdx][6] == 1) {
			odpovedeTextView[6].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[6].setBackgroundColor(Color.WHITE);
		}
	}

	public void odpoved8Clicked(View view) {
		System.out.println("odpoved8Clicked");
		test.zaskrtnute[test.aktUlohaIdx][7] = 1 - test.zaskrtnute[test.aktUlohaIdx][7];
		if (test.zaskrtnute[test.aktUlohaIdx][7] == 1) {
			odpovedeTextView[7].setBackgroundColor(Color.CYAN);
		} else {
			odpovedeTextView[7].setBackgroundColor(Color.WHITE);
		}
	}

	private boolean getBoolean(int cislo) {
		if (cislo == 0) {
			return false;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		System.out.println("onBackPressed");
		//Toast.makeText(this, "ulozili sme sa", Toast.LENGTH_SHORT).show();
	}


}
