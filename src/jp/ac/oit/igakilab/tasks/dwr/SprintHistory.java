package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintHistoryForms;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintHistoryForms.SprintList;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardData;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;

public class SprintHistory {
	public SprintList getSprintList(String boardId){
		//各種クライアントを初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		//ボード情報を取得
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(boardId, new TrelloActionDocumentParser()));
		TrelloBoardData bdata = board.buildBoardData();

		//マネージャを初期化
		SprintManager sm = new SprintManager(client, api);

		//リストを取得
		List<Sprint> sl = sm.getSprintsByBoardId(boardId);
		List<SprintResult> rl = sm.getSprintResultsByBoardId(boardId);

		client.close();
		//リストを変換して返却
		return SprintHistoryForms.SprintList.getInstance(bdata, sl, rl);
	}
}
