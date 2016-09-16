package jp.ac.oit.igakilab.tasks.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
		DateFormat pdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		DateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
		for(int i=0; i<actions.size(); i++){
			TrelloAction action = DocumentTrelloActionParser.getTrelloActionInstance(actions.get(i));
			System.out.println(String.format("%2d: ", i) + action.dataString());
		}
	}


}
