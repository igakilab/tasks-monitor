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

public class ProjectsDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "projects";

	private MongoClient client;

	public ProjectsDB(MongoClient client){
		this.client = client;
	}

	protected MongoCollection<Document> getCollection(){
		return client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public <T> boolean setProject(T data, DocumentConverter<T> converter){
		Document doc = converter.convert(data);
		if( doc != null ){
			Bson filter = Filters.eq("id", doc.getString("id"));
			UpdateOptions options = new UpdateOptions();
			options.upsert(true);
			getCollection().replaceOne(filter, doc);
			return true;
		}
		return false;
	}

	public <T> List<T> getAllProjects(DocumentConverter<T> converter){
		List<T> list = new ArrayList<T>();
		getCollection().find().forEach(new Block<Document>(){
			@Override
			public void apply(Document doc){
				list.add(converter.parse(doc));
			}
		});
		return list;
	}
}