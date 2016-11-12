package jp.ac.oit.igakilab.tasks.scripts;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloBoardBuilder {
	public static long ACTION_UPDATE_TIMEOUT_MS = 30 * 1000;

	private MongoClient client;
	private boolean autoUpdate;

	public TrelloBoardBuilder(MongoClient client){
		this.client = client;
		this.autoUpdate = true;
	}

	public void setAutoUpdate(boolean b){
		autoUpdate = b;
	}

	public TrelloBoard buildTrelloBoardFromTrelloActions(String boardId){
		//ボードデータの有無をチェック
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		if( !bdb.boardIdExists(boardId) ){
			return null;
		}

		//最終更新日時チェックとアップデート
		Date boardLastUpdate = bdb.getLastUpdateDate(boardId);
		Calendar now = Calendar.getInstance();
		if(
			(now.getTimeInMillis() - boardLastUpdate.getTime())
			>= ACTION_UPDATE_TIMEOUT_MS &&
			autoUpdate
		){
			TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
			TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);
			updater.updateBoardActions(boardId, boardLastUpdate);
		}

		//ボードのビルド
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		List<TrelloAction> actions = adb.getTrelloActions(boardId, new TrelloActionDocumentParser());
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		return board;
	}
}
