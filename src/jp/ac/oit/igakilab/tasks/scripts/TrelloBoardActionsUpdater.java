package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDBUpdater;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB.Board;
import jp.ac.oit.igakilab.tasks.trello.BoardActionFetcher;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TrelloBoardActionsUpdater {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);

		updater.setPrintResult(true);
		updater.clearAllTrelloBoardActionsCache();
		updater.updateAllBoardActions();

		client.close();
	}

	public static class UpdateResult{
		private String boardId;
		private Date since;
		private int receivedCount;
		private int upsertedCount;

		public UpdateResult(String bid, Date sd, int rc, int uc){
			boardId = bid;
			since = sd;
			receivedCount = rc;
			upsertedCount = uc;
		}

		public String getBoardId(){return boardId;}
		public Date getSince(){return since;}
		public int getReceivedCount(){return receivedCount;}
		public int getUpsertedCount(){return upsertedCount;}
	}

	private MongoClient dbClient;
	private TrelloApi<Object> trelloApi;
	private boolean printResult;

	public TrelloBoardActionsUpdater(MongoClient dbc, TrelloApi<Object> tapi){
		this.dbClient = dbc;
		this.trelloApi = tapi;
		this.printResult = false;
	}

	public void setPrintResult(boolean b0){
		printResult = b0;
	}

	public boolean clearLastUpdateDate(String boardId){
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		return bdb.clearLastUpdateDate(boardId);
	}

	public int clearAllLastUpdateDate(){
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		return bdb.clearAllLastUpdateDate();
	}

	public int clearTrelloBoardActionsCache(String boardId){
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(dbClient);
		int removed = adb.removeTrelloActions(boardId);
		clearLastUpdateDate(boardId);
		if( printResult ){
			System.out.println("[BOARD ACTIONS CACHE CLEAR]");
			System.out.println("\ttarget: " + boardId);
			System.out.println("\tremoved record(s): " + removed);
		}
		return removed;
	}

	public int clearAllTrelloBoardActionsCache(){
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(dbClient);
		int removed = adb.removeAllTrelloActions();
		clearAllLastUpdateDate();
		if( printResult ){
			System.out.println("[BOARD ACTIONS CACHE CLEAR]");
			System.out.println("\ttarget: <ALL BOARD DATA>");
			System.out.println("\tremoved record(s): " + removed);
		}
		return removed;
	}

	public UpdateResult updateBoardActions(String boardId, Date since){
		//取得モジュールの初期化
		BoardActionFetcher fetcher = new BoardActionFetcher(trelloApi, boardId);
		//更新日時の記録
		Calendar cal = Calendar.getInstance();

		//データ取得
		if( printResult ){
			System.out.println("fetching... (" + boardId + ")");
		}
		fetcher.fetch(since);
		JSONArray records = fetcher.getJSONArrayData();

		//データ変換と並び順をリバース
		List<Document> docs = new ArrayList<Document>();
		for(int i=records.size()-1; i>=0; i--){
			JSONObject tmp = (JSONObject)records.get(i);
			docs.add(Document.parse(tmp.toJSONString()));
		}

		//データベースクライアントの初期化
		TrelloBoardActionsDBUpdater updater = new TrelloBoardActionsDBUpdater(dbClient);
		//データベースを更新
		int uc = updater.upsertDatabase(docs, boardId);
		//ボードデータベースに更新日時を記録
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		bdb.updateLastUpdateDate(boardId, cal.getTime());

		//Resultの書き出し
		UpdateResult result = new UpdateResult(boardId, since, records.size(), uc);
		if( printResult ){
			System.out.println("[BOARD ACTIONS UPDATE]");
			System.out.format("\ttarget: %s\n\tsince: %s\n", result.getBoardId(), result.getSince());
			System.out.println("\treceived record(s): " + result.getReceivedCount());
			System.out.println("\tupserted record(s): " + result.getUpsertedCount());
		}

		return result;
	}

	public UpdateResult updateBoardActions(String boardId){
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		Date lastUpdateDate = bdb.getLastUpdateDate(boardId);
		if( lastUpdateDate == null ) return new UpdateResult(boardId, null, 0, 0);

		return updateBoardActions(boardId, lastUpdateDate);
	}

	public List<UpdateResult> updateAllBoardActions(){
		//ボードリストを取得
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		List<Board> boards = bdb.getBoardList();

		//各ボードを更新
		List<UpdateResult> results = new ArrayList<UpdateResult>();
		for(Board board : boards){
			results.add(
				updateBoardActions(board.getId(), board.getLastUpdate()));
		}

		//結果を返却
		return results;
	}
}
