package websockets;

import java.io.IOException;
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

	private static final long timeout = 5000; // timeout in ms
	private static final long timeBetweenPings = 200; // time to wait between
														// sending a ping

	// games as a concurrent Set
	static Set<Game> games = Collections.newSetFromMap(new ConcurrentHashMap<Game, Boolean>());
	// All sessions
	static Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());

	// static Set<Game> games = new HashSet<>();

	// Map from the session to the player, independent if they are in a game or
	// not
	static Map<Session, Player> map = new HashMap<>();

	static { // Thread to send pings and check the connection
		new Thread() {
			public void run() {
				checkConnection();
			};
		}.start();
		System.out.println("Thread started");
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {

		System.out.println("Client Connected");
		sendMess(session, "Connected but not registered");
		map.put(session, null);
		// sessions.add(session);
		// TODO already in a game? - reconnect

		// register("handy", 1111, session);
		// createGame(session, "game1", "");

		sendMess(session, "Welcome to <<PASS THE BOMB>>");
		sendMess(session, "Possible orders: register [name], create [gamename], refresh, join [gamename], leave or status");

	}

	@OnMessage
	public void onMessage(String message, Session session) {

		
		
		long uuid = 1234;
		String password = "password";
		// System.out.println(Thread.currentThread());
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

		System.out.println("Message received: " + header + " " + body);
		
		if(!header.equals("register") && map.get(session) == null){
			sendMess(session, "Please register yourself befor start gaming");
			return;
		}
		
		switch (header) {
		case "register":
			if(body.equals("")) register("Default Name", uuid, session); // create player and add to the
			else  register(body, uuid, session);
			break; // map
		case "create":
			if(body.equals(""))	createGame(session, "Default Gamename", password);// open a game and add the player as the creator
			else createGame(session, body, password);
			break;
		case "refresh": // or join
			getLobbyList(session); // returns all current games
			break;
		case "join":
			if(body.equals("")) sendMess(session, "Which game you wanna join?");
			else joinGame(session, body, password); // adds the player to a game
			break;
		case "leave"://TODO can u do it intentionally?, next creator random?
			leaveGame(session); // removes a player from a game
			break;
		case "status":
			returnStatus(session);
			break;
		default:
		}
		// System.out.println("Message from " + session.getId() + ": " +
		// message);
		System.out.println("games:" + Integer.toString(games.size()));
		// System.out.println(games.size());
		// for (Game g : games) {
		// System.out.println(g.getGamename() + ": " + g.getPlayers());
		// }
	}

	@OnClose
	public void onClose(Session session) {
		leaveGame(session);
		sessions.remove(session);
		
		if (map.get(session) != null) System.out.println(map.get(session).getName() + " just left us :(");
		System.out.println("session closed");
	}

	/*
	 * @OnError public void onError(Session session, Throwable t) {
	 * System.out.println("Höu, error?"); System.out.println(t.getMessage()); }
	 */

	public static void checkConnection() {
		while (true) {
			for (Session s : sessions) {
				Player p = map.get(s);
				if (p.getSession().isOpen() && p.isConnected()) {// TODO this if
																	// should be
																	// unnecessary
					// check last received pong
					if (Math.abs(p.getLastPong() - System.currentTimeMillis()) > timeout) {
						System.out.println("================================================");
						System.out.println("============Connection timeout==================");
						System.out.println("================================================");
						try {
							// TODO
							p.getSession().close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// send a Ping with the current Time
					ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
					buffer.asLongBuffer().put(System.currentTimeMillis());
					try {
						// TODO getAsyncRemote or getBasicRemote?
						if(p.getSession().isOpen())
							p.getSession().getBasicRemote().sendPing(buffer);
						Thread.sleep(timeBetweenPings);
					} catch (IllegalArgumentException | IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	// OnPong
	@OnMessage
	public void onPong(PongMessage pongMessage, Session session) {
		// System.out.println("--Pong received");
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer = pongMessage.getApplicationData();

		Player p = map.get(session);
		p.setLastPong(buffer.asLongBuffer().get());

		// long diff = Math.abs(buffer.asLongBuffer().get() -
		// System.currentTimeMillis());
		// System.out.println(Long.toString(diff));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	private void register(String username, long uuid, Session session) {
		// TODO player (uuid) already exist? - reconnect

		if(map.get(session) != null){//already registered
			System.out.println("Second register try received");
			sendMess(session, "This connection is already registered with name: " + map.get(session).getName());
			return;		
		}
		Player p = new Player(username, uuid, session);
		p.setConnection(true);
		p.joinGame(null); // unnecessary
		p.setLastPong(System.currentTimeMillis());
		map.put(session, p);
		sessions.add(session); // start pinging
		sendMess(session, "Successful Registered");
		System.out.println(username + " has been registered");
	}

	private void createGame(Session session, String gamename, String password) {
		
		Player creator = map.get(session);
		if(creator.getJoinedGame()!=null){ //already in a Game
			sendMess(session, "Stupid? You're already in a game");
			return;
		}
		// gamename unique: adds a number to the current gamename
		int i = 0;
		while (!uniqueGamename(gamename)) {
			gamename = gamename + Integer.toString(i);
			sendMess(session, "A game with this gamename already exists, it was changed to: " + gamename);
			i++;
		}

		
		// creator.setLastPong(System.currentTimeMillis());
		Game game = new Game(creator, gamename, password);
		creator.joinGame(game);
		games.add(game);
		sendMess(session, "A game with gamename " + gamename + " was created");
		System.out.println("A game with gamename " + gamename + " was created");
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
		if (games.size() == 0) {
			sendMess(session, "No games in Lobby, come later or create one");
		}

		for (Game g : games) {
			sendMess(session, "Game: " + g.getGamename() + ", numberOfPlayers: " + g.numberOfPlayers()
					+ ", playernames: " + g.getPlayersName());
		}

	}

	private void joinGame(Session session, String gamename, String password) {
		
		Player player = map.get(session);
		if(player.getJoinedGame()!=null){ //already in a Game
			sendMess(session, "Stupid? You're already in a game");
			return;
		}
		
		
		boolean joined = false;
		for (Game game : games) {
			if (game.getGamename().equals(gamename)) {
				if (game.checkPassword(password)) {
					player.joinGame(game);
					game.addPlayer(player);
					joined = true;
					sendMess(session, "You joined successful the game: " + game.getGamename());
					System.out.println(player.getName() + " joined the game " + game.getGamename());
					break;
				} else {
					sendMess(session, "Stupid? - Wrong Password");
					System.out.println("Wrong password");
				}
			}
		}

		if (!joined) {
			sendMess(session, "No such gameroom found - it doesnt exist or already started");
			System.out.println("No game with that name found - already started");
		}
	}

	private void leaveGame(Session session) {

		Player player = map.get(session);
		
		if(player == null){ //left befor registerd
			return;
		}
		if (player.getJoinedGame() == null) {
			System.out.println("Player is in no game");
			sendMess(session, "You cant leave, you aren't in a game");
		} else{
			Game game = player.getJoinedGame();

			if(game.numberOfPlayers() == 1){ //Only the player is in the game
				game.removePlayer(player);
				games.remove(game);
				player.joinGame(null);
			}else if (game.getCreator().equals(player)){ //The player is the creator
				game.setNewCreator();
				sendMess(game.getCreator().getSession(),"You're now the creator of the game: "+game.getGamename());
				game.removePlayer(player);
				player.joinGame(null);				
			}else{//player is a normal player
				game.removePlayer(player);				
			}
			sendMess(session, "You left successful the game: " + game.getGamename());
			System.out.println(player.getName() + " left the game " + game.getGamename());
		}
	}

	public void returnStatus(Session session) {
		Player p = map.get(session);
		sendMess(session, "Your name is: " + p.getName());
		sendMess(session, "Number of existing games: " + games.size());
		if (p.getJoinedGame() != null) {
			sendMess(session, "you're in game: " + p.getJoinedGame().getGamename());
			sendMess(session, "The creator is: " + p.getJoinedGame().getCreator().getName());
		} else {
			sendMess(session, "You're not in a game");
		}

	}

	private void sendMess(Session s, String mess) {
		try {
			if(s.isOpen())
				s.getBasicRemote().sendText(mess);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
