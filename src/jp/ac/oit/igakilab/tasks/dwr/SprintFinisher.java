package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintFinisherForms.ClosedSprintResult;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintFinisherForms.MemberCards;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintResultForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class SprintFinisher {
	private void addCardToMemberCardsList(List<MemberCards> mcs, String mid, String cardId, boolean remained){
		for(MemberCards mc : mcs){
			if( mc.getMemberId().equals(mid) ){
				if( remained ){
					mc.addReaminedCard(cardId);
				}else{
					mc.addFinishedCards(cardId);
				}
				return;
			}
		}
		MemberCards mc = new MemberCards(mid);
		if( remained ){
			mc.addReaminedCard(cardId);
		}else{
			mc.addFinishedCards(cardId);
		}
		mcs.add(mc);
	}

	private ClosedSprintResult getClosedSprintResult(SprintResult res, TrelloBoard board){
		ClosedSprintResult result = new ClosedSprintResult();
		result.setSprintId(res.getSprintId());
		result.setCreatedAt(res.getCreatedAt());

		List<MemberCards> memberTasks = new ArrayList<MemberCards>();
		List<String> remained = new ArrayList<String>();
		for(TrelloCardMembers tcm : res.getRemainedCards()){
			for(String mid : tcm.getMemberIds()){
				addCardToMemberCardsList(memberTasks, mid, tcm.getCardId(), true);
			}
			remained.add(tcm.getCardId());
		}

		List<String> finished = new ArrayList<String>();
		for(TrelloCardMembers tcm : res.getFinishedCards()){
			for(String mid : tcm.getMemberIds()){
				addCardToMemberCardsList(memberTasks, mid, tcm.getCardId(), false);
			}
			finished.add(tcm.getCardId());
		}

		result.setMemberTasks(memberTasks);
		result.setRemainedCards(remained);
		result.setFinishedCards(finished);

		List<TrelloCardForm> cards = new ArrayList<TrelloCardForm>();
		res.getAllCards().forEach((ttcm) ->
			cards.add(TrelloCardForm.getInstance(
				board.getCardById(ttcm.getCardId()))));
		result.setSprintCards(cards);

		return result;
	}

	/*
	 * ボードIDで指定されたボードで、現在進行中のスプリントをクローズします。
	 * スプリントのクローズに成功したら、SprintResultFormのオブジェクトを返却します。
	 */
	public ClosedSprintResult closeCurrentSprint(String boardId)
	throws ExcuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在進行中のスプリントを取得
		Sprint currSpr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( currSpr == null ){
			throw new ExcuteFailedException("現在進行中のスプリントはありません");
		}

		//クローズ処理
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintManager manager = new SprintManager(client, api);
		SprintResult res = manager.closeSprint(currSpr.getId());

		if( res == null ){
			throw new ExcuteFailedException("スプリントのクローズ処理が失敗しました");
		}

		//リザルトの取得
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(boardId, new TrelloActionDocumentParser()));
		board.build();

		ClosedSprintResult cres = getClosedSprintResult(res, board);

		client.close();
		return cres;
	}

	/*
	 * ボードIDで指定されたボードの、SprintResultを一覧取得します。
	 */
	public List<SprintResultForm> getSprintResultsByBoardId(String boardId){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintManager manager = new SprintManager(client, null);

		//返却する配列を初期化
		List<SprintResultForm> forms = new ArrayList<SprintResultForm>();

		//結果を取得、変換して返却配列に格納
		manager.getSprintResultsByBoardId(boardId).forEach((result ->
			forms.add(SprintResultForm.getInstance(result))));

		//結果を返却
		client.close();
		return forms;
	}
}