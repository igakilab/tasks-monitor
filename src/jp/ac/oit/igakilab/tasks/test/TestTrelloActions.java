package jp.ac.oit.igakilab.tasks.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.http.TrelloApi;
import jp.ac.oit.igakilab.tasks.http.TrelloApi.Parameters;

public class TestTrelloActions {
	public static String TRELLO_API_KEY = "67ad72d3feb45f7a0a0b3c8e1467ac0b";
	public static String TRELLO_API_TOKEN = "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65";

	public static void main(String[] args){
		TrelloApi trello = new TrelloApi(TRELLO_API_KEY, TRELLO_API_TOKEN);

		Parameters params = new Parameters();
		params.setParameter("display", "true");
		Object reply = trello.get("/1/boards/57cfb3b08c566b61d9edc3f7/actions", params);

		JSONArray actions = (JSONArray)reply;
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
