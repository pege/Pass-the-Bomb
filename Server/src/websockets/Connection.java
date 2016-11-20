package websockets;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/echo")
public class Connection {

	private boolean connected; // wrong
	private final long timeout = 5000; // timeout in ms
	private final long timeBetweenPings = 300; // in ms

	private long timeOfLastping;
	private long timeOfLastpong;
	
	static Set<Game> games = Collections.newSetFromMap(new ConcurrentHashMap<Game, Boolean>());
	//static Set<Game> games = new HashSet<>();
	static Map<Session, Player> map = new HashMap<>();

	
	static { // Thread to send pings and check the connection
		new Thread() {
			public void run() {
				//checkConnection2(session);
				checkConnection();
			};
		}.start();
		System.out.println("Thread started");
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {

		System.out.println("Client Connected");

		map.put(session, null);

		connected = true;

		// TODO already in a game? - reconnect
	}

	@OnMessage
	public void onMessage(String message, Session session) {

		String playername = "playername";
		long uuid = 1234;
		String gamename = "Gamename";
		String password = "password";
		System.out.println(Thread.currentThread());
		// Object jsonObject = (JSONObject)parser.parse(message);
		// JSONArray msg = (JSONArray) ((ArrayList) jsonObject).get("messages");

		String messArr[] = message.split(" ", 2);

		String header, body;
		if (messArr.length == 1) {
			header = messArr[0];
			body = "";
		} else {
			header = messArr[0];
			body = messArr[1];
		}

		System.out.println("header: " + header);
		System.out.println("body: " + body);

		switch (header) {
		case "register":
			register(playername, uuid, session); // create player and add to the
													// map
		case "create":
			createGame(session, gamename, password);// open a game and add the
													// player as th creator
			break;
		case "refresh": // or join
			getLobbyList(session); // returns all current games
			break;
		case "joinGame":
			joinGame(session, gamename, password); // adds the player to a game
			break;
		case "leave":
			leaveGame(session); // removes a player from a game
			break;
		case "delete":
			// TODO --deleteGame is unnecessary--(Check in leave Game, if only
			// one player in game)
			// TODO Delete Game (Creator) //TODO can u do it intentionally?
			deleteGame(session); // deletes the game,
			break;
		default:
		}
		System.out.println("Message from " + session.getId() + ": " + message);
		System.out.println("games:" + Integer.toString(games.size()));
		System.out.println(games.size());
		//for (Game g : games) {
		//	System.out.println(g.getGamename() + ": " + g.getPlayers());
		//}
	}

	@OnClose
	public void onClose(Session session) {
		leaveGame(session);
		System.out.println("session closed");
	}

	/*
	 * @OnError public void onError(Session session, Throwable t) {
	 * System.out.println("Höu, error?"); System.out.println(t.getMessage()); }
	 */

	public void checkConnection2(Session session) {
		while (connected) {
			timeOfLastping = System.currentTimeMillis();
			if (session.isOpen()) { // Session is open
				try {
					session.getBasicRemote().sendPing(null);
					// TODO dont know if Thread.sleep is best function
					Thread.sleep(timeBetweenPings);

					// System.out.println("--Ping sended");

					if (Math.abs(timeOfLastping - timeOfLastpong) > timeout) { // Connection
																				// timeout
						System.out.println("Connection timeout");
						connected = false;
						session.close();
						// Leave Game, Delete Game
					}
				} catch (IllegalArgumentException | IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {// Session closed
				connected = false;
			}
		}

	}

	public static void checkConnection(){
		while(true){
			for(Game g : games){
				for(Player p : g.getPlayers()){
					if(p.getSession().isOpen() && p.isConnected()){
						ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
					    buffer.asLongBuffer().put(System.currentTimeMillis());		
						try {
							Thread.sleep(200); //TODO should be after the send
							p.getSession().getBasicRemote().sendPing(buffer);
							//System.out.println("hier");
						} catch (IllegalArgumentException | IOException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			
			
			
		}
		
	}
	
	// OnPong
	@OnMessage
	public void onPong(PongMessage pongMessage, Session session) {
		System.out.println("--Pong received");
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer = pongMessage.getApplicationData();
	    long diff = Math.abs(buffer.asLongBuffer().get() - System.currentTimeMillis());
	    System.out.println(Long.toString(diff));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	private void register(String username, long uuid, Session session) {
		Player p = new Player(username, uuid, session);
		p.setConnection(true);
		p.setGameStatus(false);
		map.put(session, p);
	}

	private void createGame(Session session, String gamename, String password) {

		// gamename unique: adds a number to the current gamename
		int i = 0;
		while (!uniqueGamename(gamename)) {
			gamename = gamename + Integer.toString(i);
			try {
				session.getBasicRemote()
						.sendText("A game with this gamename already exists, it was changed to: " + gamename);
			} catch (IOException e) {
				e.printStackTrace();
			} // TODO too manys messsages will be sent
			i++;
		}

		Player creator = map.get(session);
		Game game = new Game(creator, gamename, password);
		games.add(game);
	}

	private boolean uniqueGamename(String name) {
		for (Game g : games) {
			if (g.getGamename().equals(name)) {
				return false;
			}
		}
		return true;
	}

	private void getLobbyList(Session session) {
		try {
			for (Game g : games) {
				session.getBasicRemote().sendText(g.getGamename());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void joinGame(Session session, String gamename, String password) {

		boolean joined = false;
		for (Game game : games) {
			if (game.getGamename().equals(gamename)) {
				if (game.checkPassword(password)) {
					Player p = map.get(session);
					p.setGameStatus(true);
					game.addPlayer(p);
					joined = true;
					break;
				} else {
					System.out.println("Wrong password");
				}
			}
		}

		if (!joined)
			System.out.println("No game with that name found - already started");
	}

	private void leaveGame(Session session) {

		boolean left = false;
		Player p = map.get(session);

		if (!p.getGameStatus()) {
			System.out.println("Player is in no game");
		}
		for (Game game : games) {
			if (game.playerInGame(p)) {
				game.removePlayer(p);
				p.setGameStatus(false);
				left = true;
				break;
			}
		}
		if (!left)
			System.out.println("Error - no game found with this player");
	}

	public void deleteGame(Session session) {
		Player p = map.get(session);

		for (Game game : games) {
			if (game.playerInGame(p)) {
				if (game.numberOfPlayers() == 1) {// Only the creator in the
													// game
					games.remove(game);
				} else { // more than one player in the game, a new creator i
							// set
					game.setNewCreator();
					game.removePlayer(p);
				}
				p.setGameStatus(false);
			}
		}

	}
}
