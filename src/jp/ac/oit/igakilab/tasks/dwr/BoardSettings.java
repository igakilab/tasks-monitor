package jp.ac.oit.igakilab.tasks.dwr;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardActionsUpdater;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class BoardSettings {
	public boolean updateBoard(String boardId){
		//インスタンス初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			return false;
		}

		//アップデータの初期化
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);

		//アップデート
		updater.updateBoardActions(boardId, bdb.getLastUpdateDate(boardId));

		client.close();
		return true;
	}

	public boolean refreshBoard(String boardId){
		//インスタンス初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			return false;
		}

		//アップデータの初期化
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);

		//アップデート
		updater.clearLastUpdateDate(boardId);
		updater.updateBoardActions(boardId);

		client.close();
		return true;
	}

	public boolean deleteBoard(String boardId){
		//インスタンス初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			return false;
		}

		boolean res = bdb.removeBoard(boardId);

		client.close();
		return res;
	}
}
