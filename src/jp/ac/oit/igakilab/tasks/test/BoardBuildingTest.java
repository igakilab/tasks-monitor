package jp.ac.oit.igakilab.tasks.test;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDBUpdater;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.trello.BoardActionFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.SimpleJsonResponseTextParser;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class BoardBuildingTest {
	public static String DB_HOST = "localhost";
	public static String TRELLO_KEY = "67ad72d3feb45f7a0a0b3c8e1467ac0b";
	public static String TRELLO_TOKEN = "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65";

	public static void main(String[] args){
		int piv = (int)(Math.random() * 2);
		String boardId = "57ab33677fd33ec535cc4f28";

		for(int i=0; i<2; i++){
			if( (i + piv) % 2 == 0 ){
				long s = System.currentTimeMillis();
				buildByTrelloAction(boardId);
				long e = System.currentTimeMillis();
				System.out.println("actions: " + (e - s) + "ms");
			}else{
				long s = System.currentTimeMillis();
				buildByDirect(boardId);
				long e = System.currentTimeMillis();
				System.out.println("direct:  " + (e - s) + "ms");
			}
		}
	}


	public static TrelloBoard buildByTrelloAction(String boardId){
		MongoClient client = new MongoClient(DB_HOST);
		TrelloApi<Object> api = new TrelloApi<Object>(
			TRELLO_KEY, TRELLO_TOKEN, new SimpleJsonResponseTextParser());

		//期限の取得
		//TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		//Date d = bdb.getLastUpdateDate(boardId);

		//アクションのフェッチ
		BoardActionFetcher fetcher = new BoardActionFetcher(api, boardId);
		fetcher.fetch();
		JSONArray actions = fetcher.getJSONArrayData();
		List<Document> docs = new ArrayList<Document>();
		for(Object obj : actions){
			JSONObject tmp = (JSONObject)obj;
			docs.add(Document.parse(tmp.toJSONString()));
		}
		TrelloBoardActionsDBUpdater updater = new TrelloBoardActionsDBUpdater(client);
		updater.upsertDatabase(docs, boardId);

		//アクションの取得
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		List<TrelloAction> dbacts =
			adb.getTrelloActions(boardId, new TrelloActionDocumentParser());

		//ボードを構成
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(dbacts);
		board.build();

		client.close();
		return board;
	}

	public static TrelloBoard buildByDirect(String boardId){
		TrelloApi<Object> api = new TrelloApi<Object>(
			TRELLO_KEY, TRELLO_TOKEN, new SimpleJsonResponseTextParser());

		//データを取得
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api, boardId);
		fetcher.fetch();

		//ボードを構成
		TrelloBoard board = fetcher.getBoard();

		return board;
	}
}
