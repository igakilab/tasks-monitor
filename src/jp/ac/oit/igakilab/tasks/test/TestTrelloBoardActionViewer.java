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

public class TestTrelloBoardActionViewer {
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
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		for(int i=0; i<actions.size(); i++){
			Document action = actions.get(i);
			Document memberCreator = (Document)action.get("memberCreator");
			System.out.format("%2d: %s %s\n",
				i, df.format(action.getDate("date")),
				action.getString("type"), memberCreator.getString("fullname"));
		}
	}
}
