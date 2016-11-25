package ch.ethz.inf.vs.mawyss.wstest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;


public class MainActivity extends AppCompatActivity implements MessageListener{
    MessageService mService;
    boolean mBound = false;

    public void onClick(View view)
    {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }

    public void onClick2(View view)
    {
        mService.sendMessage("Hello");
    }

    @Override
    public void onMessage(String message) {
        // Do action depending on message...
        System.out.println("Received the message :-)");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MessageService.class);
        intent.putExtra("ip", "54.213.92.251");
        intent.putExtra("port", "8080");

        Reference r = new Reference();
        r.setActivity(this);
        intent.putExtra("activity", r);
        //TODO
        intent.putExtra("uuid", "");

        // Start service (this is done only by main activity)
        startService(intent);

        // Bind to service (every activity should do this at the beginning
        boolean b = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    // Defines callbacks for service binding
    private ServiceConnection mConnection = new ServiceConnection() {

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
}

class Reference implements Serializable
{
    public static MessageListener activity;

    public void setActivity(MessageListener activity)
    {
        this.activity = activity;
    }

    public MessageListener getActivity(){
        return activity;
    }
}
