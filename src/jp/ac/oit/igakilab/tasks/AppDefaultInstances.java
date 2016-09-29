package jp.ac.oit.igakilab.tasks;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.trello.TrelloApi;

public class AppDefaultInstances {
	public static MongoClient createMongoClient(){
		String dbHost = AppProperties.globalGet("tasks.db.host", "localhost");
		int dbPort = Integer.parseInt(AppProperties.globalGet("tasks.db.port", "27017"));
		return new MongoClient(dbHost, dbPort);
	}

	public static TrelloApi getTrelloApiClient(){
		String apiKey = AppProperties.globalGet("tasks.trello.key");
		String apiToken = AppProperties.globalGet("tasks.trello.token");
		if( apiKey != null && apiToken != null ){
			return new TrelloApi(apiKey, apiToken);
		}else{
			DebugLog.logm("AppDefaultInstances", DebugLog.LS_ERROR,
				"getTrelloApiClient", "apikey or apitoken not defined");
			return null;
		}
	}
}
