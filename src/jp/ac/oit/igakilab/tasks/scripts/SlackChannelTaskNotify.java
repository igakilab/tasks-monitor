package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.hubot.ChannelNotification;
import jp.ac.oit.igakilab.tasks.hubot.HubotTaskNotify;
import jp.ac.oit.igakilab.tasks.hubot.NotifyTrelloCard;
import jp.ac.oit.igakilab.tasks.members.MemberSlackIdTable;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class SlackChannelTaskNotify {
	public static void main(String[] args){
		//String boardId = "57ab33677fd33ec535cc4f28";
		MongoClient client = TasksMongoClientBuilder.createClient();
		HubotTaskNotify msg = new HubotTaskNotify("http://igakilabot.herokuapp.com");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 7);
		SlackChannelTaskNotify notifer = new SlackChannelTaskNotify(client, msg);
		notifer.setNotifyLine(cal.getTime());

		//TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		//bdb.setSlackNotifyEnabled(boardId, true);

		System.out.println(notifer.execute());

		client.close();
	}

	protected MongoClient client;
	private HubotTaskNotify msg;
	private Date notifyLine;
	private String header;

	private TrelloBoardActionsDB adb;
	private DocumentParser<TrelloAction> parser;
	private MemberSlackIdTable stable;
	private MemberTrelloIdTable ttable;
	protected ChannelNotification cmsg;

	public SlackChannelTaskNotify(MongoClient client, HubotTaskNotify msg){
		this.client = client;
		this.msg = msg;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 3);
		this.notifyLine = cal.getTime();
		this.header = "期限の近づいているタスクがあります";
		init();
	}

	public SlackChannelTaskNotify(MongoClient client, HubotTaskNotify msg, Date d){
		this.client = client;
		this.msg = msg;
		this.notifyLine = d;
		this.header = "期限の近づいているタスクがあります";
		init();
	}

	public void init(){
		adb = new TrelloBoardActionsDB(client);
		parser = new TrelloActionDocumentParser();
		stable = new MemberSlackIdTable(client);
		ttable = new MemberTrelloIdTable(client);
		cmsg = new ChannelNotification(msg);
		cmsg.setSlackIdTable(stable);
		//cmsg.setTestMode(true);
	}

	public void setNotifyLine(Date d){
		notifyLine = d;
	}

	public void setHeader(String header){
		this.header = header;
	}

	protected TrelloBoard buildBoard(String boardId){
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(boardId, parser));
		board.build();
		return board;
	}

	private boolean needsNotify(TrelloCard card){
		return card.getDue() != null && !card.isClosed() && (
			(notifyLine == null || notifyLine.compareTo(card.getDue()) >= 0) );
	}

	protected List<NotifyTrelloCard> collectNotifyCards(TrelloBoard board){
		List<NotifyTrelloCard> cards = new ArrayList<NotifyTrelloCard>();
		Consumer<TrelloCard> collector = (c) -> {
			if( needsNotify(c) ){
				cards.add(NotifyTrelloCard.getInstance(c, board, ttable));
			}
		};

		board.getCardsByListNameMatches(TasksTrelloClientBuilder.REGEX_TODO).forEach(collector);
		board.getCardsByListNameMatches(TasksTrelloClientBuilder.REGEX_DOING).forEach(collector);

		return cards;
	}


	public boolean execute(String boardId){
		//ボードのビルド
		TrelloBoard board = buildBoard(boardId);

		//ボード名取得
		String boardName = board.getName();
		if( boardName == null ) return false;

		//カードの選択と変換
		List<NotifyTrelloCard> cards = collectNotifyCards(board);

		//送信
		if( cards.size() > 0 ){
			return cmsg.taskNotification(boardName, header, cards);
		}else{
			return true;
		}
	}

	public boolean execute(){
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		List<TrelloBoardsDB.Board> boards =  bdb.getBoardList();
		boolean res = true;

		for(TrelloBoardsDB.Board board : boards){
			if( board.getSlackNotifyEnabled() ){
				res = execute(board.getId()) && res ? true : false;
			}else{
				System.out.println(board.getId() + "is notify disabled");
			}
		}

		return res;
	}
}
