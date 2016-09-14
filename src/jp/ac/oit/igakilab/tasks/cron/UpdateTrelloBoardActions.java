package jp.ac.oit.igakilab.tasks.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver.Board;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionUpdater;
import jp.ac.oit.igakilab.tasks.trello.BoardActionFetcher;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;

public class UpdateTrelloBoardActions implements Runnable{
	private DebugLog logger = new DebugLog("cron_UpdateTrelloBoardActions");

	public static Scheduler createScheduler(String schedule){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new UpdateTrelloBoardActions());
		return scheduler;
	}

	private int updateBoardActions(String boardId, Date since){
		BoardActionFetcher fetcher = new BoardActionFetcher(
			TasksTrelloClientBuilder.createApiClient(), boardId);

		logger.log("fetch board actions");
		logger.log("boardId: " + boardId);
		logger.log("since: " + (since != null ? since.toString() : "null"));
		fetcher.fetch(since);

		JSONArray data = fetcher.getJSONArrayData();
		logger.log("-- received " + data.size() + "record(s)");

		List<Document> docs = new ArrayList<Document>();
		for(int i=data.size()-1; i>=0; i--){
			JSONObject obj = (JSONObject)data.get(i);
			docs.add(Document.parse(obj.toJSONString()));
		}

		TrelloBoardActionUpdater updater = new TrelloBoardActionUpdater(
			TasksMongoClientBuilder.createClient());
		int uc = updater.upsertDatabase(docs, boardId);
		logger.log("-- upserted " + uc + "record(s)");

		return uc;
	}

	public void run(){
		logger.log(DebugLog.LS_INFO, "CRONTASK TRIGGERED");

		MongoClient client = TasksMongoClientBuilder.createClient();
		BoardDBDriver bdb = new BoardDBDriver(client);

		for(Board board : bdb.getBoardList()){
			logger.log(DebugLog.LS_INFO, "update board : " + board.getId());
			logger.log(DebugLog.LS_INFO, "board last updated : " + board.getLastUpdate());
			Calendar cal = Calendar.getInstance();
			updateBoardActions(board.getId(), board.getLastUpdate());
			bdb.updateLastUpdateDate(board.getId(), cal.getTime());
		}

		logger.log(DebugLog.LS_INFO, "CRONTASK FINISHED");
		client.close();
	}
}
