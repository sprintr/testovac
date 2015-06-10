package sk.jmurin.android.testovac.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.List;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static sk.jmurin.android.testovac.provider.Defaults.*;

public class StatistikaContentProvider extends ContentProvider {
    public static final String AUTHORITY = "sk.jmurin.android.testovac.provider.StatistikaContentProvider";

    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(Provider.Statistika.TABLE_NAME)
            .build();

    private static final int URI_MATCH_STATS = 0;
    private static final int URI_MATCH_STATS_BY_ID = 1;
    private static final int URI_MATCH_STATS_NO_NOTIFY = 2;

    private static final String MIME_TYPE_STATS = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "." + Provider.Statistika.TABLE_NAME;
    private static final String MIME_TYPE_SINGLE_STAT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + "." + Provider.Statistika.TABLE_NAME;

    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private DatabaseOpenHelper databaseHelper;

    @Override
    public boolean onCreate() {
        uriMatcher.addURI(AUTHORITY, Provider.Statistika.TABLE_NAME, URI_MATCH_STATS);
        uriMatcher.addURI(AUTHORITY, Provider.Statistika.TABLE_NAME + "/#", URI_MATCH_STATS_BY_ID);
        uriMatcher.addURI(AUTHORITY, Provider.Statistika.TABLE_NAME + "/noNotify/#", URI_MATCH_STATS_NO_NOTIFY);

        this.databaseHelper = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case URI_MATCH_STATS:
                System.out.println("namatchovane statistiky");
                cursor = listStats();
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
//            case URI_MATCH_STATS_BY_TOP_ID:
//                System.out.println("namatchovane statistiky TOP ID");
//                cursor = findByTopID();
//                cursor.setNotificationUri(getContext().getContentResolver(), uri);
//                return cursor;
            default:
                return Defaults.NO_CURSOR;
        }
    }

    private Cursor findByTopID() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return db.query(Provider.Statistika.TABLE_NAME, ALL_COLUMNS, NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, Provider.Statistika._ID + " DESC LIMIT 1");
    }

    private Cursor listStats() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return db.query(Provider.Statistika.TABLE_NAME, ALL_COLUMNS, NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, Provider.Statistika._ID + " ASC");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
//        switch(uriMatcher.match(uri)) {
//            case URI_MATCH_STATS:
//                Uri newItemUri = saveNote(values);
//                getContext().getContentResolver().notifyChange(CONTENT_URI, NO_CONTENT_OBSERVER);
//                return newItemUri;
//            default:
//                return Defaults.NO_URI;
//        }
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
//        switch(uriMatcher.match(uri)) {
//            case URI_MATCH_STATS_BY_TOP_ID:
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
            case URI_MATCH_STATS_BY_ID:
                return MIME_TYPE_SINGLE_STAT;
            case URI_MATCH_STATS:
                return MIME_TYPE_STATS;
        }
        return NO_TYPE;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_MATCH_STATS_BY_ID:
                long id = ContentUris.parseId(uri);
                int affectedRows = databaseHelper.getWritableDatabase()
                        .update(Provider.Statistika.TABLE_NAME,
                                values,
                                Provider.Statistika._ID + "=" + id,
                                NO_SELECTION_ARGS);
                System.out.println("StatistikaContentProvider: " + id + "=" + values.getAsInteger(Provider.Statistika.STATS));
                getContext().getContentResolver().notifyChange(CONTENT_URI, NO_CONTENT_OBSERVER);
                return affectedRows;
            case URI_MATCH_STATS_NO_NOTIFY:
                List<String> pathSegments = uri.getPathSegments();
                long idcko = Integer.parseInt(pathSegments.get(2));
                int affectedRows2 = databaseHelper.getWritableDatabase()
                        .update(Provider.Statistika.TABLE_NAME,
                                values,
                                Provider.Statistika._ID + "=" + idcko,
                                NO_SELECTION_ARGS);
                System.out.println("StatistikaContentProvider(noNotify): " + idcko + "=" + values.getAsInteger(Provider.Statistika.STATS));
                //getContext().getContentResolver().notifyChange(CONTENT_URI, NO_CONTENT_OBSERVER);
                return affectedRows2;
            default:
                return 0;
        }
    }

//        private Uri updateStat(ContentValues values) {
//        ContentValues note = new ContentValues();
//        note.put(Provider.Statistika.STATS, note.getAsInteger(Provider.Statistika.STATS));
//
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//        long newId = db.update(Provider.Statistika.TABLE_NAME,note,Provider.Statistika._ID+"="+values.getAsInteger(Provider.Statistika.STATS),)
//        return ContentUris.withAppendedId(CONTENT_URI, newId);
//    }
}
