package jp.ac.oit.igakilab.tasks.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi.SimpleJsonResponseTextParser;

public class TestTrelloActions {
	public static String TRELLO_API_KEY = "67ad72d3feb45f7a0a0b3c8e1467ac0b";
	public static String TRELLO_API_TOKEN = "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65";

	static String board_koike = "57cfb3b08c566b61d9edc3f7";
	static String board_actions_test = "57d3f5cac2c3720549a9b8c1";

	public static void main(String[] args){
		TrelloApi<Object> trello = new TrelloApi<Object>(TRELLO_API_KEY, TRELLO_API_TOKEN
			, new SimpleJsonResponseTextParser());

		Map<String,String> params = new HashMap<String,String>();
		params.put("display", "true");
		Object reply = trello.rget("/1/boards/57d3f5cac2c3720549a9b8c1/actions", params).getData();

		JSONArray actions = (JSONArray)reply;
		Set<String> types = new HashSet<String>();
		for(Object data : actions){
			JSONObject action = (JSONObject)data;
			System.out.println(action.get("id"));
			System.out.println("\t" + parseISODate((String)action.get("date")));

			JSONObject display = (JSONObject)action.get("display");
			System.out.println("\t" + display.get("translationKey") + "(" + action.get("type") + ")");

			JSONObject memberCreator = (JSONObject)action.get("memberCreator");
			System.out.println("\t" + memberCreator.get("fullName") + "(" + memberCreator.get("username") + ")");

			JSONObject old = (JSONObject)((JSONObject)action.get("data")).get("old");
			if( old != null ) System.out.println("\t" + "OLD: " + old.toJSONString());

			System.out.println();

			if( !types.contains(action.get("type")) ){
				types.add((String)action.get("type"));
			}
		}

		System.out.println("RECEIVED TYPES");
		for(String type : types){
			System.out.println("\t" + type);
		}
	}

	public static Date parseISODate(String isodate){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		try{
			return df.parse(isodate);
		}catch(ParseException e0){
			System.err.println(e0.getMessage());
			return null;
		}
	}

}