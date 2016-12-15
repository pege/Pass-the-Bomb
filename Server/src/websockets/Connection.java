package websockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@ServerEndpoint("/passTheBomb")
public final class Connection {

	private static final long softTimeout = 5000; // timeout in ms
	private static final long hardTimeout = 15000; // timeout in ms
	private static final long timeBetweenPings = 10; // time to wait between
														// sending a ping

	private static Set<Game> games = new CopyOnWriteArraySet<>();
	private static Set<Session> registeredSessions = new CopyOnWriteArraySet<>();
	private static Map<Session, Player> map = new HashMap<>();

	private static ReentrantLock registerLock = new ReentrantLock(); // locks

	static {
		new Thread() {
			public void run() {
				 checkConnection();
			};
		}.start();
		System.out.println("Thread started");
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		// map.put(session, null); //TODO or not TODO

		System.out.println("Client Connected");
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			JSONTokener tokener = new JSONTokener(message);
			JSONObject mess = new JSONObject(tokener);
			JSONObject header = (JSONObject) mess.get("header");
			JSONObject body = (JSONObject) mess.get("body");

			int type = (int) header.get("type");

			System.out.println("Message received: " + header + " " + body);

			switch (type) {
			case MessageFactory.REGISTER:
				register(session, body);
				break; // map
			case MessageFactory.CREATE_GAME:
				createGame(session, body);
				break;
			case MessageFactory.JOIN_GAME:
				joinGame(session, body); // adds the player to a game
				break;
			case MessageFactory.LEAVE_GAME:
				leaveGame(session); // removes a player from a game
				break;
			case MessageFactory.START_GAME:
				startGame(session);
				break;
			case MessageFactory.PASS_BOMB:
				passBomb(session, body);
				break;
			case MessageFactory.EXPLODED:
				bombExplode(session);
				break;
			case MessageFactory.LIST_GAMES:
				getGameList(session);
				break;
			case MessageFactory.UPDATE_SCORE:
				update_score(session, body);
				break;
			// TODO MessageFactory.RECONNECT ?
			default:
				sendMess(session, MessageFactory.TypeError());
				System.out.println("Type Error");
				break;
			}
		} catch (JSONException e) {
			sendMess(session, "Stupid? - No valid JSON format");
		}

		// System.out.println("Number Of connected Clients: " +
		// registeredSessions.size());
	}

	@OnClose
	public void onClose(Session session) {
		registerLock.lock();
		try {
			leaveGame(session);
			if (map.get(session) != null)
				System.out.println(map.get(session).getName() + " just left us :(");
			registeredSessions.remove(session);
			map.remove(session);
			System.out.println("session closed");
		} finally {
			registerLock.unlock();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Connection Checking via Ping-pong

	public static void checkConnection() {
		while (true) {
			// System.out.println("number of registered sessions: " +
			// registeredSessions.size());
			for (Session session : registeredSessions) {
				registerLock.lock();// player is register - lock
				Player player = map.get(session);
				if (player != null) { // player not already left us
					synchronized (player) {
						registerLock.unlock();

						// get and setPong are synchronized
						if (Math.abs(player.getLastPong() - System.currentTimeMillis()) > hardTimeout) {
							System.out.println("================================================");
							System.out.println("============Connection timeout==================");
							System.out.println("================================================");
							try {
								session.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						if (Math.abs(player.getLastPong() - System.currentTimeMillis()) > softTimeout
								&& !player.isMaybeDisconnected()) {
							System.out.println("==================================");
							System.out.println("====Connection timeout (Maybe)====");
							System.out.println("==================================");
							player.setMaybeConnection(true);
							Game game = player.getJoinedGame();
							if (game != null) {
								synchronized (game) { // TODO
									game.broadcast_detailed_state(MessageFactory.SC_PLAYER_MAYBEDC);
								}
							}
						}
						// send a Ping with the current Time
						ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
						buffer.asLongBuffer().put(System.currentTimeMillis());
						try {
							if (session.isOpen()) {
								session.getAsyncRemote().sendPing(buffer);
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
				} else {
					registerLock.unlock();
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
		registerLock.lock();
		Player player = map.get(session);
		if (player != null) { // TODO can this ve null?
			synchronized (player) {
				registerLock.unlock();
				player.setLastPong(buffer.asLongBuffer().get());
			}
		} else {
			registerLock.unlock();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// methods a player can do

	private void register(Session session, JSONObject body) {
		final String username = (String) body.get("username");
		final String uuid = (String) body.get("user_id");

		boolean reconnect = false;
		registerLock.lock();
		try {
			if (map.containsKey(session)) {
				// player is already registered
				System.out.println("Second register try received");
				sendMess(session, MessageFactory.SC_denyRegister());
			} else {
				// does the player try to reconnect?
				Iterator<Player> it = map.values().iterator();
				while (it.hasNext()) {
					Player player = it.next();
					if (player != null && player.getUuid().equals(uuid)) {
						synchronized (player) {

							System.out.println("Reconnect attempt");
							// remove the old session
							Session oldSession = player.getSession();
							map.remove(oldSession);
							// map.put(oldSession, null);
							// add the new one
							map.put(session, player);

							player.setLastPong(System.currentTimeMillis());
							player.setSession(session);
							registeredSessions.add(session); // start pinging
							try {
								oldSession.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							// TODO - client
							// sendMess(session,
							MessageFactory.SC_GameUpdate(player.getJoinedGame().toJSON(1));
							// sendMess(session,
							// MessageFactory.sc_registerSuccessful());
							System.out.println("=== " + username + " has reconnected ===");
							reconnect = true;
						}
					}
				}
			}
			if (!reconnect) {
				Player p = new Player(username, uuid, session);
				p.setLastPong(System.currentTimeMillis());
				map.put(session, p);
				registeredSessions.add(session); // start pinging
				// TODO - client
				// sendMess(session,
				// MessageFactory.SC_GameUpdate(p.getJoinedGame().toJSON(1)));
				sendMess(session, MessageFactory.sc_registerSuccessful());
				System.out.println(username + " has been registered");
			}
		} finally {
			registerLock.unlock();
		}

	}

	private void createGame(Session session, JSONObject body) {
		final String gamename = (String) body.get("game_id");
		final String password = (String) body.get("password");

		registerLock.lock();
		Player owner = map.get(session);

		if (NeedRegister(session, owner)) {
			registerLock.unlock();
			return;
		}

		synchronized (owner) {
			registerLock.unlock();
			if (!alreadyInGame(session, owner)) {
				String newname = gamename;
				Game game;

				newname = makeUnique(gamename);

				game = new Game(owner, newname, password);
				games.add(game);

				owner.joinGame(game);

				sendMess(session, MessageFactory.SC_GameCreated(game.toJSON(1)));
				System.out.println("A game with gamename " + game.getGamename() + " was created");
			}
		}

	}

	private String makeUnique(final String proposed) {
		return makeUnique(proposed, 0);
	}

	private String makeUnique(String proposed, int level) {
		if (games.stream().anyMatch(game -> game.getGamename().equals(proposed)))
			return makeUnique(proposed + (level == 0 ? "_x" : "x"), level + 1);
		else
			return proposed;
	}

	private void getGameList(Session session) {
		JSONArray gameArray = new JSONArray();
		games.stream().filter(g -> !g.hasStarted()).map(g -> g.toJSON(0)).forEach(g -> gameArray.put(g));
		sendMess(session, MessageFactory.SC_GameList(gameArray));
	}

	private void joinGame(Session session, JSONObject body) {
		registerLock.lock();
		Player player = map.get(session);
		if (!NeedRegister(session, player)) {
			synchronized (player) {
				registerLock.unlock();
				if (!alreadyInGame(session, player)) {
					String gamename = (String) body.get("game_id");
					String password = (String) body.get("pw");

					Game game = null;
					for (Game g : games) {
						if (g.getGamename().equals(gamename)) {
							game = g;
							break;
						}
					}
					if (game != null && game.checkPassword(password)) {
						synchronized (game) {
							if (game.hasStarted()) {
								sendMess(session, MessageFactory.Already_Started_Error(game.getGamename()));
								System.out.println("Game already started");
							} else { // not started
								if (game.numberOfPlayers() >= 5) {
									sendMess(session, MessageFactory.SC_joinDenied());
									System.out.println("Game already full");
								} else {
									player.joinGame(game);
									game.addPlayer(player);
									// Send all players the updated game
									// status
									game.broadcast(MessageFactory.SC_PlayerJoined(game.toJSON(1)));
									System.out.println(player.getName() + " joined the game " + game.getGamename());
								}
							}
						}
					} else if (game != null) { // wrong password
						sendMess(session, MessageFactory.Wrong_Password_Error());
						System.out.println("Wrong password");
					}
					if (game == null) {// wrong gamename
						sendMess(session, MessageFactory.GameNotFoundError());
						System.out.println("No game with that name found");
					}
				}
			}
		} else {
			registerLock.unlock();
		}
	}

	private void leaveGame(Session session) {
		registerLock.lock();
		Player player = map.get(session);
		if (!NeedRegister(session, player)) {
			synchronized (player) {
				registerLock.unlock();
				if (!notInGame(session, player)) {
					Game game = player.getJoinedGame();
					synchronized (game) {
						player.leaveGame();
						game.removePlayer(player);
						if (game.numberOfPlayers() == 0) {
							games.remove(game);
							System.out.println("Game deleted: " + game.getGamename());
						} else {
							System.out.println(player.getName() + "left the game " + game.getGamename());
							game.broadcast(MessageFactory.SC_PlayerLeft(game.toJSON(1)));
						}
					}
				}
			}
		} else {
			registerLock.unlock();
		}

	}

	private void startGame(Session session) {
		registerLock.lock();
		Player player = map.get(session);
		if (!NeedRegister(session, player)) {
			synchronized (player) {
				registerLock.unlock();
				if (!notInGame(session, player)) {
					Game game = player.getJoinedGame();
					synchronized (game) {
						if (!NeedStarted(session, game, false)) {
							if (game.getOwner() != player) {
								sendMess(session, MessageFactory.NotGameOwnerError());
							} else {
								game.startGame();
								game.broadcast_detailed_state(MessageFactory.SC_GAME_STARTED);
							}
						}
					}
				}
			}
		} else {
			registerLock.unlock();
		}

	}

	private void passBomb(Session session, JSONObject body) {
		registerLock.lock();
		Player player = map.get(session);
		if (!NeedRegister(session, player)) {
			synchronized (player) {
				registerLock.unlock();
				if (!notInGame(session, player) && !NeedStarted(session, player.getJoinedGame(), true)
						&& !NeedBomb(session, player)) {
					boolean transfered = false;
					Game game = player.getJoinedGame();
					synchronized (game) {
						int bomb = (int) body.get("bomb");
						game.setBomb(bomb);
						String targetUUID = (String) body.get("target");

						for (Player p : game.getPlayers()) {
							if (p.getUuid().equals(targetUUID)) {
								game.setBombOwner(p);
								game.broadcast_detailed_state(MessageFactory.SC_BOMB_PASSED);
								transfered = true;
								break;
							}
						}
					}
					if (!transfered) {
						System.out.println("Error in Bomb passing, ID not found");
					}
				}
			}
		} else {
			registerLock.unlock();
		}

	}

	private void bombExplode(Session s) {
		// Inform other players
		registerLock.lock();
		Player player = map.get(s);
		if (!NeedRegister(s, player)) {
			synchronized (player) {
				registerLock.unlock();
				if (!notInGame(s, player) && !NeedStarted(s, player.getJoinedGame(), true) && !NeedBomb(s, player)) {
					Game game = player.getJoinedGame();
					game.bomb_exploded(player);
					game.broadcast_detailed_state(MessageFactory.SC_BOMB_EXPLODED);
					if (game.isFinished()) {
						game.destroy();
						System.out.println("Game Over");
					} else {
						game.startGame();
					}
				}
			}
		} else {
			registerLock.unlock();
		}
	}

	private void update_score(Session session, JSONObject body) {
		// Inform other players
		registerLock.lock();
		Player player = map.get(session);

		if (!NeedRegister(session, player)) {
			synchronized (player) {
				registerLock.unlock();
				if (!notInGame(session, player) && !NeedStarted(session, player.getJoinedGame(), true)
						&& !NeedBomb(session, player)) {
					int new_score = (int) body.get("score");
					player.setScore(new_score);
					player.getJoinedGame().broadcast_detailed_state(MessageFactory.SC_UPDATE_SCORE);
				}
			}
		} else {
			registerLock.unlock();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ERROR HADNLING

	private boolean NeedBomb(Session s, Player player) {
		if (!player.hasBomb()) {
			sendMess(s, MessageFactory.DoesntOwnBombError());
			return true;
		}
		return false;
	}

	private boolean NeedStarted(Session s, Game g, boolean status) {
		if (g.hasStarted() && !status) {
			sendMess(s, MessageFactory.Already_Started_Error(g.getGamename()));
			return true;
		} else if (!g.hasStarted() && status) {
			sendMess(s, MessageFactory.NotStartedError());
			return true;
		}
		return false;
	}

	private boolean NeedRegister(Session s, Player p) {
		if (p == null) {
			sendMess(s, MessageFactory.Not_Registered_Error());
			return true;
		}
		return false;
	}

	private boolean notInGame(Session s, Player p) {
		if (p.getJoinedGame() == null) {
			sendMess(s, MessageFactory.NotInGameError());
			return true;
		}
		return false;
	}

	private boolean alreadyInGame(Session s, Player p) {
		if (p.getJoinedGame() != null) {
			sendMess(s, MessageFactory.AlreadyInGameError());
			return true;
		}
		return false;
	}

	private void sendMess(Session s, String mess) {
		System.out.println(mess);
		try {
			if (s.isOpen())
				s.getBasicRemote().sendText(mess);
		} catch (IOException e) {
			e.printStackTrace();
			if (mess == null)
				System.out.println("Message creation failed");
		}
	}
}
