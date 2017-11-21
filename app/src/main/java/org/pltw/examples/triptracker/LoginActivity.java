package org.pltw.examples.triptracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class LoginActivity extends AppCompatActivity {

    private final String APP_ID = "738C04F7-8DE5-4CC6-FF44-D41D610FB600";
    private final String SECRET_KEY = "AD454BD7-2DB0-BAEB-FF09-E6D741AB8F00";
    private EditText mNameEdit;
    private EditText mEmailEdit;
    private EditText mPasswordEdit;
    private Button mSignUpButton;
    private Button mLoginButton;
    private TextView mSignUpTextView;
    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mNameEdit = (EditText) findViewById(R.id.enter_name);
        mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        SignUpButtonOnClick signUpButtonOnClick = new SignUpButtonOnClick();
        mSignUpButton.setOnClickListener(signUpButtonOnClick);
        mLoginButton = (Button) findViewById(R.id.login_button);
        LoginButtonOnClick loginButtonOnClick = new LoginButtonOnClick();
        mLoginButton.setOnClickListener(loginButtonOnClick);
        mSignUpTextView = (TextView) findViewById(R.id.sign_up_text);
        mEmailEdit = (EditText) findViewById(R.id.enter_email);
        mPasswordEdit = (EditText) findViewById(R.id.enter_password);
        SignUpTextOnClick signUpTextOnClick = new SignUpTextOnClick();
        mSignUpTextView.setOnClickListener(signUpTextOnClick);
        Backendless.initApp(this, APP_ID, SECRET_KEY, "v1");
    }

    @Override
    public void onBackPressed(){
        mSignUpButton.setVisibility(View.GONE);
        mNameEdit.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
        mSignUpTextView.setVisibility(View.VISIBLE);
    }

    public void warnUser(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean validateData(String email, String password){
        if (email.contains("@")){
            if (password.length() >= 6){
                if (!password.contains(email.split("@")[0])){
                    return true;
                }
                else {
                    warnUser("Password cannot match or contain any portion of the email address.");
                }
            }
            else {
                warnUser("Password does not meet complexity requirements.");
            }
        }
        else{
            warnUser("Email address " + email + " doesn't follow standard address format. Please check and retype your email address.");
        }
        return false;
    }

    private class SignUpTextOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            mSignUpButton.setVisibility(View.VISIBLE);
            mNameEdit.setVisibility(View.VISIBLE);
            mLoginButton.setVisibility(View.GONE);
            mSignUpTextView.setVisibility(View.GONE);
        }
    }

    private class LoginButtonOnClick implements  View.OnClickListener{

        @Override
        public void onClick(final View view) {
            String email = mEmailEdit.getText().toString();
            String password = mPasswordEdit.getText().toString();
            email = email.trim();
            password = password.trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                final ProgressDialog pDialog = ProgressDialog.show(LoginActivity.this,
                        "Please Wait!",
                        "Logging in...",
                        true);
                Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Toast.makeText(view.getContext(), response.getProperty("name") + " logged in successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, TripListActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        warnUser(fault.getMessage());
                        pDialog.dismiss();
                    }
                });

            }
            else{
                warnUser(getString(R.string.empty_field_signup_error));
            }
        }
    }

    private class SignUpButtonOnClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            String userEmail = mEmailEdit.getText().toString();
            String password = mPasswordEdit.getText().toString();
            String name = mNameEdit.getText().toString();

            userEmail = userEmail.trim();
            password = password.trim();
            name = name.trim();

            if (!userEmail.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
                if (validateData(userEmail, password)) {
                    BackendlessUser newUser = new BackendlessUser();
                    newUser.setEmail(userEmail);
                    newUser.setPassword(password);
                    newUser.setProperty("name", name);
                    final ProgressDialog pDialog = ProgressDialog.show(LoginActivity.this,
                            "Please Wait!",
                            "Creating new account...",
                            true);
                    Backendless.UserService.register(newUser, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser backendlessUser) {
                            Log.i(TAG, "Successfully registered user: " + backendlessUser.getProperty("name"));
                            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            pDialog.dismiss();
                            warnUser(backendlessFault.getMessage());
                        }
                    });
                }
            }
            else {
                warnUser(getString(R.string.empty_field_signup_error));

            }
        }
    }

}