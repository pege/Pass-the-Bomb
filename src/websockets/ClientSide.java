package websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

@ClientEndpoint
public class ClientSide {

	private static CountDownLatch latch;

	public static void main(String[] args) throws URISyntaxException, DeploymentException, InterruptedException, IOException {
		latch = new CountDownLatch(1);

		ClientManager client = ClientManager.createClient();
		URI uri = new URI("ws://localhost:8080/websockets/echo");
		client.connectToServer(ClientSide.class, uri);
		latch.await();
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		System.out.println("Connected, SessionId: " + session.getId());
		sess=session;
		session.getBasicRemote().sendText("Message");
	}
	
	private int i = 0;
	private Session sess;
	
	@OnMessage
	public String onMessage(String message, Session session) throws IOException {
		System.out.println("Received: " + message);
		sess.getBasicRemote().sendText("Sessss");
		if(i==1) session.close();
		i++;
		return "Heeeeey";
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.printf("Session close because of %s", closeReason);
		latch.countDown();
	}

}
