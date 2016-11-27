package websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.websocket.Session;

import org.json.JSONArray;
import org.json.JSONObject;


public class Game {

	private Player owner;
	private ArrayList<Player> players = new ArrayList<>();

	private String gameName;
	private String password;
	
	private boolean started;
	
	private Player bombOwner;
	private int bomb;
	

	public Game(Player owner, String gamename, String password) {
		this.gameName = gamename;
		this.password = password;
		this.owner = owner;
		players.add(owner);
	}

	public Player getBombOwner() { return bombOwner; }
	public void setBombOwner(Player owner) { bombOwner = owner; } 
	
	public boolean hasStarted() { return started; }
	
	public int getBomb() { return bomb; }
	public void setBomb(int value) { bomb = value; }
	
	public String getGamename() {
		return gameName;
	}

	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}
	
	
	public boolean hasPassword(){
		return password != "";
	}
	

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
		if (player == owner)
			owner = players.get(0);
	}

	public String getPlayersName() {

		String s = "";
		for (Player p : players) {
			s = s + p.getName() + ", ";
		}

		return s;
	}

	public Player getCreator() {
		return owner;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public boolean playerInGame(Player p) {
		return players.contains(p);
	}

	public int numberOfPlayers() {
		return players.size();
	}
	
	//TODO
	public void startGame(){
		started = true;
		
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
	}
	
	//TODO
	private int createBomb() {
		Random r = new Random();
		return r.nextInt(lifetimeBomb);
	}
	
	public int indexOfPlayer(Player player){
		return players.indexOf(player);
	}

	public String getPlayerInfos(){
		String s = "";
		for (Player p : players){
			s = s + "Name:" + p.getName() + ", UUID:" + p.getUuid(); 
		}
		return s;
	}
	
	public void broadcast(String message) {
		for (Player player : players) {
			Session s = player.getSession();

			try {
				if (s.isOpen())
					s.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public JSONObject toJSON(int level) {
		JSONObject o = new JSONObject();
		o.put("name", gameName);
		o.put("hasPasswort", password != "");
		o.put("noP", numberOfPlayers());
		
		if (level > 0) {
		o.put("owner", owner.getUuid());
		o.put("started", started);
		o.put("hasPasswort", hasPassword());
		o.put("bombOwner", bombOwner);
		o.put("bomb", bomb);
		
		JSONArray players = new JSONArray();
		for (Player player: this.getPlayers())
			players.put(player.toJSON());
		}
		
		return o;	
	}
}
