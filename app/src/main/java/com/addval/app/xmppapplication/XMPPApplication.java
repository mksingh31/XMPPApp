package com.addval.app.xmppapplication;

import android.app.Application;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by deepaksingh on 29/01/15.
 */
public class XMPPApplication extends Application {
    public static ConnectionConfiguration configuration;
    public static XMPPConnection connection;
    @Override
    public void onCreate(){
        super.onCreate();
        SmackAndroid.init(getApplicationContext());
        SmackConfiguration.setPacketReplyTimeout(50000);
    }
}
