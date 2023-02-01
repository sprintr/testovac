package com.testovac.provider;

import android.provider.BaseColumns;

public interface Provider {
	public interface Biologia extends BaseColumns {
		public static final String TABLE_NAME = "biologia";

		public static final String OTAZKA = "otazka";
		public static final String ODPOVED_1 = "odpoved_1";
		public static final String ODPOVED_2 = "odpoved_2";
		public static final String ODPOVED_3 = "odpoved_3";
		public static final String ODPOVED_4 = "odpoved_4";
		public static final String ODPOVED_5 = "odpoved_5";
		public static final String ODPOVED_6 = "odpoved_6";
		public static final String ODPOVED_7 = "odpoved_7";
		public static final String ODPOVED_8 = "odpoved_8";
		public static final String JESPRAVNA_1 = "jespravna_1";
		public static final String JESPRAVNA_2 = "jespravna_2";
		public static final String JESPRAVNA_3 = "jespravna_3";
		public static final String JESPRAVNA_4 = "jespravna_4";
		public static final String JESPRAVNA_5 = "jespravna_5";
		public static final String JESPRAVNA_6 = "jespravna_6";
		public static final String JESPRAVNA_7 = "jespravna_7";
		public static final String JESPRAVNA_8 = "jespravna_8";
	}

	public interface Statistika extends BaseColumns {
		public static final String TABLE_NAME = "statistika";
		public static final String STATS = "stats";
	}

	public interface Mapa extends BaseColumns {
		public static final String TABLE_NAME = "mapa";
		public static final String KEY = "key";
		public static final String VALUE = "value";
	}
}
