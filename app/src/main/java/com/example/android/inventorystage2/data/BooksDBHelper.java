package com.example.android.inventorystage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper for books app. Manages database creation and version management.
 */

public class BooksDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = BooksDBHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link BooksDBHelper}.
     *
     * @param context of the app
     */
    public BooksDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOG_TAG, "Creating the table - " + BooksContract.BookEntry.TABLE_NAME);
        // Create a String that contains the SQL statement to create the Bookstore table
        String SQL_CREATE_BOOK_DETAILS_TABLE = "CREATE TABLE " + BooksContract.BookEntry.TABLE_NAME + " ("
                + BooksContract.BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BooksContract.BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                + BooksContract.BookEntry.COLUMN_BOOK_PRICE + " TEXT, "
                + BooksContract.BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL, "
                + BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER + " TEXT, "
                + BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOK_DETAILS_TABLE);
        Log.i(LOG_TAG, "Table created successfully" );

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
