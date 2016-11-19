package websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
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
	
	public static int tryConnect()
	{
		try {
			ClientManager client = ClientManager.createClient();
			URI uri;
			
			uri = new URI("ws://10.2.134.220:8080/websockets/echo");
			
			client.connectToServer(ClientSide.class, uri);
			
			Scanner sc = new Scanner(System.in);
			String mess="";
			
			while(!mess.equals("exit") && sess.isOpen()){
				System.out.println("Something to send?");
				mess = sc.nextLine();
				if (sess != null && sess.isOpen()) sess.getBasicRemote().sendText(mess);
				else System.out.println("Schnauze!");
			}
			
			
			System.out.println("Closing session");
			sess.close();
			
			} catch (URISyntaxException | IOException | DeploymentException e) {
				return -1;
			}
		return 0;
	}

	public static void main(String[] args) throws InterruptedException {
		while (tryConnect() < 0) {
			System.out.println("Failed");
			Thread.sleep(500);
		}
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		System.out.println("Connected, SessionId: " + session.getId());
		sess=session;
		session.getBasicRemote().sendText("Message");
		
		
	}
	
	private static Session sess;
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		System.out.println("Received: " + message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.printf("Session close because of %s", closeReason);
	}

}
