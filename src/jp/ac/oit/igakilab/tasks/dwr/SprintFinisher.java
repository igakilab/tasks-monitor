package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintFinisherForms.ClosedSprintResult;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintFinisherForms.MemberCards;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintFinisherForms.SprintResultCardTagsForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.sprints.CardResult;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultProvider;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardFetcher;
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
		List<String> finished = new ArrayList<String>();

		for(CardResult scard : res.getAllCardResults()){
			for(String mid : scard.getMemberIds()){
				addCardToMemberCardsList(
					memberTasks, mid, scard.getCardId(), scard.isFinished());
			}
			if( scard.isFinished() ){
				finished.add(scard.getCardId());
			}else{
				remained.add(scard.getCardId());
			}
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
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在進行中のスプリントを取得
		Sprint currSpr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( currSpr == null ){
			throw new ExecuteFailedException("現在進行中のスプリントはありません");
		}

		//クローズ処理
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardFetcher bf = new TrelloBoardFetcher(api, boardId);
		TrelloCardFetcher cf = new TrelloCardFetcher(api);
		SprintManager manager = new SprintManager(client);
		SprintResult res = null;
		bf.fetch();
		if( manager.closeSprint(bf.getBoard(), cf, currSpr.getId()) ){
			SprintResultProvider provider = new SprintResultProvider(client);
			res = provider.getSprintResultBySprintId(currSpr.getId());
		}

		if( res == null ){
			throw new ExecuteFailedException("スプリントのクローズ処理が失敗しました");
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
	 * スプリントIDとカードIDで指定されたカードにタグを設定します。
	 * 配列で指定されたタグ列はデータベース上のデータを上書きします。
	 * (既に設定されているタグは破棄されます)
	 */
	public boolean setSprintCardTags(String sprintId, String cardId, List<String> tags){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintResultsDB srdb = new SprintResultsDB(client);

		boolean res = srdb.setTagsToSprintCard(sprintId, cardId, tags);

		client.close();
		return res;
	}

	/*
	 * 上記メソッドの複数セットできるバージョンです
	 */
	public boolean setSprintCardsTags(String sprintId, List<SprintResultCardTagsForm> cts){
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		SprintsDB sdb = new SprintsDB(client);
		SprintResultsDB srdb = new SprintResultsDB(client);

		String boardId = sdb.getSprintById(sprintId, new SprintDocumentConverter()).getBoardId();
		List<String> defaultTags = bdb.getDefaultTags(boardId);

		List<String> newTags = new ArrayList<String>();
		boolean res = false;
		if( srdb.sprintIdExists(sprintId) ){
			res = true;
			for(SprintResultCardTagsForm ct : cts){
				res = srdb.setTagsToSprintCard(sprintId, ct.getCardId(), ct.getTags()) && res;
				ct.getTags().forEach((tag) -> {
					if( !defaultTags.contains(tag) && !newTags.contains(tag) ){
						newTags.add(tag);
					}
				});
			}
		}

		bdb.addDefaultTags(boardId, newTags);

		client.close();
		return res;
	}
}
