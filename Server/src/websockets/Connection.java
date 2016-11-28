package websockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


@ServerEndpoint("/echo")
public class Connection {
	
	private static final long softTimeout = 5000; // timeout in ms
	private static final long hardTimeout = 15000; // timeout in ms
	private static final long timeBetweenPings = 10; // time to wait between sending a ping
	
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
		//unnötig? sendMess(session, "Connected but not registered");
		// map.put(session, null); 
		//sendMess(session, "Welcome to <<PASS THE BOMB>>");
		//sendMess(session,
		//		"Possible orders: register [name], create [gamename], refresh, join [gamename], leave, status, passBomb [playername], explode");

	}

	@OnMessage
	public void onMessage(String message, Session session) {
		JSONTokener tokener = new JSONTokener(message);
		JSONObject mess = new JSONObject(tokener);
		JSONObject header = (JSONObject) mess.get("header");
		JSONObject body = (JSONObject) mess.get("body");

		int type = (int) header.get("type");
		
		System.out.println("Message received: " + header + " " + body);

		switch (type) {
		case Message.REGISTER:
				register(session, body);
			break; // map
		case Message.CREATE_GAME:
				createGame(session, body);
			break;
		case Message.JOIN_GAME:
				joinGame(session, body); // adds the player to a game
			break;
		case Message.LEAVE_GAME:
			leaveGame(session); // removes a player from a game
			break;
		case Message.STATUS:
			returnStatus(session);
			break;
		case Message.START_GAME:
			startGame(session);
			break;
		case Message.PASS_BOMB:
			passBomb(session, body);
			break;
		case Message.EXPLODED:
			bombExplode(session);
			break;
		
		default:
			sendMess(session, Message.TypeError());
			System.out.println("Type Error");
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

	
	public static void checkConnection() {
		while (true) {
			// System.out.println("number of registered sessions: " +
			// registeredSessions.size());
			for (Session s : registeredSessions) {
				Player p = map.get(s); // FIXME: can this be null?
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
					// FIXME getAsyncRemote or getBasicRemote?
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
	// FIXME: Race Condition if p is removed from Playerlist?
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
	
	
	//Add to the session a Player and start pinging the session
	private void register(Session session, JSONObject body) {
		long uuid = (long) body.get("uuid");
		String username = (String) body.get("username");
		
		if (map.containsKey(session)) {
			//player is already registered
			System.out.println("Second register try received");
			sendMess(session, Message.denyRegister());
			//sendMess(session, "This connection is already registered with name: " + map.get(session).getName());
			return;// ?
		} 
		
			// does the player try to reconnect?
			Iterator<Player> it = map.values().iterator();
			while (it.hasNext()) {
				Player p = it.next();
				if (p != null && p.getUuid() == uuid) {
					System.out.println("Reconnect attempt");
					// remove the old session
					Session oldSession = p.getSession();
					map.remove(oldSession);
					//map.put(oldSession, null);

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
										
					sendMess(session, Message.SC_GameUpdate(p.getJoinedGame().toJSON(1)));
					System.out.println("=== " + username + " has reconnected ===");
					return;
				}
			
		}
		// if uuid already exist -- Reconnect
		

		Player p = new Player(username, uuid, session);
		// p.setMaybeConnection(false);
		//p.joinGame(null); // unnecessary
		p.setLastPong(System.currentTimeMillis());
		map.put(session, p);
		registeredSessions.add(session); // start pinging
		sendMess(session, "Successful Registered");
		System.out.println(username + " has been registered");

	}

	private void createGame(Session session, JSONObject body) {
		final String gamename = (String) body.get("game_id");
		String password = (String) body.get("password");
		Player owner = map.get(session);//player who creates a game is automatically the creator
		
		if (NeedRegister(session, owner) || NeedInGame(session, owner)) return;
			
		//FIXME: race condition if 
		Game game;
		synchronized (games) {
			Optional<Integer> largest = games.stream()
					.map(Game::getGamename)
					.filter(s -> s.startsWith(gamename))
					.map(s -> s.substring(gamename.length()))
					.map(s -> Integer.getInteger(s))
					.filter(i -> i != null)
					.sorted().findFirst();
			String newname = gamename;
			if (largest.isPresent()) {
				int addition = largest.get().intValue() + 1;
				newname = gamename + Integer.toString(addition);
			}
			game = new Game(owner, gamename, password);
			games.add(game);
		}
		owner.joinGame(game);
				
		sendMess(session, Message.SC_GameUpdate(game.toJSON(1)));
		System.out.println("A game with gamename " + game.getGamename() + " was created");
	}

	//FIXME: Race Condition, If game is deleted
	private boolean uniqueGamename(String name) {
		for (Game g : games) {
			if (g.getGamename().equals(name)) {
				return false;
			}
		}
		return true;
	}

	private void getLobbyList(Session session) {
		// FIXME: Does player need to be registered?
		
		JSONArray gameArray = new JSONArray();
		for (Game g: games) {
			if (!g.hasStarted()) 
				gameArray.put(g.toJSON(0));
		}
		sendMess(session, Message.SC_GameList(gameArray));
	}

	private void joinGame(Session session, JSONObject body) {
		Player player = map.get(session);
		if (NeedRegister(session, player) || NeedInGame(session, player)) return;
		
		String gamename = (String) body.get("game_id");
		String password = (String) body.get("pw");
		
		for (Game game : games) {
			if (game.getGamename().equals(gamename)) {
				if (game.checkPassword(password)) {
					
					if (game.hasStarted()){
						sendMess(session, Message.Already_Started_Error());
						System.out.println("Game already started");
						return;
					}
					
					player.joinGame(game);
					game.addPlayer(player);
					
					// Send all players the updated game status
					game.broadcast(Message.SC_GameUpdate(game.toJSON(1)));
					
					System.out.println(player.getName() + " joined the game " + game.getGamename());
					return;
				} else {
					sendMess(session, Message.Wrong_Password_Error());
					System.out.println("Wrong password");
					return;
				}
			}
		}
		
		sendMess(session, Message.GameNotFoundError());
		System.out.println("No game with that name found");
		
	}

	private void leaveGame(Session session) {
		Player player = map.get(session);
		if (NeedRegister(session, player) || NeedInGame(session, player)) return;
			
		Game game = player.getJoinedGame();
		player.leaveGame();
		
		if (game.numberOfPlayers() == 0) { // Only this player is in game
			//FIXME: Race Condition
			games.remove(game);
		} else {
			game.broadcast(Message.SC_GameUpdate(game.toJSON(1)));
		}
		
	}

	//FIXME: WTf?
	private void returnStatus(Session session) {
		try {
		Player p = map.get(session);
		sendMess(session, "Your name is: " + p.getName());
		sendMess(session, "Number of existing games: " + games.size());
		if (p.getJoinedGame() != null) {
			sendMess(session, "you're in game: " + p.getJoinedGame().getGamename());
			sendMess(session, "The creator is: " + p.getJoinedGame().getCreator().getName());
		} else {
			sendMess(session, "You're not in a game");
		}
		} catch (Exception e) {
			System.out.println("ouuuuuuuh");
		}
	}

	private void startGame(Session session) {
		Player player = map.get(session);
		if (NeedRegister(session, player) || NeedInGame(session, player)) return;
		
		Game game = player.getJoinedGame();
		if (NeedStarted(session, game, false)) return;
		
		if (game.getCreator() != player) {
			sendMess(session, Message.NotGameOwnerError());
		}
		else {
			game.startGame();
			game.broadcast_detailed_state();
		}
	}


	private void sendMess(Session s, String mess) {
		try {
			if (s.isOpen())
				s.getBasicRemote().sendText(mess);
		} catch (IOException e) {
			e.printStackTrace();
			if (mess == null)
				System.out.println("Message creation failed");
		}
	}

	private void passBomb(Session s, JSONObject body) {
		Player player = map.get(s);
		if (NeedRegister(s, player) || NeedInGame(s, player) || NeedStarted(s, player.getJoinedGame(), true)) return;
				
		long targetUUID = (long) body.get("target");
		
		//FIXME ?? übergeben wir hier den Score mit?
		int bomb = (int) body.get("bomb");
		
		//FIXME: Does the player tell its score via 'passbomb'?
		int score = (int) body.get("score");
		
		Game game = player.getJoinedGame();
		
		if (game.getBombOwner() != player) {
			sendMess(s, Message.DoesntOwnBombError());
			return;
		}
		
		//TODO: update score on Player if needed
		for (Player p : game.getPlayers()) {
			if (p.getUuid() == targetUUID) {
				game.setBombOwner(p);
				game.broadcast_detailed_state();
				break;
			}
		}
		
		//TODO: Player not in Game Error
		
		sendMess(s, "ID not found");
		System.out.println("Error in Bomb passing, ID not found");

	}


	private void bombExplode(Session s) {
		// Inform other players
		Player player = map.get(s);
		if (NeedRegister(s, player) || NeedInGame(s, player) || NeedStarted(s, player.getJoinedGame(), true)) return;
		
		Game game = player.getJoinedGame();
		
		game.bomb_exploded(player);		
		game.broadcast_detailed_state();
		
		
		if (game.isFinished()) {	
			game.destroy();
			games.remove(game);
			System.out.println("Game Over");
		} else {
			game.startGame();
		}
	}

	
	// ERROR HADNLING
	
	private boolean NeedRegister(Session s){
		Player player = map.get(s);
		return NeedRegister(s, player);
	}
	
	private boolean NeedStarted(Session s, Game g, boolean status){
		if (g.hasStarted() && !status) {
			sendMess(s, Message.Already_Started_Error());
			return true;
		} else if (!g.hasStarted() && status) {
			sendMess(s, Message.NotStartedError());
			return true;
		}
		return false;	
	}
	
	private boolean NeedRegister(Session s, Player p){
		if (p == null) {
			sendMess(s, Message.Not_Registered_Error());
			return true;
		}
		return false;
	}
	
	private boolean NeedInGame(Session s, Player p){
		if (p.getJoinedGame() == null) {
			sendMess(s, Message.NotInGameError());
			return true;
		}
		return false;
	}
	
}
