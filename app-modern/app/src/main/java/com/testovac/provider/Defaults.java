package com.testovac.provider;

import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public interface Defaults {
	/**
	 * Corresponds to all columns (*) in SELECT query.
	 */
	public static final String[] ALL_COLUMNS = null;

	/**
	 * Corresponds to an empty WHERE clause, i. e.
	 * condition that matches any row.
	 */
	public static final String NO_SELECTION = null;

	/**
	 * Corresponds to default (none) selection arguments.
	 */
	public static final String[] NO_SELECTION_ARGS = null;

	/**
	 * Corresponds to default sort order, or
	 * missing ORDER BY clause in SQL SELECT query.
	 */
	public static final String NO_SORT_ORDER = null;

	/**
	 * Corresponds to omitted GROUP BY clause in SQL SELECT query.
	 */
	public static final String NO_GROUP_BY = null;

	/**
	 * Corresponds to omitted HAVING clause in SQL SELECT query.
	 */
	public static final String NO_HAVING = null;

	/**
	 * Corresponds to the autogenerated value (NULL)
	 * for a column declared as PRIMARY KEY AUTOINCREMENT.
	 * Usually it is used in the {@link android.content.ContentValues} to explicitely
	 * indicate autogenerated column.
	 */
	public static final String AUTOGENERATED_ID = null;

	/**
	 * Corresponds to the default {@link SQLiteDatabase.CursorFactory} used in the
	 * constructor of {@link android.database.sqlite.SQLiteOpenHelper} subclasses.
	 */
	public static final SQLiteDatabase.CursorFactory DEFAULT_CURSOR_FACTORY = null;

	/**
	 * Denotes a NULL column hack not being used in the
	 * {@link SQLiteDatabase#insert(String, String, android.content.ContentValues)}
	 * method call.
	 */
	public static final String NO_NULL_COLUMN_HACK = null;

	/**
	 * Denotes a missing or not-yet specified Cursor. Usually used in the
	 * {@link android.widget.SimpleCursorAdapter} constructor to denote
	 * a cursor which will be set later via Loader.
	 */
	public static final Cursor NO_CURSOR = null;

	/**
	 * Denotes a default or missing ContentObserver
	 */
	public static final ContentObserver NO_CONTENT_OBSERVER = null;

	/**
	 * Denotes a position of a cursor that is just before the first
	 * row.
	 */
	public static final int BEFORE_FIRST = -1;

	/**
	 * Denotes a default set of flags used in the constructor
	 * of the {@link android.widget.SimpleCursorAdapter}.
	 */
	public static final int NO_FLAGS = 0;

	/**
	 * Denotes an empty cookie object in the {@link android.content.AsyncQueryHandler}
	 * and its <code>on***Complete</code> methods.
	 */
	public static final Object NO_COOKIE = null;

	/**
	 * Denotes an empty cookie object in the {@link android.content.AsyncQueryHandler}
	 * and its <code>on***Complete</code> methods.
	 */
	public static final String NO_TYPE = null;

	/**
	 * Represents an empty URI returned from methods of the {@link android.content.ContentProvider}
	 * when the {@link Uri} in the argument does not match
	 * any of the rules in the {@link android.content.UriMatcher}.
	 */
	public static final Uri NO_URI = null;

	/**
	 * Represents a default handler of the {@link android.app.AlertDialog}
	 * <code>OnClickListener</code>. This handler does not do anything,
	 * it just dismissed a dialog.
	 */
	public static final DialogInterface.OnClickListener DISMISS_ACTION = null;
}
