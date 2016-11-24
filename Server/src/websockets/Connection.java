package websockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
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

	private static final int lifetimeBomb = 100; // max Lifetime of a bomb
	private static final long softTimeout = 5000; // timeout in ms
	private static final long hardTimeout = 15000; // timeout in ms
	private static final long timeBetweenPings = 10; // time to wait between sending a ping
	
	private static final int finalScore = 100;
	private static final int scoreIncrease = 50;
	private static final int scoreDecrease = -10;
	
	// max times of clients: timeout/timeBetweenPings

	// games as a concurrent Set
	static Set<Game> games = Collections.newSetFromMap(new ConcurrentHashMap<Game, Boolean>());
	// All sessions
	static Set<Session> registeredSessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());

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
		// map.put(session, null); //TODO should we do that?
		sendMess(session, "Welcome to <<PASS THE BOMB>>");
		sendMess(session,
				"Possible orders: register [name], create [gamename], refresh, join [gamename], leave, status, passBomb [playername], explode");

	}

	@OnMessage
	public void onMessage(String message, Session session) {

		// long uuid = 1234;
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

		if (!header.equals("register") && map.get(session) == null) {
			sendMess(session, "Please register yourself befor start gaming");
			return;
		}

		switch (header) {
		case "register":
			if (body.equals(""))
				register("Default Name", 1234, session); // create player and
															// add to the
			else
				register(body, body.hashCode(), session);
			break; // map
		case "create":
			if (body.equals(""))
				createGame(session, "Default Gamename", password);// open a game
																	// and add
																	// the
																	// player as
																	// the
																	// creator
			else
				createGame(session, body, password);
			break;
		case "refresh": // or join
			getLobbyList(session); // returns all current games
			break;
		case "join":
			if (body.equals(""))
				sendMess(session, "Which game you wanna join?");
			else
				joinGame(session, body, password); // adds the player to a game
			break;
		case "leave":// TODO can u do it intentionally?, next creator random?
			leaveGame(session); // removes a player from a game
			break;
		case "status":
			returnStatus(session);
			break;
		case "start":
			startGame(session);
			break;
		case "passBomb":
			// TODO what do we receive from the client? id
			int targetUUID = body.hashCode();
			int bomb = 30;
			passBomb(session, targetUUID, bomb);
			break;
		case "explode":
			bombExplode(session);
			break;

		// reconnect denied or gameupdate
		// TODO passBomb(score,bomb)
		// TODO how to play HTML
		default:
		}
		// System.out.println("Message from " + session.getId() + ": " +
		// message);
		System.out.println("games:" + Integer.toString(games.size()));

		// System.out.println(games.size());
		for (Session s : registeredSessions) {
			System.out.print("Player according to a registered session: ");
			System.out.println(map.get(s) == null ? "null" : map.get(s).getName());
		}
	}

	@OnClose
	public void onClose(Session session) {
		leaveGame(session);
		if (map.get(session) != null)
			System.out.println(map.get(session).getName() + " just left us :(");
		registeredSessions.remove(session);
		map.remove(session);
		System.out.println("session closed");
	}

	/*
	 * @OnError public void onError(Session session, Throwable t) {
	 * System.out.println("H�u, error?"); System.out.println(t.getMessage()); }
	 */

	public static void checkConnection() {
		while (true) {
			// System.out.println("number of registered sessions: " +
			// registeredSessions.size());
			for (Session s : registeredSessions) {
				Player p = map.get(s); // TODO can this be null?
				// check last received pong
				if (Math.abs(p.getLastPong() - System.currentTimeMillis()) > hardTimeout) {
					System.out.println("================================================");
					System.out.println("============Connection timeout==================");
					System.out.println("================================================");
					try {
						p.getSession().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (Math.abs(p.getLastPong() - System.currentTimeMillis()) > softTimeout && !p.isMaybeDisconnected()) {
					System.out.println("==================================");
					System.out.println("====Connection timeout (Maybe)====");
					System.out.println("==================================");
					p.setMaybeConnection(true);
					if (p.getJoinedGame() != null) {
						for (Player otherP : p.getJoinedGame().getPlayers()) {
							if (otherP != p) {
								try {
									p.getSession().getBasicRemote()
											.sendText("Player " + p.getName() + " has a stupid connection");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}

				}
				// send a Ping with the current Time
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
				buffer.asLongBuffer().put(System.currentTimeMillis());
				try {
					// TODO getAsyncRemote or getBasicRemote?
					if (p.getSession().isOpen()) {
						p.getSession().getAsyncRemote().sendPing(buffer);
						// System.out.println("--Ping sended");
					}
					try {
						Thread.sleep(timeBetweenPings);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IllegalArgumentException | IOException e) {
					e.printStackTrace();
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
	
	//Add to the session a Player and start pinging the session
	private void register(String username, long uuid, Session session) {
		if (map.get(session) != null) {// already registered
			System.out.println("Second register try received");
			sendMess(session, "This connection is already registered with name: " + map.get(session).getName());
			return;// ?
		}
		// if uuid already exist -- Reconnect
		Iterator<Player> it = map.values().iterator();
		while (it.hasNext()) {
			Player p = it.next();
			if (p != null && p.getUuid() == uuid) {
				System.out.println("Reconnect attempt");
				// remove the old session
				Session oldSession = p.getSession();

				map.put(oldSession, null);

				// add the new one
				map.put(session, p);

				p.setLastPong(System.currentTimeMillis());
				p.setSession(session);
				registeredSessions.add(session); // start pinging

				try {
					oldSession.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				sendMess(session, "Successful Reconnected");
				System.out.println("===" + username + " has reconnected===");
				return;

			}
		}

		Player p = new Player(username, uuid, session);
		// p.setMaybeConnection(false);
		//p.joinGame(null); // unnecessary
		p.setLastPong(System.currentTimeMillis());
		map.put(session, p);
		registeredSessions.add(session); // start pinging
		sendMess(session, "Successful Registered");
		System.out.println(username + " has been registered");

	}

	private void createGame(Session session, String gamename, String password) {

		Player creator = map.get(session);//player who creates a game is automatically the creator
		if (creator.getJoinedGame() != null) { // already in a Game
			sendMess(session, "Stupid? You're already in a game");
			return;
		}
		// if the gamename is not unique it adds a number to the current gamename
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
			sendMess(session, "Game: " + g.getGamename() + ", Number of Players: " + g.numberOfPlayers()
					+ ", Password needed: " + g.passwordSet());
		}

	}

	private void joinGame(Session session, String gamename, String password) {
		Player player = map.get(session);
		if (player.getJoinedGame() != null) { // already in a Game
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
					// Send all others that a new player joined their game
					for (Player joinedPl : game.getPlayers()) {
						if (joinedPl != player) {
							sendMess(joinedPl.getSession(), "Player " + player.getName() + " joined your game.");
							sendMess(joinedPl.getSession(), "His id: " + Long.toString(player.getUuid()));
							sendMess(player.getSession(), "Player " + joinedPl.getName() + " is in this game already");
							sendMess(player.getSession(), "His id: " + Long.toString(joinedPl.getUuid()));
						}
					}

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

		if (player == null) { // left befor registerd
			return;
		}
		if (player.getJoinedGame() == null) {
			System.out.println("Player isnt in a game which he can leave");
			sendMess(session, "No game to leave found");
		} else {
			Game game = player.getJoinedGame();

			if (game.numberOfPlayers() == 1) { // Only the player is in the game
				game.removePlayer(player);
				games.remove(game);
				player.joinGame(null);
			} else if (game.getCreator().equals(player)) { // The player is the
															// creator
				game.setNewCreator();
				sendMess(game.getCreator().getSession(), "You're now the creator of the game: " + game.getGamename());
				game.removePlayer(player);
				player.joinGame(null);
				for (Player joinedPl : game.getPlayers()) { // Inform the other
															// players
					sendMess(joinedPl.getSession(), player.getName() + " left the game");
					sendMess(joinedPl.getSession(), "His uuid: " + player.getUuid());
					
				}
			} else {// player is a normal player
				game.removePlayer(player);
				player.joinGame(null);
				for (Player joinedPl : game.getPlayers()) { // Inform the other
															// players
					sendMess(joinedPl.getSession(), player.getName() + " left the game");
				}
			}
			sendMess(session, "You left successful the game: " + game.getGamename());
			System.out.println(player.getName() + " left the game " + game.getGamename());
		}
	}

	private void returnStatus(Session session) {
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

	private void startGame(Session session) {

		Player player = map.get(session);
		Game game = player.getJoinedGame();
		if (game == null) {
			sendMess(session, "You're in no game");
		} else if (game.getCreator() != player) {// creator cant start the game
			sendMess(session, "Only the creator can start the game");
		} else {
			// TODO synchronized
			startNewRound(game);
		}

	}

	private int createBomb() {
		Random r = new Random();
		return r.nextInt(lifetimeBomb);
	}

	private void sendMess(Session s, String mess) {
		try {
			if (s.isOpen())
				s.getBasicRemote().sendText(mess);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void passBomb(Session fromSession, int targetUUID, int bomb) {
		if (!map.get(fromSession).hasBomb()) {
			sendMess(fromSession, "Stupid? You dont got the bomb!!");
			return;
		}
		for (Player p : map.get(fromSession).getJoinedGame().getPlayers()) {
			if (p.getUuid() == targetUUID) {
				if (!p.isMaybeDisconnected()) {// TODO can we send here the bomb
												// or not?
					if (p == map.get(fromSession)) {
						sendMess(fromSession, "You already have the bomb");
					} else {
						sendBomb(p.getSession(), bomb);
						map.get(fromSession).setBomb(false);
						p.setBomb(true);
					}
					return; // Only break
				} else {
					sendMess(fromSession, "The target Player is maybe DC, the server tries to fix the connection");
				}

			}
		}
		sendMess(fromSession, "ID not found");
		System.out.println("Error in Bomb passing, ID not found");

	}

	private void sendBomb(Session session, int bomb) {

		Player targetPlayer = map.get(session);
		for (Player p : targetPlayer.getJoinedGame().getPlayers()) {

			if (p != targetPlayer) { // players who dont receive the bomb get
										// informed
				sendMess(p.getSession(), "Player " + targetPlayer.getName() + " has received the bomb");
			} else {
				sendMess(session, Integer.toString(bomb));
				sendMess(session, "You received the bomb");
			}
		}
		System.out.println("Bomb of game " + targetPlayer.getJoinedGame().getGamename() + " has moved to "
				+ targetPlayer.getName());
	}

	private void bombExplode(Session loserSession) {
		// Inform other players
		Player loserPlayer = map.get(loserSession);

		loserPlayer.setBomb(false);
		loserPlayer.changeScore(scoreDecrease);
		sendMess(loserSession, "You loose " + Integer.toString(scoreDecrease) + " score, your new score: " + Integer.toString(loserPlayer.getScore()));
		// inform the other players and their score
		for (Player winnerPlayer : loserPlayer.getJoinedGame().getPlayers()) {
			if (winnerPlayer != loserPlayer) {
				sendMess(winnerPlayer.getSession(), loserPlayer.getName() + " was very stupid, the bomb exploded, rip");
				winnerPlayer.changeScore(scoreIncrease);
				sendMess(winnerPlayer.getSession(),
						"Your score increases by " + Integer.toString(scoreIncrease) + ", your score now: " + Integer.toString(winnerPlayer.getScore()));
			}
		}

		if (endGame(loserPlayer.getJoinedGame())) {
			
			Player winner = highestScore(loserPlayer.getJoinedGame());
			int winnerScore = winner.getScore();
			
			games.remove(winner.getJoinedGame());
			
			for (Player p : loserPlayer.getJoinedGame().getPlayers()) {
				sendMess(p.getSession(), "GAME OVER");
				sendMess(p.getSession(), winner.getName() + " is the winner with a score of " + winnerScore);
				p.leaveGame();
				p.resetScore();
			}
			sendMess(winner.getSession(), "====Congratulations====");
			System.out.println("Game Over");
			
		} else {
			startNewRound(loserPlayer.getJoinedGame());
		}
	}

	private void startNewRound(Game game) {
		int startPlayer = new Random().nextInt(game.getPlayers().size());
		int i = 0;
		for (Player p : game.getPlayers()) {
			sendMess(p.getSession(), "Game started");
			if (i == startPlayer) {
				p.setBomb(true);
				sendBomb(p.getSession(), createBomb());
			}
			i++;
		}
		System.out.println(game.getGamename() + " has started");
	}

	private boolean endGame(Game game) {
		for (Player p : game.getPlayers()) {
			if (p.getScore() >= finalScore)
				return true;
		}
		return false;
	}
	
	private Player highestScore(Game g){
		int hSc = Integer.MIN_VALUE;
		Player currentP = null;
		for(Player p : g.getPlayers()){
			if(p.getScore() > hSc){ //TODO what if same score
				currentP = p;
				hSc = p.getScore();
			}
		}
		return currentP;
	}

}
