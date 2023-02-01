package com.testovac;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

/**
 * Created by Janco1 on 31. 5. 2015.
 */
public class ObrazokGenerator {

	public static Bitmap getDefaultObrazok(int[] stats) {
		//System.out.println("generujem default obrazok");
		double sirkaObrazkaPx = 1015;
		double vyskaObrazkaPx = 165;
		//System.out.println("rozmery default obrazka: "+sirkaObrazkaPx+" "+vyskaObrazkaPx);
		int stvorcekSize = (int) Math.floor(Math.sqrt(sirkaObrazkaPx * vyskaObrazkaPx / 1500.0));
		if (stvorcekSize == 0) {
			return null;
		}
		//System.out.println("velkost default stvorceka: "+stvorcekSize);
		int sirka = (int) (sirkaObrazkaPx / stvorcekSize);
		int vyska = (int) (vyskaObrazkaPx / stvorcekSize);
		Bitmap obrazok = Bitmap.createBitmap(sirka * stvorcekSize, vyska * stvorcekSize, Bitmap.Config.ARGB_8888);

		for (int i = 1; i < 1501; i++) {
			int x = (i - 1) % sirka;
			int y = (i - 1) / sirka;
			if (stats[i] > 2) {
				drawCircle(obrazok, x, y, Color.GREEN, false, stvorcekSize);
			} else {
				if (stats[i] > 1) {
					drawCircle(obrazok, x, y, Color.rgb(255, 153, 0), false, stvorcekSize); //oranzova
				} else {
					if (stats[i] > 0) {
						drawCircle(obrazok, x, y, Color.YELLOW, false, stvorcekSize);
					} else {
						if (stats[i] > -1) {
							drawCircle(obrazok, x, y, Color.WHITE, false, stvorcekSize);
						} else {
							drawCircle(obrazok, x, y, Color.RED, false, stvorcekSize);
						}
					}
				}
			}
		}

		return obrazok;
	}

	public static Bitmap nakresliObrazok(int[] stats, ImageView statImageView, int min, int max) {
		System.out.println("rozmery obrazka: " + statImageView.getWidth() + " " + statImageView.getHeight());
		int stvorcekSize = (int) Math.floor(Math.sqrt(statImageView.getWidth() * statImageView.getHeight() / 1500.0));
		if (stvorcekSize == 0) {
			return null;
		}
		System.out.println("velkost stvorceka: " + stvorcekSize);
		int match = statImageView.getWidth() / stvorcekSize;
		int high = statImageView.getHeight() / stvorcekSize;
		Bitmap picture = Bitmap.createBitmap(match * stvorcekSize, high * stvorcekSize, Bitmap.Config.ARGB_8888);
		boolean[][] math = new boolean[high][match];
//        int min = Integer.parseInt(String.valueOf(minEditText.getText()));
//        int max = Integer.parseInt(String.valueOf(maxEditText.getText()));

		for (int i = 1; i < 1501; i++) {
			int x = (i - 1) % match;
			int y = (i - 1) / match;

			if (y < math.length && x < math[y].length) {
				if (i >= min && i <= max) {
					math[y][x] = true;
				}

				if (stats[i] > 2) {
					drawCircle(picture, x, y, Color.GREEN, math[y][x], stvorcekSize);
				} else {
					if (stats[i] > 1) {
						drawCircle(picture, x, y, Color.rgb(255, 153, 0), math[y][x], stvorcekSize); //oranzova
					} else {
						if (stats[i] > 0) {
							drawCircle(picture, x, y, Color.YELLOW, math[y][x], stvorcekSize);
						} else {
							if (stats[i] > -1) {
								drawCircle(picture, x, y, Color.WHITE, math[y][x], stvorcekSize);
							} else {
								drawCircle(picture, x, y, Color.RED, math[y][x], stvorcekSize);
							}
						}
					}
				}
			}
		}

		// nakreslime zvoleny rozsah
		for (int x = 0; x < math[0].length; x++) {
			for (int y = 0; y < math.length; y++) {
				if (math[y][x]) {
					if (niejeSused(y, x, -1, 0, math)) {
						nakresliCiaru(y, x, -1, 0, picture, Color.BLACK, 1, stvorcekSize);
					}
					if (niejeSused(y, x, 1, 0, math)) {
						nakresliCiaru(y, x, 1, 0, picture, Color.BLACK, 1, stvorcekSize);
					}
					if (niejeSused(y, x, 0, 1, math)) {
						nakresliCiaru(y, x, 0, 1, picture, Color.BLACK, 1, stvorcekSize);
					}
					if (niejeSused(y, x, 0, -1, math)) {
						nakresliCiaru(y, x, 0, -1, picture, Color.BLACK, 1, stvorcekSize);
					}
				}
			}
		}
		//System.out.println("klikX: "+klikX+" klikY: "+klikY);
//        if (klikX == 0 && klikY == 0) {
//            // nekreslime stvorcek
//        } else {
//            int x = klikX / 8;
//            int y = klikY / 8;
//            nakresliCiaru(y, x, 0, 1, obrazok, Color.DKGRAY, 2, stvorcekSize);
//            nakresliCiaru(y, x, 0, -1, obrazok, Color.DKGRAY, 2, stvorcekSize);
//            nakresliCiaru(y, x, 1, 0, obrazok, Color.DKGRAY, 2, stvorcekSize);
//            nakresliCiaru(y, x, -1, 0, obrazok, Color.DKGRAY, 2, stvorcekSize);
//        }
		//grafikaLabel.setIcon(new ImageIcon(obrazok));
		//statImageView.setImageBitmap(obrazok);
		return picture;
	}

	private static boolean niejeSused(int y, int x, int yp, int xp, boolean[][] matica) {
		if (y + yp < 0 || y + yp > matica.length - 1) {
			return true;
		}
		if (x + xp < 0 || x + xp > matica[0].length - 1) {
			return true;
		}
		if (matica[y + yp][x + xp]) {
			return false;
		} else {
			return true;
		}
		// return false;
	}

	private static void nakresliCiaru(int y, int x, int yp, int xp, Bitmap obrazok, int farba, int hrubka, int stvorcekSize) {
		//System.out.println("x:" + x + " y:" + y);
		for (int k = 0; k < hrubka; k++) {
			if (yp == -1) {
				for (int i = 0; i < stvorcekSize; i++) {
					obrazok.setPixel(x * stvorcekSize + i, y * stvorcekSize + k, farba);
				}
			}
			if (yp == 1) {
				for (int i = 0; i < stvorcekSize; i++) {
					obrazok.setPixel(x * stvorcekSize + i, y * stvorcekSize + (stvorcekSize - 1) - k, farba);
				}
			}
			if (xp == -1) {
				for (int i = 0; i < stvorcekSize; i++) {
					obrazok.setPixel(x * stvorcekSize + k, y * stvorcekSize + i, farba);
				}
			}
			if (xp == 1) {
				for (int i = 0; i < stvorcekSize; i++) {
					obrazok.setPixel(x * stvorcekSize + (stvorcekSize - 1) - k, y * stvorcekSize + i, farba);
				}
			}
		}
	}

	private static void drawCircle(Bitmap obrazok, int x, int y, int farba, boolean jeVRozsahu, int stvorcekSize) {
		// nakreslime stvorcek
		for (int k = 0; k < stvorcekSize; k++) {
			for (int l = 0; l < stvorcekSize; l++) {
				obrazok.setPixel(x * stvorcekSize + k, y * stvorcekSize + l, farba);
			}
		}
		if (jeVRozsahu) {
//            int kx = klikX / 8;
//            int ky = klikY / 8;
//            if (ky == y && kx == x) {
//                // nekreslime bodku do oznaceneho stvorceka
//            } else {
			obrazok.setPixel(x * stvorcekSize + (stvorcekSize / 2 - 1), y * stvorcekSize + (stvorcekSize / 2 - 1), Color.BLACK);
			//}
		}
	}

}
