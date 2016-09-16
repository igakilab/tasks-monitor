package jp.ac.oit.igakilab.tasks.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.model.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloAction;

public class TestTrelloBoardActionViewer2 {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MongoCollection<Document> col = client
			.getDatabase("tasks-monitor").getCollection("trello_board_actions");

		FindIterable<Document> result = col.find(Filters.eq("boardId", "57d3f5cac2c3720549a9b8c1"));

		showActions(toList(result));

		client.close();
	}

	private static <T> List<T> toList(Iterable<T> iterable){
		List<T> list = new ArrayList<T>();
		for(T obj : iterable){
			list.add(obj);
		}
		return list;
	}

	private static void showActions(List<Document> actions){
		//DateFormat pdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		//DateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
		for(int i=0; i<actions.size(); i++){
			TrelloAction action = DocumentTrelloActionParser.parse(actions.get(i));
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
