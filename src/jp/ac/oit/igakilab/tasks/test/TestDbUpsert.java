package jp.ac.oit.igakilab.tasks.test;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class TestDbUpsert {
	public static void main(String[] args){
		Document[] docs = {
			new Document("id", 2).append("name", "maguro"),
			new Document("id", 3).append("name", "tai"),
			new Document("id", 4).append("name", "katsuo"),
			new Document("id", 5).append("name", "buri"),
			new Document("id", 6).append("name", "iwashi")
		};

		MongoClient client = new MongoClient("192.168.1.193");
		MongoCollection<Document> col =
				client.getDatabase("test").getCollection("upsert_test");

		for(Document doc : docs){
			Bson filter = Filters.eq("id", doc.getInteger("id"));
			UpdateOptions options = new UpdateOptions();
			options.upsert(true);
			UpdateResult result = col.replaceOne(filter, doc, options);
			System.out.println("upserted: " +
				((result.getUpsertedId() != null) ? result.getUpsertedId().toString() : "nil"));
		}

		client.close();
	}
}
