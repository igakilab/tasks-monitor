package jp.ac.oit.igakilab.tasks.db;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;

public class TrelloBoardCacheDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "trello_board_cache";

	private MongoClient client;
	private MongoCollection<Document> collection;

	public TrelloBoardCacheDB(MongoClient client){
		this.client = client;
		this.collection = getCollection();
	}

	protected MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}


	public boolean boardIdExists(String boardId){
		Bson filter = Filters.eq("boardId", boardId);

		return collection.count(filter) > 0;
	}


	public <T> boolean updateBoardData(String boardId, Date lastUpdate, T data, DocumentConverter<T> converter){
		Document docData = converter.convert(data);
		UpdateResult result = null;

		if( docData != null && boardId != null ){
			Bson updates = Updates.combine(
				Updates.set("boardId", boardId),
				Updates.set("lastUpdate", lastUpdate),
				Updates.set("data", docData)
			);

			Bson filter = Filters.eq("boardId", boardId);
			UpdateOptions options = new UpdateOptions();
			options.upsert(true);

			result = collection.updateOne(filter, updates, options);
		}

		return result != null ? result.getModifiedCount() > 0 : false;
	}


	public <T> boolean updateBoardData(String boardId, T data, DocumentConverter<T> converter){
		return updateBoardData(boardId, Calendar.getInstance().getTime(), data, converter);
	}


	public <T> T findBoardData(String boardId, DocumentParser<T> parser){
		Bson filter = Filters.eq("boardId", boardId);

		Document doc = collection.find(filter).first();

		if( doc != null ){
			T data = parser.parse(doc);
			return data;
		}else{
			return null;
		}
	}


	public Date getLastUpdateDate(String boardId){
		Bson filter = Filters.eq("boardId", boardId);

		Document doc = collection.find(filter).first();

		if( doc != null && doc.get("lastUpdate") instanceof Date ){
			return doc.getDate("lastUpdate");
		}else{
			return null;
		}
	}
}
