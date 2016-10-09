package jp.ac.oit.igakilab.tasks.hubot;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.hubot.TrelloCardNotifyList.NotifyCard;
import jp.ac.oit.igakilab.tasks.members.MemberSlackIdTable;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloCardDeadlineNotification {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(client);
		MemberSlackIdTable stable = new MemberSlackIdTable(client);
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		List<TrelloAction> actions = adb.getTrelloActions(
			"57ab33677fd33ec535cc4f28", new TrelloActionDocumentParser());
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 3);

		TrelloCardDeadlineNotification notif = new TrelloCardDeadlineNotification(cal.getTime(), mtable);
		notif.apply(board);
		try{
			notif.execute("http://igakilabot.herokuapp.com", stable);
		}catch(IOException e0){
			e0.printStackTrace();
		}

		client.close();
	}

	private Date notifyLine;
	private MemberTrelloIdTable mtable;
	private TrelloCardNotifyList notifyList;

	public TrelloCardDeadlineNotification(Date nline, MemberTrelloIdTable mtable){
		this.notifyLine = nline;
		this.mtable = mtable;
		this.notifyList = new TrelloCardNotifyList();
	}

	private List<TrelloCard> getNotifyTargetCards(TrelloBoard board){
		List<TrelloCard> cards = new ArrayList<TrelloCard>();

		//tmpに対象リストのカードを登録する
		List<TrelloCard> tmp = new ArrayList<TrelloCard>();
		tmp.addAll(board.getCardsByListNameMatches("(?i)to\\s*do"));
		tmp.addAll(board.getCardsByListNameMatches("(?i)doing"));

		//closeされておらず、期限の近いカードを選択し、cardsに登録する
		tmp.forEach((card) -> {
			if( !card.isClosed()
				&& (card.getDue() != null)
				&& (notifyLine.compareTo(card.getDue()) >= 0 )
			){
				cards.add(card);
			}
		});

		return cards;
	}

	private Map<String,List<TrelloCard>> assortMemberCardsMap(List<TrelloCard> cards){
		Map<String,List<TrelloCard>> map = new HashMap<String,List<TrelloCard>>();

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

	public int apply(TrelloBoard board){
		int addedCount = 0;

		//カードを選出する
		List<TrelloCard> targets = getNotifyTargetCards(board);

		//カードをメンバーにマッピングする
		Map<String,List<TrelloCard>> mcmap = assortMemberCardsMap(targets);

		//NotifyListに追加する
		for(Entry<String,List<TrelloCard>> entry : mcmap.entrySet()){
			String memberId = entry.getKey();
			for(TrelloCard card : entry.getValue()){
				boolean res = notifyList.add(memberId, card,
					board.getListById(card.getListId()), board);
				if( res ) addedCount++;
			}
		}

		return addedCount;
	}

	public TrelloCardNotifyList getNotifyList(){
		return notifyList;
	}

	private String buildNotifyMessage(String memberId){
		StringBuffer buffer = new StringBuffer();
		DateFormat df = new SimpleDateFormat("M/d");

		buffer.append("期限が近づいているタスクがあります");

		List<NotifyCard> cards = notifyList.filterByMemberId(memberId).list();
		for(NotifyCard card : cards){
			buffer.append(String.format("\n> [%s:%s] %s (%sまで)",
				card.getBoard().getName(),
				card.getList().getName(),
				card.getCard().getName(),
				df.format(card.getCard().getDue())));
		}

		return buffer.toString();
	}

	public void execute(String hubotUrl, MemberSlackIdTable stable)
	throws IOException{
		//hubotメッセンジャーの初期化
		HubotSendMessage msg = new HubotSendMessage(hubotUrl);

		//対象メンバーをリストアップ
		List<String> members = notifyList.getNotifyMemberList();

		//メンバーごとに送信
		for(String memberId : members){
			String slackId = stable.getSlackId(memberId);
			if( slackId != null ){
				String message = buildNotifyMessage(memberId);
				msg.send(slackId, message);
			}
		}
	}
}
