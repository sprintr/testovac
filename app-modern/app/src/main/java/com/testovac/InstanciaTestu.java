package com.testovac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Janco1 on 26. 5. 2015.
 */
public class InstanciaTestu implements Serializable {

	private boolean ucenieSelected;
	boolean treningSelected;
	boolean testSelected;
	int[][] idckaUloh;
	int aktUlohaIdx = 0;
	int uspesnych;
	int pocetMinusBodov;
	int[][] odpovedeOrder;
	List<Integer> cisla = new ArrayList<>();
	int[][] zaskrtnute;
	boolean[] ohodnotene;

	public InstanciaTestu(List<Otazka> otazky, int[] stats) {// stats je komplet zoznam statistiky pre kazdu ulohu, 1500
		cisla.add(0);
		cisla.add(1);
		cisla.add(2);
		cisla.add(3);
		cisla.add(4);
		cisla.add(5);
		cisla.add(6);
		cisla.add(7);
		// zapiseme si idcka otazok ktore chceme
		idckaUloh = new int[otazky.size()][3];//0- idcko, 1- povodna statistika, 2- aktualna statistika
		odpovedeOrder = new int[otazky.size()][8]; // nahodne rozmiesame kazdu z odpovedi
		zaskrtnute = new int[otazky.size()][8]; // aby sme vedeli ktore sme ako zaskrtli
		ohodnotene = new boolean[otazky.size()];  // aby sme vedeli ktore ulohy su uz ohodnotene aby sa hned vykreslili
		for (int i = 0; i < otazky.size(); i++) {
			idckaUloh[i][0] = otazky.get(i).id;
			idckaUloh[i][1] = stats[otazky.get(i).id];
			Collections.shuffle(cisla); // pomiesame poradie odpovedi
			for (int j = 0; j < 8; j++) {
				odpovedeOrder[i][j] = cisla.get(j);
			}
		}
	}

	public boolean isUcenieSelected() {
		return ucenieSelected;
	}

	public void setUcenieSelected(boolean ucenieSelected) {
		this.ucenieSelected = ucenieSelected;
		if (ucenieSelected) {
			Arrays.fill(ohodnotene, true);
			System.out.println("ucenie selected: " + Arrays.toString(ohodnotene));
		}
	}

	public int getOhodnotenych() {
		int pocet = 0;
		for (int i = 0; i < ohodnotene.length; i++) {
			if (ohodnotene[i]) {
				pocet++;
			}
		}
		return pocet;

	}

	public int[] getZleZodpovedane() {
		// iba z tych co su ohodnotene chceme vybrat tie idcka, kde sme zle odpovedali
		System.out.println("getZleZodpovedane: ");
		System.out.println("ohodnotenych: " + getOhodnotenych() + "  uspesnych: " + uspesnych);
		int[] zle = new int[getOhodnotenych() - uspesnych];
		int pocet = 0;
		for (int i = 0; i < idckaUloh.length; i++) {
			if (ohodnotene[i]) {
				if (idckaUloh[i][1] >= 0) {
					if (idckaUloh[i][1] > idckaUloh[i][2]) {
						// zle sme odpovedali, znizil sa rating
						zle[pocet] = idckaUloh[i][0];
						pocet++;
						//System.out.println("zle zodpovedane: ["+i+"] "+idckaUloh[i][1]+">"+idckaUloh[i][2]);
					}
				} else {
					if (idckaUloh[i][1] == idckaUloh[i][2]) {
						// obidve idcka su -1, takze znova zla odpoved
						zle[pocet] = idckaUloh[i][0];
						pocet++;
						//System.out.println("zle zodpovedane: ["+i+"] "+idckaUloh[i][1]+"=="+idckaUloh[i][2]);
					}
				}
			}
		}
		if (pocet != zle.length) {
			throw new RuntimeException("pocet zlych sa nerovna poctu zlych");
		}
		return zle;
	}

	public int[] getPribudlo() {
		int[] pribudlo = new int[5];
		int zelenych = 0;
		int oranzovych = 0;
		int zltych = 0;
		int bielych = 0;
		int cervenych = 0;
		int povodnaStatistika;
		int aktualnaStatistika;
		for (int i = 1; i < idckaUloh.length; i++) {
			povodnaStatistika = idckaUloh[i][1];
			aktualnaStatistika = idckaUloh[i][2];
			// ci je uspesna
			if (povodnaStatistika < aktualnaStatistika) {
				// uloha bola urcite uspesna
				switch (idckaUloh[i][1]) {
					case 0:
						zltych++;
						bielych--;
						break;
					case 1:
						oranzovych++;
						zltych--;
						break;
					case 2:
						zelenych++;
						oranzovych--;
						break;
					case -1:
						zltych++;
						cervenych--;
						break;
					default:
						;
				}
			} else {
				// povodna >= aktualna statistika
				// ak bola povodna -1 alebo 3, tak sa mozu rovnat povodna a aktualna
				// urcite sa nemozu rovnat ak bola povodna 0, 1, 2
				switch (povodnaStatistika) {
					case 0:
						cervenych++;
						bielych--;
						break;
					case 1: // biela pribudla
						bielych++;
						zltych--;
						break;
					case 2:
						oranzovych--;
						zltych++;
						break;
					case 3:
						if (povodnaStatistika == aktualnaStatistika) {
							// uloha bola urcite uspesna, iba sa 3 nezmenila na 4
						} else {
							//uloha bola urcite neuspesna lebo povodna > aktualna
							zelenych--;
							oranzovych++;
						}
						break;
					case -1:
						// predosla bola -1 a aktualna == povodna, lebo mensia nemoze byt a vyssia je v prvom ife
						// teda znova je uloha neuspesna a pocet cervenych sa nezvysil
						// z cervenej sa preslo znova do cervenej
						break;
					default:
						;
				}
			}
		}
		pribudlo[0] = cervenych;
		pribudlo[1] = bielych;
		pribudlo[2] = zltych;
		pribudlo[3] = oranzovych;
		pribudlo[4] = zelenych;

		return pribudlo;
	}
}
