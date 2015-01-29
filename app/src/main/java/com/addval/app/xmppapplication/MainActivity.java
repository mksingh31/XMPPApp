package com.addval.app.xmppapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLPlainMechanism;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    EditText txtHost, txtPort;
    Button btnConnect, btnDisconnect;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize layout
        updateView();
        // Add onClick Listeners for buttons
        addOnClickListeners();
    }

    /**
     * Method to initialize layout elements
     */
    private void updateView () {
        txtHost = (EditText) findViewById(R.id.txt_host);
        txtPort = (EditText) findViewById(R.id.txt_port);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        progressDialog = new ProgressDialog(new WeakReference<Context>(MainActivity.this).get());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.btn_connect:
                progressDialog.setMessage("Connecting...");
                progressDialog.show();
                new ConnectAsync().execute();
                break;
            case R.id.btn_disconnect:
                if (XMPPApplication.connection.isConnected()) {
                    try {
                        XMPPApplication.connection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void addOnClickListeners () {
        btnConnect.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
    }

    private void initializeConfiguration () throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        XMPPApplication.configuration = new ConnectionConfiguration(txtHost.getText().toString()
                , Integer.parseInt(txtPort.getText().toString()));
        XMPPApplication.configuration.setDebuggerEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            XMPPApplication.configuration.setKeystoreType("AndroidCAStore");
            XMPPApplication.configuration.setKeystorePath(null);
        } else {
            XMPPApplication.configuration.setKeystoreType("BKS");
            String path = System.getProperty("javax.net.ssl.trustStore");
            if (path == null)
                path = System.getProperty("java.home") + File.separator + "etc"
                        + File.separator + "security" + File.separator
                        + "cacerts.bks";
            XMPPApplication.configuration.setKeystorePath(path);
        }
//        configuration.setCustomSSLContext(createContext());
        SASLAuthentication.registerSASLMechanism("PLAIN", SASLPlainMechanism.class);
        SASLAuthentication.supportSASLMechanism("PLAIN");
        XMPPApplication.configuration.setRosterLoadedAtLogin(false);
        XMPPApplication.configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
    }
    private void connectToServer () throws XMPPException, IOException {
        XMPPApplication.connection = new XMPPConnection(XMPPApplication.configuration);
        XMPPApplication.connection.connect();
    }

    private class ConnectAsync extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                initializeConfiguration();
                connectToServer();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
                    if (XMPPApplication.connection.isConnected() && XMPPApplication.connection.isSecureConnection()) {
                        Toast.makeText(getApplicationContext(), "Connected using TLS", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to connect using TLS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
//    private SSLContext createContext() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
//        KeyStore trustStore;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            trustStore = KeyStore.getInstance("AndroidCAStore");
//        } else {
//            trustStore = KeyStore.getInstance("BKS");
//        }
//
//        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        trustManagerFactory.init(trustStore);
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
//        return sslContext;
//    }
}
