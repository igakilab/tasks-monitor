package jp.ac.oit.igakilab.tasks.cron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver.Board;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.hubot.TrelloCardDeadlineNotification;
import jp.ac.oit.igakilab.tasks.members.MemberSlackIdTable;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class HubotTasksNotification implements Runnable{
	public static Scheduler createScheduler(String schedule, String url){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new HubotTasksNotification(url));
		return scheduler;
	}

	private String hubotUrl;

	public HubotTasksNotification(String hubotUrl){
		this.hubotUrl = hubotUrl;
	}

	private boolean sendBoardNotification(List<TrelloBoard> boards, Date notifyLine,
	MemberTrelloIdTable mtable, MemberSlackIdTable stable){
		TrelloCardDeadlineNotification notifer = new TrelloCardDeadlineNotification(notifyLine, mtable);

		for(TrelloBoard board : boards){
			notifer.apply(board);
		}

		try{
			notifer.execute(hubotUrl, stable);
		}catch(IOException e0){
			e0.printStackTrace();
			return false;
		}
		return true;
	}

	private TrelloActionsBoard buildTrelloActionsBoard(TrelloBoardActionsDB adb, String boardId){
		if( adb.boardIdExists(boardId) ){
			List<TrelloAction> actions = adb.getTrelloActions(boardId, new DocumentTrelloActionParser());
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			board.build();
			return board;
		}else{
			return null;
		}
	}

	public void run(){
		//クライアントを生成
		MongoClient dbClient = TasksMongoClientBuilder.createClient();

		//ボード一覧の取得
		BoardDBDriver bdb = new BoardDBDriver(dbClient);
		List<Board> boardIds = bdb.getBoardList();

		//ボードのビルド
		List<TrelloBoard> boards = new ArrayList<TrelloBoard>();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(dbClient);
		for(Board boardId : boardIds){
			TrelloActionsBoard tmp = buildTrelloActionsBoard(adb, boardId.getId());
			if( tmp != null ){
				boards.add(tmp);
			}
		}

		//通知の送信
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 3);
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(dbClient);
		MemberSlackIdTable stable = new MemberSlackIdTable(dbClient);
		sendBoardNotification(boards, cal.getTime(), mtable, stable);

		dbClient.close();
	}
}
