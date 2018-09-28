package com.example.android.inventorystage2;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.inventorystage2.data.BooksContract;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri currentBookUri = null;

    private boolean mBookHasChanged = false;

    /** EditText field to enter the product's name */
    private TextInputEditText mProductNameEditText;

    /** EditText field to enter the product's price */
    private TextInputEditText mBookPriceEditText;

    /** EditText field to enter the product's quantity */
    private TextInputEditText mBookQuantityEditText;

    /** EditText field to enter the supplier's name */
    private TextInputEditText mSupplierNameEditText;

    /** EditText field to enter the supplier's phone number */
    private TextInputEditText mSupplierPhoneNumberEditText;


    /**
     *  This method listens for any user touches on a View, implying that they are modifying
     // the view, and we change the mBookHasChanged boolean to true
     *
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentBookUri = intent.getData();
        if(currentBookUri == null){
            //action is to save new book details
            setTitle(getResources().getText(R.string.editor_activity_title_new_book));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else{
            //action is to save existing book details
            setTitle(getResources().getText(R.string.editor_activity_title_existing_book));
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
        // Find all relevant views that we will need to read user input from
        mProductNameEditText =  findViewById(R.id.edit_book_name);
        mBookPriceEditText =  findViewById(R.id.edit_book_price);
        mBookQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumberEditText = findViewById(R.id.edit_supplier_phone_number);

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mBookPriceEditText.setOnTouchListener(mTouchListener);
        mBookQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        Button saveButton =  findViewById(R.id.action_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBook();
                finish();
            }
        });

        //called when phone icon is pressed.
        Button callActionButton =  findViewById(R.id.action_call);
        callActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                        mSupplierPhoneNumberEditText.getText().toString(), null));
                startActivity(intent);
                finish();
            }
        });

        //called when action_decrease button is clicked.
        Button decreaseBookQuantityButton =  findViewById(R.id.action_decrease);
        decreaseBookQuantityButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                int bookQuantity;
                String bookQuantityString = mBookQuantityEditText.getText().toString();
                if(!TextUtils.isEmpty(bookQuantityString)) {
                    bookQuantity  = Integer.parseInt(bookQuantityString);
                    if(bookQuantity > 0) {
                        //update the flag to true, as the bookQuantity data is changed.
                        mBookHasChanged = true;
                        bookQuantity = bookQuantity - 1;
                        mBookQuantityEditText.setText(Integer.toString(bookQuantity));
                    }else{
                        mBookQuantityEditText.setText(Integer.toString(bookQuantity));
                    }
                }
            }
        });

        //called when action_increase button is clicked.
        Button increaseBookQuantityButton =  findViewById(R.id.action_increase);
        increaseBookQuantityButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                //update the flag to true, as the bookQuantity data is changed.
                mBookHasChanged = true;
                int bookQuantity = 0;
                String bookQuantityString = mBookQuantityEditText.getText().toString();
                if(!TextUtils.isEmpty(bookQuantityString)) {
                    bookQuantity  = Integer.parseInt(bookQuantityString);
                    bookQuantity = bookQuantity + 1;
                    mBookQuantityEditText.setText(Integer.toString(bookQuantity));
                } else if(currentBookUri == null){
                    bookQuantity = bookQuantity + 1;
                    mBookQuantityEditText.setText(Integer.toString(bookQuantity));
                }
            }
        });
    }

    /**
     * Get user input from editor and save new/existing book details into database.
     */
    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productName = mProductNameEditText.getText().toString().trim();
        String bookQuantity = (mBookQuantityEditText.getText().toString());
        String bookPrice = mBookPriceEditText.getText().toString();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierPhone = mSupplierPhoneNumberEditText.getText().toString().trim();
        //check if the values are entered for the new book
        if (currentBookUri == null &&
                TextUtils.isEmpty(productName) &&  TextUtils.isEmpty(bookQuantity)
                && TextUtils.isEmpty(bookPrice) && TextUtils.isEmpty(supplierPhone) &&
                TextUtils.isEmpty(supplierName)) {return;}

        //enter default value for book quantity and book price
        int bookQuantityDefaultValue = 0;
        double bookPriceDefaultValue = 0.00;
        long newRowId ;

        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BooksContract.BookEntry.COLUMN_BOOK_NAME, productName);
        if(!TextUtils.isEmpty(bookPrice)) {
            values.put(BooksContract.BookEntry.COLUMN_BOOK_PRICE, bookPrice);
        } else{
            values.put(BooksContract.BookEntry.COLUMN_BOOK_PRICE, bookPriceDefaultValue);

        }
        if(!TextUtils.isEmpty(bookQuantity)) {
            values.put(BooksContract.BookEntry.COLUMN_BOOK_QUANTITY, Integer.parseInt(bookQuantity));
        }else{
            values.put(BooksContract.BookEntry.COLUMN_BOOK_QUANTITY, bookQuantityDefaultValue);
        }
        values.put(BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER, supplierName);
        values.put(BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, supplierPhone);

        if (currentBookUri == null) {
            // Insert a new row for book in the database, returning the ID of that new row.
            Uri newUri = getContentResolver().insert(BooksContract.BookEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the newUri is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editor_insert_book_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, R.string.editor_insert_book_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update a row for existing book with new details in the database, returning the ID of that row.
            newRowId = getContentResolver().update(currentBookUri, values, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (newRowId == -1) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, R.string.book_saved, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //insert the data to the database
                saveBook();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Open the dialog to confirm deletion of book
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    /*
     * This method is called after the invalidateOptionsMenu method call.
     * This creates the menu based on the given condition and is a
     * system call.(used to include or exclude some menu items)
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BooksContract.BookEntry._ID,
                BooksContract.BookEntry.COLUMN_BOOK_NAME,
                BooksContract.BookEntry.COLUMN_BOOK_PRICE,
                BooksContract.BookEntry.COLUMN_BOOK_QUANTITY,
                BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER,
                BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE,
        };
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this,
                currentBookUri,
                projection,
                null,
                null,
                null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int productNameIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_BOOK_NAME);
            int bookPriceIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_BOOK_PRICE);
            int bookQuantityIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER);
            int supplierPhoneNumberIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(productNameIndex);
            String bookPrice = cursor.getString(bookPriceIndex);
            int bookQuantity = cursor.getInt(bookQuantityIndex);
            String supplierName = cursor.getString(supplierNameIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhoneNumberIndex);

            // Update the view on the screen with the values from the database
            mProductNameEditText.setText(productName);
            mBookPriceEditText.setText(bookPrice);
            mBookQuantityEditText.setText(Integer.toString(bookQuantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mBookPriceEditText.setText("");
        mBookQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        int rowDeleted = getContentResolver().delete(currentBookUri,null,null);

        if (rowDeleted == 0) {
            // If the rowDeleted is 0, then there was an error with deletion.
            Toast.makeText(this, R.string.edit_delete_book_failed, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the deletion was successful and we can display a toast with the row ID.
            Toast.makeText(this, R.string.editor_delete_book_successful, Toast.LENGTH_SHORT).show();
        }
    }


}