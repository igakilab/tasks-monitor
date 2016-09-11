package jp.ac.oit.igakilab.tasks.db;

import com.mongodb.MongoClient;

public class MongoTrelloBoardActionUpdater {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "board-actions";

	//private MongoClient client;

	public MongoTrelloBoardActionUpdater(MongoClient client){
		//this.client = client;
	}

	public MongoTrelloBoardActionUpdater(String host, int port){
		//this.client = new MongoClient(host, port);
	}
}
