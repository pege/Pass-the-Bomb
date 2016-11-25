package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by niederbm on 11/25/16.
 */

public class Message {

    public static final int CREATE_GAME = 1;
    public static final int LIST_GAMES = 2;
    public static final int REGISTER = 3;
    public static final int JOIN_GAME = 4;
    public static final int LEAVE_GAME = 5;
    public static final int PLAYER_LIST = 6;
    public static final int PLAYER_UNREACHABLE = 7;
    public static final int RECONNECT = 8;
    public static final int GAME_UPDATE = 9;
    public static final int END_OF_ROUND = 10;
    public static final int RECONNECT_DENY = 11;
    public static final int PASS_BOMB = 12;
    public static final int EXPLODED = 13;
    public static final int GAME_OVER = 14;
    public static final int INHERIT_CREATOR = 15;

    public static final int PARSE_ERROR = -1;
    public static final int TYPE_ERROR = -2;
    public static final int STATUS = -3;
    public static final int NOT_REGISTERED_ERROR = -4;
   
    public static final int START_GAME = 16;


    public static String createGame(String game_id, String password)
    {
        try {
            JSONObject header = new JSONObject();
            header.put("type", CREATE_GAME);

            JSONObject body = new JSONObject();
            body.put("game_id", game_id);
            body.put("password", password);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String listGames() {
        try {

            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();
            header.put("type", LIST_GAMES);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String register(String user_id, String username) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", REGISTER);
            body.put("user_id", user_id);
            body.put("username", username);

            return compose(header,body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String joinGame(String game_id) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", JOIN_GAME);
            body.put("game_id", game_id);

            return compose(header,body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String leaveGame() {
        try {
            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();

            header.put("type", LEAVE_GAME);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String playerList(String[] player_names) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", PLAYER_LIST);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String playerUnreachable(String player_id) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", PLAYER_UNREACHABLE);
            body.put("player_id", player_id);

            return compose(header,body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String reconnect(String user_id) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", RECONNECT);
            body.put("user_id", user_id);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String gameUpdate(String bomb_player, int[] scores) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", GAME_UPDATE);
            body.put("bomb_player", bomb_player);
            body.put("scores", scores);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String endOfRound(int[] scores) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", END_OF_ROUND);
            body.put("scores", scores);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String reconnectDeny() {
        try {
            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();

            header.put("type", RECONNECT_DENY);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String ParseError() {
		JSONObject header = new JSONObject();
		header.put("type", PARSE_ERROR);
		return compose(header);
	}
    
    public static String TypeError() {
		JSONObject header = new JSONObject();
		header.put("type", TYPE_ERROR);
		return compose(header);
	}
    
    public static String NOT_REGISTERED_ERROR() {
		JSONObject header = new JSONObject();
		header.put("type", NOT_REGISTERED_ERROR);
		return compose(header);
	}

   

    public static String passBomb(String target_id) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", PASS_BOMB);
            body.put("target_id", target_id);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String exploded() {
        try {
            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();

            header.put("type", EXPLODED);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String gameOver(int[] scores) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", GAME_OVER);
            body.put("scores", scores);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String inheritCreator() {
        try {
            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();

            header.put("type", INHERIT_CREATOR);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String startGame() {
        try {
            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();

            header.put("type", START_GAME);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String compose(JSONObject header, JSONObject body) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("header", header);
        o.put("body", body);
        return o.toString();
    }

    public static String compose(JSONObject header) throws JSONException {
        return compose(header, new JSONObject());
    }
}
