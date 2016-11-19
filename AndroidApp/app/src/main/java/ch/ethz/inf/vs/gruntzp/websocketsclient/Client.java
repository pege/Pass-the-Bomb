package ch.ethz.inf.vs.gruntzp.websocketsclient;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashSet;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;


@ClientEndpoint
public class Client {

    //Unhandled exception in endpoint ch.ethz.inf.vs.gruntzp.websocketsclient.Client.
    //android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.


    // Using only one client class, different activities can register for message event
    private static HashSet<MessageListener> listeners = new HashSet<MessageListener>();

    public static void Subscribe(MessageListener listener){
        listeners.add(listener);
    }

    public static Boolean Unsubsribe(MessageListener listener) {
        return listeners.remove(listener);
    }

    private static void Broadcast(String message) {
        for (MessageListener listener: listeners) {
            listener.onMessage(message);
        }
    }

    //Only one instance of client per App
    //Don't call manually
    public Client() {
        instance = this;
    }
    private static Client instance;
    public static Client getInstance(){
        if (instance == null)
            instance = new Client();
        return instance;
    }

    public static void Dispose(){
        instance = null;
    }

    //////////////////////////////////////

    public static Connection connection;
    public static void openConnection(String ip)
    {
        connection = new Connection(ip);
    }

    //////////////////////////////////////


    public static String messageToSend = "Null";
    private static Session session;



    @OnOpen
    public void onOpen(Session session) {;
        Main.textView.setText("A: Connection established");
        System.out.println("A: Connection established");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        this.session = session;
        Broadcast(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        //session.getBasicRemote().sendText("CLOSING");
        Main.textView.setText("Session Closed: " + closeReason.toString());
        System.out.println("OnClose");
        this.session = null;
        Main.latch.countDown();
    }

    public static void sendMessage(String message){
        try {
            if (session == null)return;
            if (message.equals("exit")){
                session.close();
            }else {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Message sent: " + message);

    }
}
