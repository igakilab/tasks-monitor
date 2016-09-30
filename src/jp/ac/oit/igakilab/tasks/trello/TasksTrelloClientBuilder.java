package jp.ac.oit.igakilab.tasks.trello;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.trello.TrelloApi.SimpleJsonResponseTextParser;

public class TasksTrelloClientBuilder {
	private static String apiKey(){
		String key = AppProperties.globalGet("tasks.trello.key");
		if( key != null ){
			return key;
		}else{
			System.err.println("ERROR: trello api key is undefined.");
			return null;
		}
	}

	private static String apiToken(){
		String token = AppProperties.globalGet("tasks.trello.token");
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

	public static boolean setTestApiKey(){
		if( AppProperties.globalIsValid() ){
			AppProperties.global.set("tasks.trello.key",
				"67ad72d3feb45f7a0a0b3c8e1467ac0b");
			AppProperties.global.set("tasks.trello.token",
				"268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
			return true;
		}
		return false;
	}
}
