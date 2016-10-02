package jp.ac.oit.igakilab.tasks.cron;

import java.util.List;

import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardActionsUpdater;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardActionsUpdater.UpdateResult;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class UpdateTrelloBoardActions implements Runnable{
	private DebugLog logger = new DebugLog("cron_UpdateTrelloBoardActions");

	public static Scheduler createScheduler(String schedule){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new UpdateTrelloBoardActions());
		return scheduler;
	}

	public void run(){
		logger.log(DebugLog.LS_INFO, "CRONTASK TRIGGERED");

		//クライアントの初期化
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);

		//更新の実行
		List<UpdateResult> results = updater.updateAllBoardActions();

		//結果の記録
		results.forEach((result ->
			logger.log(DebugLog.LS_INFO, String.format(
				"result: id:%s, received:%d, upserted:%d",
				result.getBoardId(), result.getReceivedCount(),
				result.getUpsertedCount()))
		));

		//クライアントのクローズ
		client.close();

		logger.log(DebugLog.LS_INFO, "CRONTASK FINISHED");
	}
}
