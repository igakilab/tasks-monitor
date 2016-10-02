package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionParser;

public class TrelloBoardActionsDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "trello_board_actions";

	private MongoClient client;

	public TrelloBoardActionsDB(MongoClient client){
		this.client = client;
	}

	public TrelloBoardActionsDB(String host, int port){
		this.client = new MongoClient(host, port);
	}

	protected MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public List<Document> getActionDocuments(String boardId){
		Bson filter = Filters.eq("boardId", boardId);
		List<Document> result = new ArrayList<Document>();

		for(Document doc : getCollection().find(filter)){
			result.add(doc);
		}

		return result;
	}

	public List<TrelloAction> getTrelloActions(String boardId, TrelloActionParser<Document> parser){
		Bson filter = Filters.eq("boardId", boardId);
		List<TrelloAction> result = new ArrayList<TrelloAction>();

		for(Document doc : getCollection().find(filter)){
			result.add(parser.parse(doc));
		}

		return result;
	}
}
