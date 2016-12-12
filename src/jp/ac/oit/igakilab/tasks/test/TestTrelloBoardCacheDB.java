package jp.ac.oit.igakilab.tasks.test;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardCacheProvider;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class TestTrelloBoardCacheDB {
	public static String BOARD_ID = "57ab33677fd33ec535cc4f28";

	public static void main(String[] args){
		MongoClient client = new MongoClient();

		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloBoardCacheProvider provider = new TrelloBoardCacheProvider(client, api);
		provider.verboseEnabled = true;

		TrelloBoard board = provider.getBoard(BOARD_ID);
		board.printContentId();

		client.close();
	}
}
