package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Marc on 25.11.2016.
 */

public class ServiceConnector {
    static private MessageService mService;
    static private boolean mBound = false;
    //private static final String ip = "54.213.92.251";
    private static final String ip = "10.0.2.2";
    private static final String port = "8080";

    private static ServiceConnector instance;

    public static ServiceConnector getInstance()
    {
        if(instance == null)
        {
            instance = new ServiceConnector();
        }
        return instance;
    }

    // Defines callbacks for service binding
    public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MessageService.LocalBinder binder = (MessageService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            System.out.println("BOUND TO SERVICE");

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            System.out.println("DISCONNECTED FROM SERVICE");
            startService((AppCompatActivity) mService.activity);
        }
    };

    /*
    This method starts the service and should only be called once, as soon as the App is started.
     */
    public void startService(AppCompatActivity activity)
    {
        if(!mBound)
        {
            Intent intent = new Intent(activity, MessageService.class);

            intent.putExtra("ip", ip);

            intent.putExtra("port", port);


            Reference r = new Reference();
            r.setActivity((MessageListener) activity);
            intent.putExtra("activity", r);
            //TODO
            intent.putExtra("uuid", "");

            // Start service (this is done only by main activity)
            activity.startService(intent);
        } else {
            System.out.println("Trying to reconnect MessageService");
            mService.reconnect(ip, port); //Try to reconnect with current activity as listener
        }
    }

    /*
    This method binds an activity to the service and should only be called every time an
    activity is started.
     */
    public void bind(AppCompatActivity activity)
    {
        Intent intent = new Intent(activity, MessageService.class);

        Reference r = new Reference();
        r.setActivity((MessageListener) activity);
        intent.putExtra("activity", r);

        // Bind to service (every activity should do this at the beginning
        boolean b = activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if(b){
            MessageService.activity = (MessageListener) activity;
        }
    }

    public void unbind(AppCompatActivity activity)
    {
        if (mBound) {
            activity.unbindService(mConnection);
            mBound = false;
            System.out.println("Did unbind activity from service.");
        }
    }

    public void sendMessage(String message)
    {
        mService.sendMessage(message);
    }

    public void tryReconnecting() {
        System.out.println("Trying to restart");
        mService.reconnect(ip, port); //Try to reconnect with current activity as listener
    }
}
