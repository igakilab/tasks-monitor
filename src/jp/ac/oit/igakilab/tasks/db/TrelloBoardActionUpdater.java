package jp.ac.oit.igakilab.tasks.db;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class TrelloBoardActionUpdater {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "trello_board_actions";

	private MongoClient client;

	public TrelloBoardActionUpdater(MongoClient client){
		this.client = client;
	}

	public TrelloBoardActionUpdater(String host, int port){
		this.client = new MongoClient(host, port);
	}

	private MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
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

	/*
	public int upsertDatabaseByJson(List<String> jsons, String boardId){
		List<Document> docs = new ArrayList<Document>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		for(String json : jsons){
			Document doc = Document.parse(json);
			if( doc.containsKey("date") ){
				Date date = null;
				try{
					date = df.parse(doc.getString("date"));
				}catch(Exception e0){
					e0.printStackTrace();
				}
				doc.append("date", date);
			}
			docs.add(doc);
		}

		return upsertDatabase(docs, boardId);
	}
	*/
}