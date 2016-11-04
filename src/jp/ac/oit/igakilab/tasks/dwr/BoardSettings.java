package jp.ac.oit.igakilab.tasks.dwr;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.BoardSettingsForms;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardActionsUpdater;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class BoardSettings {
	public BoardSettingsForms.Info getInfomation(String boardId){
		//インスタンス初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			return null;
		}

		//データ格納
		BoardSettingsForms.Info inf = new BoardSettingsForms.Info();
		inf.setLastUpdate(bdb.getLastUpdateDate(boardId));
		inf.setSlackNotifyEnabled(bdb.getSlackNotifyEnabled(boardId));

		client.close();
		return inf;
	}

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

	public boolean setSlackNotifyEnabled(String boardId, boolean enabled){
		//インスタンス初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			return false;
		}

		//処理
		boolean res = bdb.setSlackNotifyEnabled(boardId, enabled);

		//返却
		client.close();
		return res;
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
