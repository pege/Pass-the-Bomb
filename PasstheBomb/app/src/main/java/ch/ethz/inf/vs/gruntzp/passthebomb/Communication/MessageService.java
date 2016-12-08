package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

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

        System.out.println("Message: " + message);
        if(this.activity != null)
        {
            int type = 0;
            JSONObject body = null;
            try {
                JSONObject msg = new JSONObject(message);
                type = msg.getJSONObject("header").getInt("type");
                body = msg.getJSONObject("body");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.activity.onMessage(type, body);
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
            } catch (Exception e) {}
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
                    } catch (Exception e) {}
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
                    uri = new URI("ws://" + ip + ":" + port + "/websockets/echo");
                    wsSession = client.connectToServer(MessageService.class, uri);
                }
                catch(Exception ex){}
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
