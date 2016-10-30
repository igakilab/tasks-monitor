package jp.ac.oit.igakilab.tasks.dwr.admin;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardActionsUpdater;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class AdminBoardCacheController {
	public boolean refreshAll(){
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);

		boolean r1 = updater.clearAllTrelloBoardActionsCache() > 0;
		boolean r2 = updater.updateAllBoardActions().size()> 0;

		client.close();

		return (r1 && r2);
	}

	public boolean refreshBoard(String boardId){
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);

		boolean r1 = updater.clearTrelloBoardActionsCache(boardId) > 0;
		boolean r2 = updater.updateBoardActions(boardId).getReceivedCount() > 0;

		client.close();

		return (r1 && r2);
	}
}
