package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintMemberHistoryForms.AssignedCard;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintMemberHistoryForms.MemberTasksResult;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintMemberHistoryForms.MemberTasksWrapper;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloBoardDataForm;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.sprints.CardTagsAggregator;
import jp.ac.oit.igakilab.tasks.sprints.SprintDataContainer;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultCard;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultProvider;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionRawDataParser;

public class SprintMemberHistory {
	/**
	 * sprintResultデータベースから指定されたメンバーidが担当するカード一覧を取得します
	 * @param memberId
	 * @return
	 */
	public MemberTasksResult getTaskCardsByMemberId(String memberId)
	throws ExecuteFailedException{
		//クライアントの初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		MemberTasksResult result = new MemberTasksResult();

		//スプリントリザルト一覧を取得する
		SprintResultProvider provider = new SprintResultProvider(client);
		List<SprintDataContainer> sprints  =
			provider.getSprintResultsByCardMemberId(memberId);

		//wrapperを生成する
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		List<TrelloBoard> boardCache = new ArrayList<TrelloBoard>();
		TrelloActionRawDataParser parser = new TrelloActionRawDataParser();
		CardTagsAggregator tagagr = new CardTagsAggregator();
		for(SprintDataContainer container : sprints){
			//System.out.println("\t>>オブジェクト生成 " + container.getSprintId());
			//フォームを生成する
			MemberTasksWrapper wrapper = new MemberTasksWrapper();

			//ボードデータを検索する
			String boardId = container.getSprint().getBoardId();
			TrelloBoard board = null;
			for(TrelloBoard btmp : boardCache){
				if( btmp.getId().equals(boardId) ){
					board = btmp; break;
				}
			}
			if( board == null ){
				board = builder.buildTrelloBoardFromTrelloActions(boardId);
				boardCache.add(board);
				if( board == null ){
					client.close();
					throw new ExecuteFailedException("ボード取得中にエラーが発生しました");
				}
			}

			//スプリントデータを設定する
			wrapper.setSprint(SprintForm.getInstance(container.getSprint()));
			//ボードデータを設定する
			wrapper.setBoard(TrelloBoardDataForm.getInstance(board));

			//カード情報を設定する
			List<SprintResultCard> cards =
				container.getSprintResult().getCardsByMemberIdContains(memberId);
			for(SprintResultCard card : cards){
				wrapper.addCard(AssignedCard.getInstance(
					card.getTrelloActionsCard(parser),
					container.getSprintId(),
					card.getMemberIds(),
					card.isFinished()));
				tagagr.apply(card);
			}

			//リストに追加する
			result.addTasksWrapper(wrapper);
		}

		//wrapperの内容を並び替える
		result.getSprints().sort((o1, o2) ->
			o2.getSprint().getClosedDate().compareTo(o1.getSprint().getClosedDate()));

		//タグ情報をセットする
		result.setTagCounts(tagagr.getTagCounts());

		//結果を返却
		client.close();
		return result;
	}
}
