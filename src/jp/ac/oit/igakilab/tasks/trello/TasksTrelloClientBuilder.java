package jp.ac.oit.igakilab.tasks.trello;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.trello.api.SimpleJsonResponseTextParser;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TasksTrelloClientBuilder {
	public static String REGEX_TODO = "(?i)to\\s*do";
	public static String REGEX_DOING = "(?i)doing";
	public static String REGEX_DONE = "(?i)done";

	private static String apiKey(){
		String key = AppProperties.global.get("tasks.trello.key");
		if( key != null ){
			return key;
		}else{
			System.err.println("ERROR: trello api key is undefined.");
			return null;
		}
	}

	private static String apiToken(){
		String token = AppProperties.global.get("tasks.trello.token");
		if( token != null ){
			return token;
		}else{
			System.err.println("ERROR: trello api token is undefined.");
			return null;
		}
	}

	public static TrelloApi<Object> createApiClient(){
		String key = apiKey();
		String token = apiToken();
		if( key != null && token != null ){
			TrelloApi<Object> api = new TrelloApi<Object>(key, token, new SimpleJsonResponseTextParser());
			return api;
		}
		return null;
	}

	public static String TEST_API_KEY = "67ad72d3feb45f7a0a0b3c8e1467ac0b";
	public static String TEST_API_TOKEN = "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65";

	public static boolean setTestApiKey(){
		if( AppProperties.globalIsValid() ){
			AppProperties.global.set("tasks.trello.key", TEST_API_KEY);
			AppProperties.global.set("tasks.trello.token", TEST_API_TOKEN);
			return true;
		}
		return false;
	}
}
