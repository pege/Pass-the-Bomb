//package websockets;
package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by niederbm on 11/25/16.
 */

public class MessageFactory {
	
	// FROM CLIENT TO SERVER
    public static final int CREATE_GAME = 1; 	// server
    public static final int LIST_GAMES = 2; 	// server
    public static final int REGISTER = 3; 		// server
    public static final int JOIN_GAME = 4; 		// server
    public static final int LEAVE_GAME = 5; 	// server
    public static final int RECONNECT = 6; 		// --
    public static final int PASS_BOMB = 7;		// server
    public static final int EXPLODED = 8;		// server
    public static final int START_GAME = 9;	// server
    public static final int UPDATE_SCORE = 10;
    
 // FROM SERVER TO CLIENT
    public static final int SC_GAME_LIST = 11;
    public static final int SC_GAME_UPDATE = 12;
    public static final int SC_REGISTER_SUCCESSFUL = 13;
    public static final int SC_GAME_CREATED = 14;
    public static final int SC_PLAYER_JOINED = 15;
    public static final int SC_PLAYER_LEFT = 16;
    public static final int SC_UPDATE_SCORE = 17;
    public static final int SC_PLAYER_MAYBEDC = 18;
    public static final int SC_GAME_STARTED = 19; 
    public static final int SC_BOMB_PASSED = 20;
    public static final int SC_BOMB_EXPLODED = 21;
    
    // ERRORS (and, mysteriously, STATUS)
    public static final int PARSE_ERROR = -1;
    public static final int TYPE_ERROR = -2;
    public static final int STATUS = -3;
    public static final int NOT_REGISTERED_ERROR = -4;
    public static final int ALREADY_IN_GAME_ERROR = -5;
    public static final int WRONG_PASSWORD_ERROR = -6;
    public static final int ALREADY_STARTED_ERROR = -7;
    public static final int GAME_NOT_FOUND_ERROR = -8;
    public static final int NOT_IN_GAME_ERROR = -9;
    public static final int NOT_GAME_OWNER_ERROR = -10; 
    public static final int DOESNT_OWN_BOMB_ERROR = -11;
    public static final int NOT_STARTED_ERROR = -12;
    public static final int SC_RECONNECT_DENIED_ERROR = -13;

    
    // GAMEUPDATEREASONS
    public static final int UR_UNDEFINED = -1;
//    public static final int UR_LOBBY_PLAYER_UPDATED = -1;
//    public static final int UR_UNDEFINED = -1;
//    public static final int UR_UNDEFINED = -1;
//    public static final int UR_UNDEFINED = -1;
    
    // CLIENT TO SERVER CS_


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

    public static String joinGame(String game_id, String password) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", JOIN_GAME);
            body.put("game_id", game_id);
            body.put("pw", password);

            return compose(header,body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String joinGame(String game_id) {
        return joinGame(game_id, null);
    }

    public static String leaveGame() {
        try {
            JSONObject header = new JSONObject();

            header.put("type", LEAVE_GAME);

            return compose(header);
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

    
    public static String updateScore(int bomb, int score) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", UPDATE_SCORE);
            body.put("bomb", bomb);
            body.put("score", score);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    
    // SERVER TO CLIENT SC_
    
    public static String SC_denyRegister() {
        try {
            JSONObject header = new JSONObject();

            header.put("type", SC_RECONNECT_DENIED_ERROR);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sc_registerSuccessful() {
        try {
            JSONObject header = new JSONObject();

            header.put("type", SC_REGISTER_SUCCESSFUL);

            return compose(header);
        } catch( JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String ParseError() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", PARSE_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String AlreadyInGameError() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", ALREADY_IN_GAME_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
 
    public static String NotInGameError() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", NOT_IN_GAME_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String NotGameOwnerError() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", NOT_GAME_OWNER_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String TypeError() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", TYPE_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}
    
    public static String GameNotFoundError() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", GAME_NOT_FOUND_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}
    
    public static String Already_Started_Error(String gameName) {
        try {
            JSONObject header = new JSONObject();
            header.put("type", ALREADY_STARTED_ERROR);
            JSONObject body = new JSONObject();
            body.put("game_id", gameName);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}
    
    public static String Not_Registered_Error() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", NOT_REGISTERED_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String DoesntOwnBombError() {
		try {
            JSONObject header = new JSONObject();
            header.put("type", DOESNT_OWN_BOMB_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}
    
    public static String NotStartedError() {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", NOT_STARTED_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}

    
    public static String Wrong_Password_Error() {
        try {
            JSONObject header = new JSONObject();
            header.put("type", WRONG_PASSWORD_ERROR);
            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_GameList(JSONArray gameArray) {
        try {
            JSONObject header = new JSONObject();
            header.put("type", SC_GAME_LIST);
            JSONObject body = new JSONObject();
            body.put("games", gameArray);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_GameUpdate(JSONObject game) {
        try {
            JSONObject header = new JSONObject();
            header.put("type", SC_GAME_UPDATE);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_GameCreated(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_GAME_CREATED);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_PlayerJoined(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_PLAYER_JOINED);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_PlayerLeft(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_PLAYER_LEFT);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_UpdateScore(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_UPDATE_SCORE);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_PlayerMaybeDC(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_PLAYER_MAYBEDC);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_GameStarted(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_GAME_STARTED);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_BombPassed(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_BOMB_PASSED);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String SC_BombExploded(JSONObject game) {
    	try {
            JSONObject header = new JSONObject();
            header.put("type", SC_BOMB_EXPLODED);
            JSONObject body = new JSONObject();
            body.put("game", game);
            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
   

    public static String passBomb(String target_uiid, int bomb) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", PASS_BOMB);
            body.put("target", target_uiid);
            body.put("bomb", bomb);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String exploded() {
        try {
            JSONObject header = new JSONObject();

            header.put("type", EXPLODED);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getGames() {
        try {
            JSONObject header = new JSONObject();

            header.put("type", LIST_GAMES);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String startGame() {
        try {
            JSONObject header = new JSONObject();

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
