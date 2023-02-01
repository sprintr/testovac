package com.testovac.provider;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static com.testovac.provider.Defaults.ALL_COLUMNS;
import static com.testovac.provider.Defaults.NO_GROUP_BY;
import static com.testovac.provider.Defaults.NO_HAVING;
import static com.testovac.provider.Defaults.NO_SELECTION;
import static com.testovac.provider.Defaults.NO_SELECTION_ARGS;
import static com.testovac.provider.Defaults.NO_SORT_ORDER;
import static com.testovac.provider.Defaults.NO_TYPE;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.List;

public class BiologiaContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.testovac.provider.BiologiaContentProvider";

	public static final Uri CONTENT_URI = new Uri.Builder()
			.scheme(SCHEME_CONTENT)
			.authority(AUTHORITY)
			.appendPath(Provider.Biologia.TABLE_NAME)
			.build();

	private static final int URI_MATCH_OTAZKY = 0;
	private static final int URI_MATCH_OTAZKA_BY_ID = 1;
	private static final int URI_MATCH_OTAZKY_BY_RANGE = 2;
	private static final int URI_MATCH_OTAZKY_BY_VYBRANE = 3;

	private static final String MIME_TYPE_OTAZKY = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "." + Provider.Biologia.TABLE_NAME;
	private static final String MIME_TYPE_SINGLE_OTAZKA = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + "." + Provider.Biologia.TABLE_NAME;

	private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	private DatabaseOpenHelper databaseHelper;

	@Override
	public boolean onCreate() {
		uriMatcher.addURI(AUTHORITY, Provider.Biologia.TABLE_NAME, URI_MATCH_OTAZKY);
		uriMatcher.addURI(AUTHORITY, Provider.Biologia.TABLE_NAME + "/#", URI_MATCH_OTAZKA_BY_ID);
		uriMatcher.addURI(AUTHORITY, Provider.Biologia.TABLE_NAME + "/#/#", URI_MATCH_OTAZKY_BY_RANGE);
		uriMatcher.addURI(AUTHORITY, Provider.Biologia.TABLE_NAME + "/vybrane", URI_MATCH_OTAZKY_BY_VYBRANE);

		this.databaseHelper = new DatabaseOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	                    String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		switch (uriMatcher.match(uri)) {
			case URI_MATCH_OTAZKY:
				System.out.println("namatchovane otazky");
				cursor = listOtazky();
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				return cursor;
			case URI_MATCH_OTAZKA_BY_ID:
				System.out.println("namatchovane otazky ID");
				long id = ContentUris.parseId(uri);
				cursor = findById(id);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				return cursor;
			case URI_MATCH_OTAZKY_BY_RANGE:
				System.out.println("namatchovane otazky RANGE");
				List<String> pathSegments = uri.getPathSegments();
				int min = Integer.parseInt(pathSegments.get(1));
				int max = Integer.parseInt(pathSegments.get(2));

				cursor = getOtazkyFromRozsah(min, max);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				return cursor;
			case URI_MATCH_OTAZKY_BY_VYBRANE:
				System.out.println("namatchovane otazky VYBRANE");
				cursor = getOtazkyFromVybrane(selection);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				return cursor;
			default:
				return Defaults.NO_CURSOR;
		}
	}

	private Cursor getOtazkyFromVybrane(String selection) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		return db.query(Provider.Biologia.TABLE_NAME, ALL_COLUMNS, selection, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
	}

	private Cursor getOtazkyFromRozsah(int min, int max) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		String selection = Provider.Biologia._ID + ">" + (min - 1) + " AND " + Provider.Biologia._ID + "<" + (max + 1);
		return db.query(Provider.Biologia.TABLE_NAME, ALL_COLUMNS, selection, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
	}

	private Cursor findById(long id) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		String selection = Provider.Biologia._ID + "=" + id;
		return db.query(Provider.Biologia.TABLE_NAME, ALL_COLUMNS, selection, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
	}

	private Cursor listOtazky() {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		return db.query(Provider.Biologia.TABLE_NAME, ALL_COLUMNS, NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, Provider.Biologia._ID + " ASC");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("Not yet implemented");
//        switch(uriMatcher.match(uri)) {
//            case URI_MATCH_OTAZKY:
//                Uri newItemUri = saveNote(values);
//                getContext().getContentResolver().notifyChange(CONTENT_URI, NO_CONTENT_OBSERVER);
//                return newItemUri;
//            default:
//                return Defaults.NO_URI;
//        }
	}

//    private Uri saveNote(ContentValues values) {
//        throw new UnsupportedOperationException("Not yet implemented");
//        ContentValues note = new ContentValues();
//        note.put(Provider.Biologia._ID, AUTOGENERATED_ID);
//        note.put(Provider.Biologia.DESCRIPTION, values.getAsString(Provider.Biologia.DESCRIPTION));
//        note.put(Provider.Biologia.TIMESTAMP, new Date().getTime() / 1000);
//
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//        long newId = db.insert(Provider.Biologia.TABLE_NAME, NO_NULL_COLUMN_HACK, note);
//        return ContentUris.withAppendedId(CONTENT_URI, newId);
//    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("Not yet implemented");
//        switch(uriMatcher.match(uri)) {
//            case URI_MATCH_OTAZKA_BY_ID:
//                long id = ContentUris.parseId(uri);
//                int affectedRows = databaseHelper.getWritableDatabase()
//                        .delete(Provider.Biologia.TABLE_NAME, Provider.Biologia._ID + " = " + id, Defaults.NO_SELECTION_ARGS);
//                getContext().getContentResolver().notifyChange(CONTENT_URI, NO_CONTENT_OBSERVER);
//                return affectedRows;
//            default:
//                return 0;
//        }
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case URI_MATCH_OTAZKA_BY_ID:
				return MIME_TYPE_SINGLE_OTAZKA;
			case URI_MATCH_OTAZKY:
				return MIME_TYPE_OTAZKY;
		}
		return NO_TYPE;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,
	                  String[] selectionArgs) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
