package jp.ac.oit.igakilab.tasks.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.BoardDBDriver.Board;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionUpdater;
import jp.ac.oit.igakilab.tasks.trello.BoardActionFetcher;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;

public class TestTrelloBoardActionsUpdater {
	public static void main(String[] args){
		initProperties();

		BoardDBDriver bdb = new BoardDBDriver(
			TasksMongoClientBuilder.createClient());
		for(Board board : bdb.getBoardList()){
			System.out.println("fetching board : " + board.getId());
			int res = updateBoardActions(board.getId(), null);
			System.out.println("complete (" + res + " records added.)");
		}


	}

	static int updateBoardActions(String boardId, Date since){
		BoardActionFetcher fetcher = new BoardActionFetcher(
			TasksTrelloClientBuilder.createApiClient(), boardId);
		fetcher.fetch(since);

		JSONArray data = fetcher.getJSONArrayData();
		System.out.println(data.size() + " record(s) received.");

		List<Document> actions = new ArrayList<Document>();
		for(Object obj : data){
			JSONObject jobj = (JSONObject)obj;
			actions.add(Document.parse(jobj.toJSONString()));
		}
		Collections.reverse(actions);

		TrelloBoardActionUpdater updater = new TrelloBoardActionUpdater(
			TasksMongoClientBuilder.createClient());
		return updater.upsertDatabase(actions, boardId);
	}

	static void initProperties(){
		AppProperties.globalInit();
		AppProperties.globalSet("tasks.trello.key", "67ad72d3feb45f7a0a0b3c8e1467ac0b");
		AppProperties.globalSet("tasks.trello.token",
			"268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
		AppProperties.globalSet("tasks.db.host", "192.168.1.193");
	}
}
