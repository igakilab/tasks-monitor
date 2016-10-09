package jp.ac.oit.igakilab.tasks.test;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TestTrelloActionBoard {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MongoCollection<Document> col = client
			.getDatabase("tasks-monitor").getCollection("trello_board_actions");

		FindIterable<Document> result = col.find(Filters.eq("boardId", "57d3f5cac2c3720549a9b8c1"));

		List<TrelloAction> actions = convertToTrelloActions(toList(result));

		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		System.out.println("BOARDID: " + board.getId());
		System.out.println("BOARDNAME: " + board.getName());
		board.printListsAndCards(System.out);

		System.out.println();
		System.out.println("<ignored actions (" + board.getIgnoredActions().size() + ")>");
		board.getIgnoredActions().forEach((action) ->
			System.out.println(action.getType()));

		client.close();
	}

	private static <T> List<T> toList(Iterable<T> iterable){
		List<T> list = new ArrayList<T>();
		for(T obj : iterable){
			list.add(obj);
		}
		return list;
	}

	private static List<TrelloAction> convertToTrelloActions(List<Document> docs){
		List<TrelloAction> actions = new ArrayList<TrelloAction>();
		TrelloActionDocumentParser parser = new TrelloActionDocumentParser();
		for(Document doc : docs){
			actions.add(parser.parse(doc));
		}
		return actions;
	}


}
