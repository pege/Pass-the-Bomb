package websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
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
import org.json.JSONObject;

@ClientEndpoint
public class ClientSide {

	private static CountDownLatch latch;

	private static Session sess;

	public static int tryConnect() {
		try {
			ClientManager client = ClientManager.createClient();
			URI uri;

			// uri = new URI("ws://192.168.0.18:8080/websockets/echo");
			uri = new URI("ws://localhost:8080/websockets/echo");
			// uri = new URI("ws://10.2.134.220:8080/websockets/echo");

			client.connectToServer(ClientSide.class, uri);

			Scanner sc = new Scanner(System.in);
			String mess = "";
			Random r = new Random();
			Long uuid = new Long(r.nextInt(1000));
			while (!mess.equals("exit")) {

				System.out.println("Something to send?");
				mess = sc.nextLine();

				switch (mess) {
				
				case "register":
					JSONObject obj = new JSONObject();
					obj.put("longId", uuid);
					sess.getBasicRemote().sendText(MessageFactory.register(uuid, "pege"));
					break;
				case "create":
					sess.getBasicRemote().sendText(MessageFactory.createGame("Game1", "8888"));
					break;
				case "join":
					sess.getBasicRemote().sendText(MessageFactory.joinGame("Game1", "8888"));
					break;
				case "leave":
					sess.getBasicRemote().sendText(MessageFactory.leaveGame());
					break;
				case "list":
					sess.getBasicRemote().sendText(MessageFactory.getGames());
					break;
				case "start":
					sess.getBasicRemote().sendText(MessageFactory.startGame());
					break;
				case "passBomb":
					//sess.getBasicRemote().sendText(MessageFactory.passBomb(target_uiid, bomb)());
					break;
				case "explode":
					sess.getBasicRemote().sendText(MessageFactory.exploded());
					break;
				case "updateScore":
					//sess.getBasicRemote().sendText(MessageFactory.updateScore(bomb, score));
					break;
				default:
					break;
				}
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
		sess = session;
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		System.out.println("Received: " + message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.printf("Session close because of %s", closeReason);
	}

	@OnMessage
	public void onPong(PongMessage pongMessage, Session session) {
		System.out.println("Pong received");
	}

}
