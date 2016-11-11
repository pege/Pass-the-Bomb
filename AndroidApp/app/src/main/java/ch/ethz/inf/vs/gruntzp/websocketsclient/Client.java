package ch.ethz.inf.vs.gruntzp.websocketsclient;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.io.IOException;

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

    public static String messageToSend = "Null";
    public static Session session;


    @OnOpen
    public void onOpen(Session session) {;
        Main.textView.setText("A: Connection established");
        System.out.println("A: Connection established");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        this.session = session;
        Main.textView.setText(message);
        System.out.println("Message received: "+message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        //session.getBasicRemote().sendText("CLOSING");
        Main.textView.setText("Session Closed: " + closeReason.toString());
        System.out.println("OnClose");
        this.session=null;
        Main.latch.countDown();
    }

    public static void sendMessage(){
        try {
            if (session==null)return;
            if (messageToSend.equals("exit")){
                session.close();
            }else {
                session.getBasicRemote().sendText(messageToSend);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Message sent: " + messageToSend);

    }
}
