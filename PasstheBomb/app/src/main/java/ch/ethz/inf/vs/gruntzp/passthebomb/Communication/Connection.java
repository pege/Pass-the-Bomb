package ch.ethz.inf.vs.mawyss.wstest;

import android.os.Looper;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * Created by Marc on 24.11.2016.
 */

@ClientEndpoint
public class Connection {
    // Assume only one session.
    private Session session;

    @OnOpen
    public void onOpen(Session session) {;
        this.session = session;
        System.out.println("Connected");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        System.out.println("Message: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        System.out.println("Disconnected");
    }


}
