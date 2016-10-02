package jp.ac.oit.igakilab.tasks.db;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class TrelloBoardActionsDBUpdater extends TrelloBoardActionsDB{
	public TrelloBoardActionsDBUpdater(MongoClient client){
		super(client);
	}

	public int upsertDatabase(List<Document> docs, String boardId){
		MongoCollection<Document> col = getCollection();
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		int upsertCnt = 0;
		for(Document doc : docs){
			if( boardId != null ){
				doc.append("boardId", boardId);
			}
			Bson filter = Filters.eq("id",doc.get("id"));
			UpdateResult result = col.replaceOne(filter, doc, options);
			if( result.getUpsertedId() != null ) upsertCnt++;
		}

		return upsertCnt;
	}
}
