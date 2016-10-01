package jp.ac.oit.igakilab.tasks.scripts;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardData;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloCardNotification {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		List<TrelloAction> actions = adb.getTrelloActions("57d3f5cac2c3720549a9b8c1",
			new DocumentTrelloActionParser());

		if( actions.size() > 0 ){
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			TrelloBoardData data = board.buildBoardData();
			System.out.println(data.toString());
		}

		client.close();
	}
}
