package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintHistoryForms;
import jp.ac.oit.igakilab.tasks.dwr.forms.jsmodule.SprintResultAnalyzerForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintDataContainer;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultProvider;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoardData;


public class SprintHistory {
	public SprintHistoryForms.SprintList getSprintList(String boardId){
		//各種クライアントを初期化
		MongoClient client = TasksMongoClientBuilder.createClient();

		//ボード情報を取得
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(boardId, new TrelloActionDocumentParser()));
		TrelloBoardData bdata = board.buildBoardData();

		//マネージャを初期化
		SprintResultProvider provider = new SprintResultProvider(client);

		//リストを取得
		List<SprintDataContainer> list = provider.getSprintResultsByBoardId(boardId);

		client.close();
		//リストを変換して返却
		return SprintHistoryForms.SprintList.getInstanceByDataContainer(bdata, list);
	}

	public SprintHistoryForms.SprintResultData getSprintResult(String sprintId)
	throws ExecuteFailedException{
		//各種クライアントを初期化
		MongoClient client = TasksMongoClientBuilder.createClient();

		//dbインスタンスを初期化
		SprintsDB sdb = new SprintsDB(client);
		SprintResultsDB srdb = new SprintResultsDB(client);

		//スプリントを取得
		Sprint sprint = sdb.getSprintById(sprintId, new SprintDocumentConverter());
		if( sprint == null ){
			client.close();
			throw new ExecuteFailedException("スプリントがみつかりません");
		}

		//スプリントリザルトを取得
		SprintResult result = srdb.getSprintResultBySprintId(
			sprint.getId(), new SprintResultDocumentConverter(srdb));
		if( result == null ){
			client.close();
			throw new ExecuteFailedException("スプリントリザルトが見つかりません");
		}

		//ボード情報を取得
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(sprint.getBoardId(), new TrelloActionDocumentParser()));
		board.build();

		client.close();
		//リストを変換して返却
		return SprintHistoryForms.SprintResultData.getInstance(sprint, result, board);
	}

	public SprintResultAnalyzerForm getSprintResultAnalyzerData(String sprintId)
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();

		SprintResultAnalyzerForm form = null;
		try{
			form = SprintResultAnalyzerForm.buildInstance(client, sprintId);
		}catch(ExecuteFailedException e0){
			client.close();
			throw e0;
		}

		client.close();
		return form;
	}
}
