package websockets;

import javax.websocket.Session;

import org.json.JSONObject;

public final class Player {
	
	private final String uuid;
	private String name;
	private int score;
	
	private boolean maybeDC;
	private Session session;
	private long lastPong;
	private Game inGame; //null if in no game
	
	
	
	public Player(String name, String uuid, Session session){
		this.name = name;
		this.uuid = uuid;
		this.session = session;
	}
	
	public boolean hasBomb() {
		if (inGame == null) return false;
		return inGame.getBombOwner() == this;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String newName){
		this.name = newName;
	}
	
	public String getUuid(){
		return uuid;
	}
	
	public int getScore(){
		return score;
	}
	
	public void setScore(int new_score){
		score = new_score;
	}
		
	public void changeScore(int amount){
		score = score + amount;
	}
	
	public void reset(){
		score = 0;
		inGame = null;
	}
	
	public void setMaybeConnection(boolean status){
		maybeDC = status;
	}
	
	public boolean isMaybeDisconnected(){
		return maybeDC;
	}
	
	public Session getSession(){
		return session;
	}
	
	public void setSession(Session s){
		session = s;
	}
	
	public void joinGame(Game game){
		inGame = game;
	}
	
	public void leaveGame(){
		reset();
	}
	
	public Game getJoinedGame(){
		return inGame;
	}

	public synchronized void setLastPong(long time){
		lastPong = time;
	}
	
	public synchronized long getLastPong(){
		return lastPong;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("uuid", uuid);
		o.put("name", name);
		o.put("score", score);
		o.put("disconnected", maybeDC);
		return o;
	}

}
