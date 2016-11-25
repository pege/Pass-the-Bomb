package websockets;

import org.json.simple.JSONObject;

public class ServerMessages {
	public static String ParseError(){
		return 
	}
	
	public static String buildMessage(JSONObject header, JSONObject body)
	{
		JSONObject o = new JSONObject();
		o.put("header", header);
		o.put("body", body);
		return o.toJSONString();
	}
}
