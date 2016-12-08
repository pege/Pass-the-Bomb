package websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.websocket.Session;

import org.json.JSONArray;
import org.json.JSONObject;


public class Game {
	
	private static final int finalScore = 100;
	private static final int lifetimeBomb = 100; // max Lifetime of a bomb
	private static final int scoreIncrease = 50;
	private static final int scoreDecrease = -10;

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

	public Player getOwner() {
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
	

	public void startGame(){
		started = true;
		bombOwner = pickRandom();
		bomb = createBomb();
		System.out.println(getGamename() + " has started");
	}

	public void bomb_exploded(Player p) {
		p.changeScore(scoreDecrease);
		
		for (Player player: players) {
			if (p != player)
			{
				player.changeScore(scoreIncrease);
			}
		}
	}
	
	private static Random random = new Random();
	
	private static int createBomb() {
		return random.nextInt(lifetimeBomb);
	}
	
	private Player pickRandom() {
		int size = players.size();
		int choice = random.nextInt(size);
		return players.get(choice);
	}
	
	
	public int indexOfPlayer(Player player){
		return players.indexOf(player);
	}

	
	//FIXME: DEPRECATED
	public String getPlayerInfos(){
		String s = "";
		for (Player p : players){
			s = s + "Name:" + p.getName() + ", UUID:" + p.getUuid(); 
		}
		return s;
	}
	
	public void broadcast_detailed_state()
	{
		broadcast(MessageFactory.SC_GameUpdate(this.toJSON(1)));
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
	
	public boolean isFinished(){
		for (Player p: players) {
			if (p.getScore() >= finalScore)
				return true;
		}
		return false;
	}
	
	
	public void destroy() {
		for (Player p : players) {
			p.leaveGame();
			p.resetScore();
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
