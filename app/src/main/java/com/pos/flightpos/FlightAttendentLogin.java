package com.pos.flightpos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.Equipment;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class FlightAttendentLogin extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;
    POSDBHandler handler;
    Spinner equipmentSpinner;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_attendent_login);
        handler = new POSDBHandler(this);
        equipmentSpinner = (Spinner) findViewById(R.id.equipmentNumber);

        String storedValue = SaveSharedPreference.getStringValues(FlightAttendentLogin.this, Constants.SHARED_PREFERENCE_KEY);
        if(storedValue != null && storedValue.length() != 0)
        {
            reDirectToMainPage(SaveSharedPreference.getStringValues(FlightAttendentLogin.this,Constants.SHARED_PREFERENCE_KEY));
        }
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.att_email);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.att_email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form_att);
        mProgressView = findViewById(R.id.login_progress_att);
        loadEquipmentNumbers();
    }

    private void loadEquipmentNumbers(){

        List<Equipment> options=new ArrayList<>();
        ArrayAdapter<Equipment> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
        List<Equipment> equipmentList = handler.getEquipmentList();
        Equipment item = new Equipment();
        options.add(item);
        options.addAll(equipmentList);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
        equipmentSpinner.setAdapter(adapter);

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if(equipmentSpinner.getSelectedItem() == null || equipmentSpinner.getSelectedItem().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Select Equipment type.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if(isLoggingSuccessful(email,password)){
                reDirectToMainPage(email);
            }
            else{
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }
    }

    private void reDirectToMainPage(String userName){
        Intent intent = new Intent(this, AttendendMainActivity.class);
        SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_KEY,userName);
        Equipment equipment = (Equipment)equipmentSpinner.getSelectedItem();
        SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_EQUIPMENT_NO,equipment.getEquipmentNo());
        startActivity(intent);
    }

    private boolean isLoggingSuccessful(String userName, String password){
        return handler.isLoginSuccess(userName,password);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), FlightAttendentLogin.ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(FlightAttendentLogin.ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

