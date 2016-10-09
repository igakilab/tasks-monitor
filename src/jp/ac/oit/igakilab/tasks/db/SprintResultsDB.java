package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;

public class SprintResultsDB{
	public static void main(String[] args){

	}

	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "sprint-results";

	private MongoClient client;
	protected MongoCollection<Document> collection;

	public SprintResultsDB(MongoClient client){
		this.client = client;
		collection = getCollection();
	}

	protected MongoCollection<Document> getCollection(){
		return client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public boolean sprintIdExists(String id){
		if( id == null ) return false;
		Bson filter = Filters.eq("sprintId", id);
		return getCollection().count(filter) > 0;
	}

	public <T> boolean addSprintResult(T data, DocumentConverter<T> converter){
		Document doc = converter.convert(data);
		if( doc == null || doc.containsKey("sprintId") ){
			return false;
		}

		Bson filter = Filters.eq("sprintId", doc.get("sprintId"));
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		collection.replaceOne(filter, doc, options);
		return true;
	}

	public <T> T getSprintResultById(String id, DocumentParser<T> converter){
		Bson filter = Filters.eq("sprintId", id);
		Document doc = getCollection().find(filter).first();

		if( doc != null ){
			T data = converter.parse(doc);
			return data;
		}else{
			return null;
		}
	}

	public <T> List<T> getAllSprintResults(DocumentParser<T> converter){
		List<T> list = new ArrayList<T>();

		getCollection().find().forEach(new Block<Document>(){
			@Override
			public void apply(Document doc){
				T tmp = converter.parse(doc);
				if( tmp != null ) list.add(tmp);
			}
		});

		return list;
	}
}
