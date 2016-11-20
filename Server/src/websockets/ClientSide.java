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
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.json.simple.*;

@ClientEndpoint
public class ClientSide {

	private static CountDownLatch latch;
	
	public static int tryConnect()
	{
		try {
			ClientManager client = ClientManager.createClient();
			URI uri;
			
			//uri = new URI("ws://192.168.0.18:8080/websockets/echo");
			uri = new URI("ws://localhost:8080/websockets/echo");
			
			
			client.connectToServer(ClientSide.class, uri);
			
			Scanner sc = new Scanner(System.in);
			String mess="";
			
			while(!mess.equals("exit")){
				
				System.out.println("Something to send?");
				mess = sc.nextLine();
				
				//https://www.mkyong.com/java/json-simple-example-read-and-write-json/
				//JSONObject sendObj = new JSONObject();
				JSONArray sendObj = new JSONArray();
				
				if(mess.equals("create")){
					sendObj.add("create");
				}else if (mess.equals("join")){
					sendObj.add("join");
				}else if(mess.equals("pong")){
					sess.getBasicRemote().sendPong(null);
				}else{
					//sendObj.put(,);
				}
				
				if (sess != null)
					//sess.getBasicRemote().sendText(sendObj.toJSONString());
					sess.getBasicRemote().sendText(mess);
					
				else System.out.println("Schnauze!");
			}
			
			
			System.out.println("Closing session");
			sc.close();
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
	
	private int i = 0;
	private static Session sess;
	
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		//session.isOpen()
		//session.getMaxIdleTimeout()
		System.out.println("Received: " + message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.printf("Session close because of %s", closeReason);
		latch.countDown();
	}
	
	@OnMessage
    public void onPong(PongMessage pongMessage, Session session) {
    	System.out.println("Pong received");
    }
	

}
