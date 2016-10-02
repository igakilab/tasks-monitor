package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

public class TrelloBoardsDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "trello_boards";

	public static class Board{
		static Board convert(Document doc){
			Board board = new Board();
			board.id = doc.getString("id");
			board.lastUpdate = doc.getDate("lastUpdate");
			return board;
		}

		private String id;
		private Date lastUpdate;

		public String getId(){return id;}
		public Date getLastUpdate(){return lastUpdate;}
	}

	private MongoClient client;

	public TrelloBoardsDB(MongoClient client){
		this.client = client;
	}

	public TrelloBoardsDB(String host, int port){
		this.client = new MongoClient(host, port);
	}

	private MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public boolean boardIdExists(String boardId){
		Bson filter = Filters.eq("id", boardId);
		return getCollection().count(filter) > 0;
	}

	public Document getBoardById(String boardId){
		Bson filter = Filters.eq("id", boardId);
		return getCollection().find(filter).first();
	}

	public List<Board> getBoardList(){
		List<Board> result = new ArrayList<Board>();
		for(Document doc : getCollection().find()){
			result.add(Board.convert(doc));
		}
		return result;
	}

	public int updateLastUpdateDate(String boardId, Date lastdate){
		Bson filter = Filters.eq("id", boardId);
		Bson update = Updates.set("lastUpdate", lastdate);

		UpdateResult result = getCollection().updateOne(filter, update);
		return (int)result.getModifiedCount();
	}
}
