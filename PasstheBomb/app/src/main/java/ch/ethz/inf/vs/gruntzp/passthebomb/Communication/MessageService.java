package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.json.*;

/**
 * Created by Marc on 24.11.2016.
 */

@ClientEndpoint
public class MessageService extends Service {
    static Session wsSession = null;
    static MessageListener activity = null;
    static String ip = "";
    static String port = "";
    static String uuid = "";
    static Boolean firstCall = true;

    //--- Websocket ----------------------------------------------------------------
    @OnOpen
    public void onOpen(Session session) {;
        this.wsSession = session;
        System.out.println("Connected to server.");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("MessageFactory: " + message);
        if(this.activity != null)
        {
            // Parse message to JSON Object
            int type = 0;
            JSONObject body = null;

            try {
                JSONTokener tokener = new JSONTokener(message);
                JSONObject msg = new JSONObject(tokener);
                type = msg.getJSONObject("header").getInt("type");
                body = msg.getJSONObject("body");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Call onMessage() of the current activity, in MainThread
            final int copy_type = type;
            final JSONObject copy_body = body;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    activity.onMessage(copy_type, copy_body);
                }
            });
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        System.out.println("Disconnected");
    }

    public void reconnect(String ip, String port)
    {
        if(wsSession != null)
        {
            try {
                wsSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        initializeConnection(ip, port);
    }


    public void sendMessage(final String message)
    {
        if(wsSession != null)
        {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    ClientManager client = ClientManager.createClient();
                    try {
                        wsSession.getBasicRemote().sendText(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    //--- Service ----------------------------------------------------------------
    private void initializeConnection(final String ip, final String port)
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ClientManager client = ClientManager.createClient();
                try{
                    URI uri = null;
                    uri = new URI("ws://" + ip + ":" + port + "/websockets/passTheBomb");
                    wsSession = client.connectToServer(MessageService.class, uri);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                    onMessage(MessageFactory.Connection_Failed(), null);
                }
            }
        });
        t.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(firstCall)
        {
            this.ip = intent.getStringExtra("ip");
            this.port = intent.getStringExtra("port");
            this.uuid = intent.getStringExtra("uuid");

            System.out.println("Service created.");
            initializeConnection(ip, port);
            firstCall = false;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service DESTROYED");
    }

    //--- Binder for Service -------------------------------------------------------------------------------------------------
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("ON BIND");

        Reference r = (Reference) intent.getSerializableExtra("activity");
        this.activity = r.getActivity();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MessageService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MessageService.this;
        }
    }

}
