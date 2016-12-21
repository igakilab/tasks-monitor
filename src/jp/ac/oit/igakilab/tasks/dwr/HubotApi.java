package jp.ac.oit.igakilab.tasks.dwr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.HubotApiForms.CurrentSprint;
import jp.ac.oit.igakilab.tasks.dwr.forms.HubotApiForms.NotpSprintForm;
import jp.ac.oit.igakilab.tasks.members.MemberSlackIdTable;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.scripts.SprintEditException;
import jp.ac.oit.igakilab.tasks.scripts.SprintEditor;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.sprints.CardMembers;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class HubotApi {
	/**
	 * 現在進行中のスプリントを取得します
	 * 返却されるjsonオブジェクトのsprintの値にデータが渡され、
	 * スプリントがない場合sprintの値にnullがセットされます。
	 * @param boardId TrelloのボードID
	 * @return CurrentSprint(json)
	 * @throws ExecuteFailedException
	 */
	public CurrentSprint getCurrentSprint(String boardId)
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();

		//ボードを検索
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			throw new ExecuteFailedException("ボードが見つかりません");
		}

		//ボードを取得
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		TrelloBoard board = builder.buildTrelloActionsBoardFromTrelloActions(boardId);

		//現在進行中のスプリントを取得
		SprintsManageDB smdb = new SprintsManageDB(client);
		Sprint spr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		//フォームを生成
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(client);
		CurrentSprint form = CurrentSprint.getInstance(spr, board, ttb);

		client.close();
		return form;
	}

	/**
	 * 該当ボードの進行中スプリントにカードを追加します
	 * @param boardId TrelloボードID
	 * @param cardId カードのID
	 * @param slackId 担当者となる人のID
	 * @return
	 * @throws ExecuteFailedException
	 */
	public Map<String,Object> addSprintCard(String boardId, String cardId, String slackId)
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();

		//スプリントを取得
		SprintsManageDB smdb = new SprintsManageDB(client);
		Sprint spr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( spr == null ){
			client.close();
			throw new ExecuteFailedException("現在進行中のスプリントはありません");
		}

		//担当者情報取得
		MemberSlackIdTable stb = new MemberSlackIdTable(client);
		String memberId = stb.getMemberId(slackId);

		//カード生成
		CardMembers cm = new CardMembers();
		cm.setCardId(cardId);
		if( memberId != null ) cm.addMemberId(memberId);

		//カード追加処理
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintEditor editor = new SprintEditor(client, api, null);
		try{
			editor.addSprintCards(spr.getId(), Arrays.asList(cm));
		}catch(SprintEditException e0){
			client.close();
			throw new ExecuteFailedException("カード追加処理中にエラーが発生しました: " + e0.getMessage());
		}

		//結果を生成
		Map<String,Object> reply = new HashMap<String,Object>();
		reply.put("success", true);
		Sprint before = smdb.getSprintById(spr.getId(), new SprintDocumentConverter());
		reply.put("sprint", NotpSprintForm.getInstance(before));

		client.close();
		return reply;
	}
}
