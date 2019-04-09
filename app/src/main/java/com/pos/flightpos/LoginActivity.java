package com.pos.flightpos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.pt.minilcd.MiniLcd;
import android.pt.msr.Msr;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

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
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "admin:admin", "readUser:readUser"
    };

    private static final List<String> BASE_STATIONS = Arrays.asList( new String[] {
            "YYZ", "YUL", "YOW", "IAD"
    });

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private AutoCompleteTextView  baseStation;
    private View mProgressView;
    private View mLoginFormView;
    Button mEmailSignInButton;
    long mExitTime = 0;
    String parent = null;
    private Msr msr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        parent = getIntent().getExtras().getString("parent");
        //showBootImage();
        try {
            if(isAppExpired()){
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String isFlightOpen = SaveSharedPreference.getStringValues(this,"isOpenFlight");
        if(isFlightOpen != null && isFlightOpen.equals("yes")){
            Intent intent = new Intent(LoginActivity.this, SellItemsActivity.class);
            startActivity(intent);
            return;
        }
        String flightUserName = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FA_NAME);
        if(flightUserName != null && flightUserName.length() != 0){
            Intent intent = new Intent(this, AttendendMainActivity.class);
            startActivity(intent);
            return;
        }
        String canAttLogin = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_CAN_ATT_LOGIN);
        if(canAttLogin != null && canAttLogin.equals("yes")){
            Intent intent = new Intent(this, FlightAttendentLogin.class);
            startActivity(intent);
            return;
        }

        String userName = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_ADMIN_USER);
        String isAdminConfiguredFlight = SaveSharedPreference.getStringValues(this,
                Constants.SHARED_ADMIN_CONFIGURE_FLIGHT);
        if(userName != null && userName.length() != 0)
        {
            if(isAdminConfiguredFlight != null && isAdminConfiguredFlight.equals("yes")){
                Intent intent = new Intent(this, VerifyFlightByAdminActivity.class);
                startActivity(intent);
            }
            else {
                reDirectToMainPage(SaveSharedPreference.getStringValues(this, "userName"));
            }
            return;
        }
        String isFlightClosed = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_CLOSED_FLIGHT);
        if(isFlightClosed != null && isFlightClosed.equals("yes") && !"SelectModeActivity".equals(parent)){
            Intent intent = new Intent(this, SelectModeActivity.class);
            startActivity(intent);
            return;
        }
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
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
        baseStation = findViewById(R.id.baseStation);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, BASE_STATIONS);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        baseStation.setAdapter(adapter);

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        msr = new Msr();
        //readMSR();
    }

    private void showBootImage(){
        MiniLcd minilcd = new MiniLcd ();
        minilcd.open();
        minilcd.downloadBootPicture(BitmapFactory.decodeResource(getResources(), R.drawable.pos_img),1);
    }

    private void closeMSR(){
         if(msr != null)
            msr.close();
    }

    private void readMSR(){
        msr.open();
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                if(msg.what == 1){
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
                        //break;
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
                if(BASE_STATIONS.contains(credentials[1])) {
                    if (isLoggingSuccessful(credentials[2].toLowerCase(), credentials[3].toLowerCase())) {
                        setInitialData(credentials[2], credentials[1]);
                        closeMSR();
                        return;
                    } else {
                        Toast.makeText(this, "Not a valid card.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    baseStation.setError("Base station not found");
                    baseStation.requestFocus();
                }
            }
            else {
                Toast.makeText(this, "Not a valid card.", Toast.LENGTH_SHORT).show();
            }
            readMSR();
        }
    }

    private boolean isAppExpired() throws ParseException {
        String expiredDate = "01/01/2020";
        Date date=new SimpleDateFormat("dd/MM/yyyy").parse(expiredDate);
        Date today = new Date();
        if (today.compareTo(date) > 0) { //Date1 is after Date2 - system is expired.
            mEmailSignInButton.setEnabled(false);
            TextView warningTextView = findViewById(R.id.warningTextView);
            warningTextView.setText("This application is expired. Please contact administrator.");
            return true;
        }
        return false;
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
            //showProgress(true);
            if(BASE_STATIONS.contains(baseStationVal)) {
                if (isLoggingSuccessful(email, password)) {
                    setInitialData(email, baseStationVal);
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
            else{
                baseStation.setError("Base station not found");
                baseStation.requestFocus();
            }
        }
    }

    private void setInitialData(String email,String baseStation){
        if(msr != null){
            msr.close();
        }
        SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_ADMIN_USER_NAME,email);
        SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_BASE_STATION,baseStation);
        SaveSharedPreference.setStringValues(this,
                Constants.SHARED_PREFERENCE_FLIGHT_MODE,"admin");
        SaveSharedPreference.removeValue(this,Constants.SHARED_PREFERENCE_FLIGHT_TYPE);
        SaveSharedPreference.removeValue(this,"adminAdditionalSealList");
        SaveSharedPreference.removeValue(this,Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS);
        SaveSharedPreference.removeValue(this,Constants.SHARED_PREFERENCE_OUT_BOUND_SEAL_LIST);
        POSDBHandler handler = new POSDBHandler(this);
        //posdbHandler.clearDailySalesTable();
        SaveSharedPreference.removeValue(this,"orderNumber");
        reDirectToMainPage(email);
    }

    private void reDirectToMainPage(String userName){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userName",userName);
        String isFlightClosed = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_CLOSED_FLIGHT);
        if(isFlightClosed != null && isFlightClosed.equals("yes")){
            intent.putExtra("parent","SelectModeActivity");
        }
        else {
            intent.putExtra("parent", parent);
        }
        startActivity(intent);
    }

    private boolean isLoggingSuccessful(String userName, String password){
        for (String credential : DUMMY_CREDENTIALS) {
            String[] pieces = credential.split(":");
            if (pieces[0].equals(userName)) {
                // Account exists, return true if the password matches.
                return pieces[1].equals(password);
            }
        }
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

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
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
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
            if(msr != null){
                msr.close();
            }
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }
}

