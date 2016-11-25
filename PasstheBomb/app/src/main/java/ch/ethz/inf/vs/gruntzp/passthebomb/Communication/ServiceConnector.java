package ch.ethz.inf.vs.mawyss.wstest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Marc on 25.11.2016.
 */

public class ServiceConnector {
    public MessageService mService;
    public boolean mBound = false;

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
        }
    };

    public void startService(AppCompatActivity activity)
    {
        Intent intent = new Intent(activity, MessageService.class);
        intent.putExtra("ip", "54.213.92.251");
        intent.putExtra("port", "8080");

        Reference r = new Reference();
        r.setActivity((MessageListener) activity);
        intent.putExtra("activity", r);
        //TODO
        intent.putExtra("uuid", "");

        // Start service (this is done only by main activity)
        activity.startService(intent);
    }

    public void bind(AppCompatActivity activity)
    {
        Intent intent = new Intent(activity, MessageService.class);

        Reference r = new Reference();
        r.setActivity((MessageListener) activity);
        intent.putExtra("activity", r);

        // Bind to service (every activity should do this at the beginning
        boolean b = activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

}
