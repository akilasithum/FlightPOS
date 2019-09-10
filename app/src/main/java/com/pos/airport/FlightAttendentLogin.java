package com.pos.airport;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.pt.msr.Msr;

import com.pos.airport.objects.Constants;
import com.pos.airport.utils.POSDBHandler;
import com.pos.airport.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class FlightAttendentLogin extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;
    POSDBHandler handler;
    long mExitTime = 0;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private AutoCompleteTextView baseStation;
    private Msr msr = null;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private static final List<String> BASE_STATIONS = Arrays.asList( new String[] {
            "YYZ", "YUL", "YOW", "IAD"
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_attendent_login);
        handler = new POSDBHandler(this);

        String storedValue = SaveSharedPreference.getStringValues(FlightAttendentLogin.this, Constants.SHARED_PREFERENCE_FA_NAME);
        if(storedValue != null && storedValue.length() != 0)
        {
            reDirectToMainPage(SaveSharedPreference.getStringValues(FlightAttendentLogin.this,Constants.SHARED_PREFERENCE_FA_NAME));
        }
        // Set up the login form.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mEmailView =  findViewById(R.id.email);

        mPasswordView =  findViewById(R.id.password);
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

        baseStation = findViewById(R.id.baseStation);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, BASE_STATIONS);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        baseStation.setAdapter(adapter);

        ImageButton mEmailSignInButton =  findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                attemptLogin();
            }
        });
       /* ImageButton smartCardBtn = findViewById(R.id.magneticCardBtn);
        smartCardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readMSR();
            }
        });*/
    }

    private void readMSR(){
        final Msr msr = new Msr();
        msr.open();
        final ProgressDialog dia = new ProgressDialog(this);
        dia.setTitle("MSR");
        dia.setMessage("please swipe Smart card...");
        dia.show();
        dia.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                msr.close();
            }
        });
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                if(msg.what == 1){
                    dia.cancel();
                    for(int i = 1; i < 4; i++)
                    {
                        if(msr.getTrackError(i) == 0)
                        {
                            //Log.i("123", "i:"+i);
                            byte[] out_data = new byte[msr.getTrackDataLength(i)];
                            msr.getTrackData(i, out_data);
                            readMsrData(i,out_data);
                            return;
                        }
                    }
                }
                super.handleMessage(msg);
            }
        };

        new Thread(){
            public void run() {
                int ret = -1;
                while(true)
                {
                    ret = msr.poll(1000);
                    if(ret == 0)
                    {
                        Message msg = new Message();
                        msg.what    = 1;
                        handler.sendMessage(msg);
                        break;
                    }
                }
            }
        }.start();
    }

    private void readMsrData(int i, byte[] out_data){
        if(i == 1)
        {
            String track1Str = new String(out_data);
            String[] credentials = track1Str.split(" ");
            if(credentials.length > 3){
                if(isLoggingSuccessful(credentials[1].toLowerCase(),credentials[2].toLowerCase())){
                    reDirectToMainPage(credentials[1].toLowerCase());
                }
                else{
                    Toast.makeText(this, "Not a valid card.", Toast.LENGTH_SHORT).show();
                    readMSR();
                }
            }
            else {
                Toast.makeText(this, "Not a valid card.", Toast.LENGTH_SHORT).show();
                readMSR();
            }
        }
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
        String baseStationVal = baseStation.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(baseStationVal)) {
            baseStation.setError(getString(R.string.error_field_required));
            focusView = baseStation;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if(BASE_STATIONS.contains(baseStationVal)) {
                if (isLoggingSuccessful(email, password)) {
                    SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_BASE_STATION,baseStationVal);
                    reDirectToMainPage(email);
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
            else{
                baseStation.setError("Base station not found");
                baseStation.requestFocus();
            }

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
        Intent intent = new Intent(this, GateUserMainActivity.class);
        SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FA_NAME,userName);
        SaveSharedPreference.setStringValues(this,
                Constants.SHARED_PREFERENCE_FLIGHT_MODE,"faUser");
        startActivity(intent);
    }

    private boolean isLoggingSuccessful(String userName, String password){
        boolean isLoginSuccess = handler.isLoginSuccess(userName,password,"9");
        return isLoginSuccess;
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

    @Override
    public void onBackPressed()
    {
        if((System.currentTimeMillis() - mExitTime) < 2000)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }
}

