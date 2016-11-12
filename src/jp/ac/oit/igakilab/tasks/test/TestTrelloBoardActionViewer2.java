package jp.ac.oit.igakilab.tasks.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TestTrelloBoardActionViewer2 {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		List<TrelloAction> actions = adb.getTrelloActions(
			"57d3f5cac2c3720549a9b8c1", new TrelloActionDocumentParser());

		showActions(actions);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();
		System.out.println("SHORT LINK: " + board.getShortLink());

		System.out.println(
			TrelloAction.getTargetTypeLabels(TrelloAction.TARGET_CARD));

		client.close();
	}

	private static void showActions(List<TrelloAction> actions){
		//DateFormat pdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		//DateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
		for(int i=0; i<actions.size(); i++){
			TrelloAction action = actions.get(i);
			System.out.println(String.format("%2d: ", i) + action.dataString());
			if( action.getTargetType() == TrelloAction.TARGET_CARD ){
				Map<String,String> card = action.getData().getChildMap("card");
				for(Entry<String,String> entry : card.entrySet()){
					System.out.format("\t\t%s: %s\n", entry.getKey(), entry.getValue());
				}
			}
		}
	}


}
