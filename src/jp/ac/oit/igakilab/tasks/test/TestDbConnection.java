package jp.ac.oit.igakilab.tasks.test;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class TestDbConnection
implements Closeable{
	static String DEFAULT_HOST = "192.168.1.193";
	static int DEFAULT_PORT = 27017;
	static String DEFAULT_DB = "test";
	static String DEFAULT_COLLECTION = "contest";

	MongoClient client;

	public TestDbConnection(){
		client = new MongoClient(DEFAULT_HOST, DEFAULT_PORT);
	}

	private MongoCollection<Document> getDefaultCollection(){
		return client.getDatabase(DEFAULT_DB)
			.getCollection(DEFAULT_COLLECTION);
	}

	public List<String> find(){
		MongoCollection<Document> collection = getDefaultCollection();
		List<String> results = new ArrayList<String>();
		for(Document doc : collection.find()){
			results.add(doc.toJson());
		}
		return results;
	}

	public String insertOne(String jsonStr){
		MongoCollection<Document> collection = getDefaultCollection();
		Document doc = Document.parse(jsonStr);
		collection.insertOne(doc);
		return doc.toJson();
	}

	public void close(){
		System.out.println("Closed.");
		client.close();
	}
}
