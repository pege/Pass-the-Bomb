package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by niederbm on 12/18/16.
 */

public class ConnectivityService extends Service {

    private static MessageListener mListener;
    private final IBinder mBinder = new LocalBinder();
    private static ConnectivityManager cm;
    private static NetworkInfo ni;
    private Thread t;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ConnectivityService", "CREATED");

    }

    public static void setListener(MessageListener mL) {
        mListener = mL;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("ConnectivityService", "BOUND");

        Reference r = (Reference) intent.getSerializableExtra("activity");
        this.mListener = r.getActivity();

        startMonitoring();

        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ConnectivityService", "DESTROYED");
        t.interrupt();
    }

    public void interruptThread() {
        t.interrupt();
    }

    public void startMonitoring() {
            t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("ConnectivityService", "Monitor thread started");
                cm = (ConnectivityManager) ((AppCompatActivity) mListener).getSystemService(((AppCompatActivity) mListener).CONNECTIVITY_SERVICE);
                boolean keepChecking = true;
                try{
                    while(keepChecking) {
                        ni = cm.getActiveNetworkInfo();
                        if(ni != null && ni.isConnected()) { //We are connected
                            Log.d("ConnectivityService", "Connected");
                        } else { // RIP on
                            Log.d("ConnectivityService", "Not connected");
                            keepChecking = false;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    mListener.onMessage(MessageFactory.CONNECTION_FAILED, null);
                                }
                            });
                        }
                        Thread.sleep(1000);
                    }
                } catch(InterruptedException e) {
                    Log.d("ConnectivityService", "Monitor thread stopped");
                }
            }
        });
        t.start();
    }

    public class LocalBinder extends Binder {
        ConnectivityService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ConnectivityService.this;
        }
    }
}
