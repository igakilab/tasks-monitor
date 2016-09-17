package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

public class MembersDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "members";

	private MongoClient client;

	public MembersDB(MongoClient client){
		this.client = client;
	}

	public MongoCollection<Document> getCollection(){
		return client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public <T> boolean addMember(T data, DocumentConverter<T> converter){
		Document doc = converter.convert(data);
		if( doc != null ){
			Bson filter = Filters.eq("id", doc.get("id"));
			UpdateOptions options = new UpdateOptions();
			options.upsert(true);
			getCollection().replaceOne(filter, doc, options);
			return true;
		}
		return false;
	}

	public <T> List<T> getAllMemberList(DocumentConverter<T> converter){
		List<T> list = new ArrayList<T>();
		for(Document doc : getCollection().find()){
			list.add(converter.parse(doc));
		}
		return list;
	}
}
