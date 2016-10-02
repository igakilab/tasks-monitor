package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDBUpdater;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB.Board;
import jp.ac.oit.igakilab.tasks.trello.BoardActionFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TrelloBoardActionsUpdater {
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

	private MongoClient dbClient;
	private TrelloApi<Object> trelloApi;

	public TrelloBoardActionsUpdater(MongoClient dbc, TrelloApi<Object> tapi){
		this.dbClient = dbc;
		this.trelloApi = tapi;
	}

	public UpdateResult updateBoardActions(String boardId, Date since){
		//取得モジュールの初期化
		BoardActionFetcher fetcher = new BoardActionFetcher(trelloApi, boardId);
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
		TrelloBoardActionsDBUpdater updater = new TrelloBoardActionsDBUpdater(dbClient);
		//データベースを更新
		int uc = updater.upsertDatabase(docs, boardId);
		//ボードデータベースに更新日時を記録
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		bdb.updateLastUpdateDate(boardId, cal.getTime());

		return new UpdateResult(boardId, records.size(), uc);
	}

	public UpdateResult updateBoardActions(String boardId){
		return updateBoardActions( boardId, null);
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
