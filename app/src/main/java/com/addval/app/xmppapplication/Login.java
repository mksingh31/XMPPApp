package com.addval.app.xmppapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.lang.ref.WeakReference;


public class Login extends ActionBarActivity implements View.OnClickListener{
    EditText txtUsername, txtPassword;
    Button btnLogin, btnLogout;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initialize layout
        updateView();
        // Add onClick Listeners for buttons
        addOnClickListeners();
    }

    /**
     * Method to initialize layout elements
     */
    private void updateView () {
        txtUsername = (EditText) findViewById(R.id.txt_username);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogout = (Button) findViewById(R.id.btn_logout);
        progressDialog = new ProgressDialog(new WeakReference<Context>(Login.this).get());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                progressDialog.setMessage("Logging in...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new LoginToXMPP().execute();
                break;
            case R.id.btn_logout:
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (XMPPApplication.connection.isConnected()) {
                            try {
                                XMPPApplication.connection.disconnect();
                                if (!XMPPApplication.connection.isConnected()) {
                                    Log.e("Connection Status", " : Disconnected");
                                    finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                t.start();
                break;
            default:
                break;
        }
    }

    private void addOnClickListeners () {
        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
    }
    private class LoginToXMPP extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                XMPPApplication.connection.login(txtUsername.getText().toString(), txtPassword.getText().toString());
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if (XMPPApplication.connection.isAuthenticated()) {
                        Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to login. Try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
