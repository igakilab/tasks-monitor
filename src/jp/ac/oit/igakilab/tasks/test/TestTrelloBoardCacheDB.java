package jp.ac.oit.igakilab.tasks.test;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloBoardCacheDB;
import jp.ac.oit.igakilab.tasks.db.converters.JsonDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloBoardCacheBuilder;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class TestTrelloBoardCacheDB {
	public static String BOARD_ID = "57ab33677fd33ec535cc4f28";

	public static void main(String[] args){
		MongoClient client = new MongoClient();

		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloBoardCacheDB db = new TrelloBoardCacheDB(client);

		Date now = Calendar.getInstance().getTime();
		JSONObject obj = TrelloBoardFetcher.sendFetchRequest(api, BOARD_ID);

		System.out.println("LAST UPDATE: " + db.getLastUpdateDate(BOARD_ID));

		db.updateBoardData(BOARD_ID, now, obj, new JsonDocumentConverter());

		TrelloBoard board = db.findBoardData(BOARD_ID, new TrelloBoardCacheBuilder());
		board.printContentId();

		client.close();
	}
}
