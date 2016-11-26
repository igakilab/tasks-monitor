package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
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
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoardData;

public class SprintHistory {
	public SprintHistoryForms.SprintList getSprintList(String boardId){
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
		System.out.println(sl.size() + " " + sl.toString());
		System.out.println(rl.size() + " " + rl.toString());

		client.close();
		//リストを変換して返却
		return SprintHistoryForms.SprintList.getInstance(bdata, sl, rl);
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
			sprint.getId(), new SprintResultDocumentConverter());
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

		//スプリントを取得
		SprintsDB sdb = new SprintsDB(client);
		Sprint sprint = sdb.getSprintById(sprintId, new SprintDocumentConverter());
		if( sprint == null ){
			client.close();
			throw new ExecuteFailedException("スプリントが見つかりません");
		}

		//スプリントリザルトを取得
		SprintResultsDB srdb = new SprintResultsDB(client);
		SprintResult result = srdb.getSprintResultBySprintId(sprintId, new SprintResultDocumentConverter());
		if( result == null ){
			client.close();
			throw new ExecuteFailedException("スプリントリザルトが見つかりません");
		}

		//ボードを取得
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(sprint.getBoardId(), new TrelloActionDocumentParser()));
		board.build();
		if( board.getId() == null ){
			client.close();
			throw new ExecuteFailedException("ボードを生成できませんでした");
		}

		//メンバー表を作成
		List<Member> members = new ArrayList<Member>();
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(client);
		board.getMemberIds().forEach((tmid) -> {
			Member m = ttb.getMember(tmid);
			if( m != null ){
				members.add(m);
			}
		});

		//フォームに変換
		SprintResultAnalyzerForm form =
			SprintResultAnalyzerForm.getInstance(board, sprint, result, members);

		client.close();
		return form;
	}
}
