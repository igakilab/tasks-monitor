package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintMemberHistoryForms.AssignedCard;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.sprints.CardResult;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintMemberHistory {
	/**
	 * sprintResultデータベースから指定されたメンバーidが担当するカード一覧を取得します
	 * @param memberId
	 * @return
	 */
	public List<AssignedCard> getTaskCardsByMemberId(String memberId)
	throws ExecuteFailedException{
		//クライアントの初期化
		MongoClient client = TasksMongoClientBuilder.createClient();

		//スプリントリザルト一覧を取得する
		SprintsDB sdb = new SprintsDB(client);
		SprintResultsDB srdb = new SprintResultsDB(client);
		List<SprintResult> resultHistory =
			srdb.getSprintResultsByCardMemberId(memberId, new SprintResultDocumentConverter());

		//assignedCardを生成する
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		List<TrelloBoard> trelloBoardCache = new ArrayList<TrelloBoard>();
		List<AssignedCard> cards = new ArrayList<AssignedCard>();
		for(SprintResult res : resultHistory){
			//ボードidを取得する
			String boardId = sdb.getBoardIdBySprintId(res.getSprintId());
			//対象ボードを取得する
			TrelloBoard board = null;
			//キャッシュから探す
			for(TrelloBoard btmp : trelloBoardCache){
				if( btmp.getId() == boardId ){
					board = btmp;
				}
			}
			//見つからなかった場合はデータベースから取得する
			if( board == null ){
				board = builder.buildTrelloBoardFromTrelloActions(boardId);
				if( board == null ){
					client.close();
					throw new ExecuteFailedException("ボード取得中にエラーが発生しました");
				}
				trelloBoardCache.add(board);
			}

			//カードをビルドして返却リストに追加する
			for(CardResult cres : res.getCardsByMemberIdContains(memberId)){
				TrelloCard c = board.getCardById(cres.getCardId());
				if( c != null ){
					cards.add(AssignedCard.getInstance(
						c, res.getSprintId(), cres.getMemberIds(), cres.isFinished()));
				}
			}
		}

		//結果を返却
		client.close();
		return cards;
	}
}
