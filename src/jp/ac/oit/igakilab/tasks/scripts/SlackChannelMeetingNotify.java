package jp.ac.oit.igakilab.tasks.scripts;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.hubot.HubotTaskNotify;
import jp.ac.oit.igakilab.tasks.hubot.NotifyTrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class SlackChannelMeetingNotify extends SlackChannelTaskNotify{
	public static int EXPIRE_MINUTE = 60;

	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		HubotTaskNotify msg = new HubotTaskNotify("http://150.89.234.253:8097");
		AppProperties.global.set("tasks.homeurl", "http://localhost:8080/tasks-monitor");

		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		bdb.setSlackMeetingNotifyHour("57cfb3b08c566b61d9edc3f7", 15);

		SlackChannelMeetingNotify notifer = new SlackChannelMeetingNotify(client, msg);

		System.out.println(notifer.execute());

		client.close();
	}

	private String homeUrl = null;

	public SlackChannelMeetingNotify(MongoClient client, HubotTaskNotify msg){
		super(client, msg);
//		this.client = client;
//		this.msg = msg;
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.DATE, 3);
//		this.notifyLine = cal.getTime();
//		this.header = "期限の近づいているタスクがあります";
//		init();
	}

	public SlackChannelMeetingNotify(MongoClient client, HubotTaskNotify msg, Date d){
		super(client, msg);
//		this.client = client;
//		this.msg = msg;
//		this.notifyLine = d;
//		this.header = "期限の近づいているタスクがあります";
//		init();
	}

	public void setHomeUrl(String url){
		homeUrl = url;
	}

	private boolean shouldPromote(Calendar now, int hour, int expireMinute){
		System.out.format("HOUR:%d == %d, MIN:: %d <= %d\n",
			now.get(Calendar.HOUR_OF_DAY), hour, now.get(Calendar.MINUTE), expireMinute);
		return now.get(Calendar.HOUR_OF_DAY) == hour && now.get(Calendar.MINUTE) <= expireMinute;
	}

	public boolean execute(String boardId){
		//ボードのビルド
		TrelloBoard board = buildBoard(boardId);

		//ボード名取得
		String boardName = board.getName();
		if( boardName == null ) return false;

		//カードの選択と交換
		List<NotifyTrelloCard> cards = collectNotifyCards(board);
		String dashboardUrl = homeUrl != null ? homeUrl + "/?bid=" + board.getId() : null;
		System.out.println(dashboardUrl);

		//送信
		return cmsg.promoteMeeting(boardName, cards, dashboardUrl);
	}

	public boolean execute(){
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		List<TrelloBoardsDB.Board>boards = bdb.getBoardList();
		Calendar cal = Calendar.getInstance();
		boolean res = true;

		for(TrelloBoardsDB.Board board : boards){
			Integer hour = board.getSlackMeetingNotifyHour();
			if( hour != null ){
				if( shouldPromote(cal, hour, EXPIRE_MINUTE) ){
					res = execute(board.getId()) && res;
				}
			}
		}

		return res;
	}
}
