package jp.ac.oit.igakilab.tasks.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver.Board;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionUpdater;
import jp.ac.oit.igakilab.tasks.http.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.BoardActionFetcher;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;

public class UpdateTrelloBoardActions implements Runnable{
	public static class UpdateResult{
		private String boardId;
		private int receivedCount;
		private int upsertedCount;

		public UpdateResult(String bid, int rc, int uc){
			boardId = bid;
			receivedCount = rc;
			upsertedCount = uc;
		}

		public String getBoardId(){return boardId;}
		public int getReceivedCount(){return receivedCount;}
		public int getUpsertedCount(){return upsertedCount;}
	}

	private DebugLog logger = new DebugLog("cron_UpdateTrelloBoardActions");

	public static Scheduler createScheduler(String schedule){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new UpdateTrelloBoardActions());
		return scheduler;
	}

	public UpdateResult updateBoardActions(TrelloApi api, MongoClient client, String boardId, Date since){
		//取得モジュールの初期化
		BoardActionFetcher fetcher = new BoardActionFetcher(api, boardId);
		//更新日時の記録
		Calendar cal = Calendar.getInstance();

		//データ取得
		fetcher.fetch(since);
		JSONArray records = fetcher.getJSONArrayData();

		//データ変換と並び順をリバース
		List<Document> docs = new ArrayList<Document>();
		for(int i=records.size()-1; i>=0; i--){
			JSONObject tmp = (JSONObject)records.get(i);
			docs.add(Document.parse(tmp.toJSONString()));
		}

		//データベースクライアントの初期化
		TrelloBoardActionUpdater updater = new TrelloBoardActionUpdater(client);
		//データベースを更新
		int uc = updater.upsertDatabase(docs, boardId);
		//ボードデータベースに更新日時を記録
		BoardDBDriver bdb = new BoardDBDriver(client);
		bdb.updateLastUpdateDate(boardId, cal.getTime());

		return new UpdateResult(boardId, records.size(), uc);
	}

	public UpdateResult updateBoardActions(TrelloApi api, MongoClient client, String boardId){
		return updateBoardActions(api, client, boardId, null);
	}

	public List<UpdateResult> updateAllBoardActions(TrelloApi api, MongoClient client){
		//ボードリストを取得
		BoardDBDriver bdb = new BoardDBDriver(client);
		List<Board> boards = bdb.getBoardList();

		//各ボードを更新
		List<UpdateResult> results = new ArrayList<UpdateResult>();
		for(Board board : boards){
			results.add(
				updateBoardActions(
					api, client,
					board.getId(), board.getLastUpdate()));
		}

		//結果を返却
		return results;
	}

	public void run(){
		logger.log(DebugLog.LS_INFO, "CRONTASK TRIGGERED");

		//クライアントの初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi api = TasksTrelloClientBuilder.createApiClient();

		//更新の実行
		List<UpdateResult> results = updateAllBoardActions(api, client);

		//結果の記録
		results.forEach((result ->
			logger.log(DebugLog.LS_INFO, String.format(
				"result: id:%s, received:%d, upserted:%d",
				result.getBoardId(), result.getReceivedCount(),
				result.getUpsertedCount()))
		));

		//クライアントのクローズ
		client.close();

		logger.log(DebugLog.LS_INFO, "CRONTASK FINISHED");
	}
}
