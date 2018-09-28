package com.example.android.inventorystage2;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventorystage2.data.BooksContract;


public class BooksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final  int BOOK_LOADER = 0;
    BooksCursorAdapter mCursorAdapter ;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BooksContract.BookEntry._ID,
                BooksContract.BookEntry.COLUMN_BOOK_NAME,
                BooksContract.BookEntry.COLUMN_BOOK_PRICE,
                BooksContract.BookEntry.COLUMN_BOOK_QUANTITY};

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this,
                BooksContract.BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        ListView booksListView =  findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        booksListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of book data in the Cursor.
        mCursorAdapter = new BooksCursorAdapter(this, null);
        // Attach the adapter to the ListView.
        booksListView.setAdapter(mCursorAdapter);

        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(BooksActivity.this, EditorActivity.class);
                Uri selectedBookUri = ContentUris.withAppendedId(BooksContract.BookEntry.CONTENT_URI,id);
                intent.setData(selectedBookUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(BOOK_LOADER,null,this);
    }

    /**
     * Helper method to insert hardcoded book details data into the database. For debugging purposes only.
     */
    private void insertBook() {

        // Create a ContentValues object where column names are the keys,
        // and books attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BooksContract.BookEntry.COLUMN_BOOK_NAME, getString(R.string.book_name));
        values.put(BooksContract.BookEntry.COLUMN_BOOK_PRICE, getString(R.string.book_price));
        values.put(BooksContract.BookEntry.COLUMN_BOOK_QUANTITY,getString(R.string.book_quantity));
        values.put(BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER, getString(R.string.book_supplier_name));
        values.put(BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, getString(R.string.book_supplier_phone_number));

        // Insert a new row for the book into the provider using the ContentResolver.
        // Use the {@link BookEntry#CONTENT_URI} to indicate that we want to insert
        // into the books database table.
        // Receive the new content URI that will allow us to access the book's data in the future.
        try {
            Uri newUri = getContentResolver().insert(BooksContract.BookEntry.CONTENT_URI, values);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Helper method to delete all book details data into the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = 0;
        rowsDeleted  = getContentResolver().delete(BooksContract.BookEntry.CONTENT_URI,null,null);
        if(rowsDeleted > 0){
            Toast.makeText(BooksActivity.this,getString(R.string.books_deleted_success),
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(BooksActivity.this,getString(R.string.books_deleted_failed),
                    Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_books, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete All data" menu option
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
            // Respond to a click on the "Save" menu option
            case R.id.action_add:
                Intent intent = new Intent(BooksActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}