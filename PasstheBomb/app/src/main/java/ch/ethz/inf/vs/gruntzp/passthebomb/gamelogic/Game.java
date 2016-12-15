package ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Michelle on 19.11.2016.
 * Class that describes a game.
 */

// implements Parcelable so that it can be put in putExtra()
public class Game implements Parcelable{

    private String name;
    private Player creator;
    private LinkedList<Player> players;
    private Boolean locked;
    private boolean started;
    private Bomb bomb;
    private Player bombOwner;

    public final int TAP_VALUE = 2;
    public static final int DEC_OKAY = 1;
    public static final int DEC_LAST = 2;
    public static final int DEC_ERROR = 3;
    public Lock bombLock = new ReentrantLock();

    public Game(String name, Player creator, Boolean locked, boolean started){
        this.name = name;
        this.creator = creator;

        this.players = new LinkedList<>();
        players.addFirst(creator);

        this.locked = locked;
        this.started = started;
        this.bomb = new Bomb(Bomb.blank_initializer,Bomb.blank_initializer);
        this.bombOwner = null;
    }

    public Game(Parcel in){
        name = in.readString();
        creator = in.readParcelable(Player.class.getClassLoader());
        players = new LinkedList<>();
        in.readList(players, Player.class.getClassLoader());
        locked = in.readByte() != 0;
        bomb = in.readParcelable(Bomb.class.getClassLoader());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorName() {return creator.getName();}

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public void newCreator(Player creator) {
        this.creator = creator;
        players.remove(0);
        players.addFirst(creator);
    }

    public Player getCreator() {return creator;}

    public LinkedList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(LinkedList<Player> players) {
        this.players = players;
    }

    public void setPlayersAndRoles(LinkedList<Player> players, String creatorUuid) {
        this.players = players;
        for(Player p : this.players) {
            if(p.isHasBomb())
                this.bombOwner = p;
            if(p.getUuid().equals(creatorUuid))
                this.creator = p;
        }
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public void removePlayer (Player player){
        players.remove(player);
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public boolean hasStarted() {return started;}

    public int getNoPlayers() {
        return players.size();
    }

    public void adoptScore(Game other) {
        LinkedList<Player> p1 = this.getPlayers();
        LinkedList<Player> p2 = other.getPlayers();
        if(this.getNoPlayers() == other.getNoPlayers()) {
            for(int i = 0; i < this.getNoPlayers(); i++) { //Players are never shuffled, so we don't check if uuids match
                p1.get(i).setScore(p2.get(i).getScore());
            }
        }
    }

    public Player getPlayerByID(String uuid) {
        Player ret = null;
        for(int i = 0; i < players.size(); i++) {
            if(players.get(i).getUuid().equals(uuid))
                ret = players.get(i);
        }
        return ret;
    }

    /*public Bomb getBomb() {
        return bomb;
    }*/

    public Player getBombOwner() {
        return bombOwner;
    }

    public void setBomb(int bomb) {
        this.bomb.setCounter(bomb);
    }

    public void newBomb(Bomb b) {this.bomb = b;}

    public int getBombInitValue() {
        return bomb.initValue();
    }

    public void setBombOwner(Player bombOwner) {
        this.bombOwner = bombOwner;
    }

    public int getBombValue() {
        return bomb.valueOf();
    }

    public synchronized int decreaseBomb() { //Synchronized to avoid race condition
        int v = bomb.valueOf();
        if (v > 1) { //No problem, score increase allowed
            bomb.decrease();
            return DEC_OKAY;
        } else if(v == 1) { //Last decrement, score increase allowed but the bomb explodes
            bomb.decrease();
            return DEC_LAST;
        } else { //Bomb already set to zero by another thread
            return DEC_ERROR;
        }
    }

    // leave this empty please
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(creator, flags);
        dest.writeList(players);
        dest.writeByte((byte) (locked ? 1 : 0));
        dest.writeParcelable(bomb, flags);
    }

    public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>()
    {
        public Game createFromParcel(Parcel in)
        {
            return new Game(in);
        }
        public Game[] newArray(int size)
        {
            return new Game[size];
        }
    };

    public static Game createFromJSON(String jsonGame) {
        JSONObject gameInfo = null;
        JSONTokener tokener = new JSONTokener(jsonGame);
        try {
            gameInfo = new JSONObject(tokener);
            gameInfo = gameInfo.getJSONObject("game");
            //Retrieve players from game
            JSONArray jArray = gameInfo.getJSONArray("players");
            Player p;
            Player c = null;
            String uuid = gameInfo.getString("owner");
            String bombUuid = gameInfo.getString("bombOwner");
            Game game = new Game(gameInfo.getString("name"), null,
                    gameInfo.getBoolean("hasPassword"),
                    gameInfo.getBoolean("started")); //This is evil, null creator should usually be avoided and is okay here because it is set just afterwards
            for(int i = 0; i < jArray.length(); i++) {
                p = new Player(jArray.getJSONObject(i).getString("name"), jArray.getJSONObject(i).getString("uuid"));
                p.setHasBomb(p.getUuid().equals(bombUuid));
                if (uuid.equals(p.getUuid())) {
                    c = p;
                    game.newCreator(c);
                } else {
                    game.addPlayer(p);
                }
            }
            game.setCreator(c);
            return game;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Game createFromJSON(JSONObject gameInfo) {
            return createFromJSON(gameInfo.toString());
    }

    public static Game createFromJSON0(JSONObject gameInfo) {
        try {
            //Retrieve players from game
            Game game = new Game(gameInfo.getString("name"), null,
                    gameInfo.getBoolean("hasPassword"), false);
            return game;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
