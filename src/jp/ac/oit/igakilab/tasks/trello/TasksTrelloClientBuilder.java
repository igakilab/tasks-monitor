package jp.ac.oit.igakilab.tasks.trello;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.http.TrelloApi;

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

	public static TrelloApi createApiClient(){
		String key = apiKey();
		String token = apiToken();
		if( key != null && token != null ){
			TrelloApi api = new TrelloApi(key, token);
			api.setErrorHandler(new TrelloApi.SimpleErrorHandler());
			return api;
		}
		return null;
	}
}
