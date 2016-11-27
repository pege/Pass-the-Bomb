package websockets;

import javax.websocket.Session;

import org.json.JSONObject;

public class Player {
	
	private long uuid;
	private String name;
	private int score;
	//TODO: vielleicht umschreiben
	private boolean hasBomb;
	//TODO connected is same as registered => map points to player
	private boolean maybeDC; //if connected or not //TODO i think it's redundant (It's checked if map points to NULL or not)
	private Session session;
	private long lastPong;
	private Game inGame; //null if in no game
	
	
	
	public Player(String name, long uuid, Session session){
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
	
	public long getUuid(){
		return uuid;
	}
	
	public int getScore(){
		return score;
	}
	
//	public void setBomb(boolean hasB){
//		hasBomb = hasB;
//	}
	
	
	public void changeScore(int amount){
		score = score +amount;
	}
	
	public void resetScore(){
		score = 0;
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
		inGame = null;
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
		o.put("connected", maybeDC);
		return o;
	}

}
