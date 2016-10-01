package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloCardNotification {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();

		TrelloCardNotification notif = new TrelloCardNotification(client);
		notif.executeCloseToDeadlineCardsNotify("57ab33677fd33ec535cc4f28");

		client.close();
	}

	private MongoClient dbClient;

	public TrelloCardNotification(MongoClient dbc){
		this.dbClient = dbc;
	}

	private List<TrelloCard> getTodoDoingCardsByBoardId(String boardId){
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(dbClient);
		List<TrelloCard> cards = new ArrayList<TrelloCard>();

		if( adb.boardIdExists(boardId) ){
			List<TrelloAction> actions =
				adb.getTrelloActions(boardId, new DocumentTrelloActionParser());
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			board.build();

			board.getCardsByListNameMatches("(?i)to\\s*do").forEach(
				(card -> cards.add(card)));
			board.getCardsByListNameMatches("(?i)doing").forEach(
				(card -> cards.add(card)));
		}

		return cards;
	}

	private Map<String,List<TrelloCard>> assortMemberCardsMap(List<TrelloCard> cards){
		Map<String,List<TrelloCard>> map = new HashMap<String,List<TrelloCard>>();
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(dbClient);

		for(TrelloCard card : cards){
			Set<String> members = card.getMemberIds();
			for(String mtid : members){
				String mid = mtable.getMemberId(mtid);
				if( mid != null ){
					if( !map.containsKey(mid) ){
						map.put(mid, new ArrayList<TrelloCard>());
					}
					map.get(mid).add(card);
				}
			}
		}

		return map;
	}

	public void executeCloseToDeadlineCardsNotify(String boardId){
		//カードの取得
		List<TrelloCard> cards = getTodoDoingCardsByBoardId(boardId);

		//デッドラインに近いカードを選ぶ
		Calendar line = Calendar.getInstance();
		line.add(Calendar.DATE, 3);

		List<TrelloCard> cdCards = new ArrayList<TrelloCard>();
		cards.forEach((card) -> {
			Date due = card.getDue();
			if( due != null ){
				if( line.getTime().compareTo(due) >= 0 ){
					cdCards.add(card);
				}
			}
		});

		//マッピングする
		Map<String,List<TrelloCard>> mcmap = assortMemberCardsMap(cdCards);
		for(Entry<String,List<TrelloCard>> e : mcmap.entrySet()){
			System.out.println(e.getKey() + ": " + e.getValue());
		}
	}
}
