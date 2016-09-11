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

public class BoardDBDriver {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "boards";

	private MongoClient client;

	public BoardDBDriver(MongoClient client){
		this.client = client;
	}

	public BoardDBDriver(String host, int port){
		this.client = new MongoClient(host, port);
	}

	private MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public Document getBoardById(String boardId){
		Bson filter = Filters.eq("id", boardId);
		return getCollection().find(filter).first();
	}

	public List<Document> getBoardList(){
		List<Document> result = new ArrayList<Document>();
		for(Document doc : getCollection().find()){
			result.add(doc);
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
