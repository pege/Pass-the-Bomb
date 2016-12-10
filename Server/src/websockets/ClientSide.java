package websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Scanner;

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

	private static Session sess;

	public static void main(String[] args) throws InterruptedException {
		while (tryConnect() < 0) {
			System.out.println("Failed");
			Thread.sleep(1000);
		}

	}

	public static int tryConnect() {
		try {
			ClientManager client = ClientManager.createClient();
			URI uri;

			// uri = new URI("ws://192.168.0.18:8080/websockets/echo");
			uri = new URI("ws://localhost:8088/websockets/passTheBomb");
			//uri = new URI("ws://54.213.92.251:8088/websockets/passTheBomb");

			client.connectToServer(ClientSide.class, uri);

			Scanner sc = new Scanner(System.in);
			String mess = "";
			Random r = new Random();
			int uuid = r.nextInt(1000);
			System.out.println(
					"register [playername], create [gamename, pw], join [gamename, pw], leave, list, start, passBomb [targetID, bomb], explode, updateScore [bomb, score]");
			while (!mess.equals("exit")) {

				System.out.println("Something to send?");
				mess = sc.nextLine();

				String[] message = mess.split("\\s+");

				switch (message[0]) {

				case "register":
					JSONObject obj = new JSONObject();
					obj.put("longId", uuid);
					sess.getBasicRemote().sendText(MessageFactory.register(Integer.toString(uuid), message[1]));
					break;
				case "create":
					sess.getBasicRemote()
							.sendText(MessageFactory.createGame(message[1], message.length == 3 ? message[2] : ""));
					break;
				case "join":
					sess.getBasicRemote()
							.sendText(MessageFactory.joinGame(message[1], message.length == 3 ? message[2] : ""));
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
					sess.getBasicRemote().sendText(
							MessageFactory.passBomb(message[1], Integer.parseInt(message[2])));
					break;
				case "explode":
					sess.getBasicRemote().sendText(MessageFactory.exploded());
					break;
				case "updateScore":
					sess.getBasicRemote().sendText(
							MessageFactory.updateScore(Integer.parseInt(message[1]), Integer.parseInt(message[2])));
					break;
				default:
					sess.getBasicRemote().sendText(mess);
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
