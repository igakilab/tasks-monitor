package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;

public class SprintResultsDB{
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "sprint_results";

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

	public boolean createSprintResult(String sprintId, Date createdAt){
		if( sprintId != null && !sprintIdExists(sprintId) ){
			Document doc = new Document("sprintId", sprintId);
			doc.append("createdAt",
				(createdAt != null ? createdAt : Calendar.getInstance().getTime()));

			collection.insertOne(doc);
			return true;
		}else{
			return false;
		}
	}

	@Deprecated
	public <T> boolean addSprintResult(T data, DocumentConverter<T> converter){
		Document doc = converter.convert(data);
		if( doc == null || !doc.containsKey("sprintId") ){
			return false;
		}

		Bson filter = Filters.eq("sprintId", doc.get("sprintId"));
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		collection.replaceOne(filter, doc, options);
		return true;
	}

	public <T> boolean addSprintResultCard(T data, DocumentConverter<T> converter){
		Document doc = converter.convert(data);
		if( doc == null || !doc.containsKey("sprintId") ||
			!doc.containsKey("cardId") || !doc.containsKey("finished") )
		{
			return false;
		}

		Bson filter = Filters.and(
			Filters.eq("sprintId", doc.get("sprintId")),
			Filters.eq("cardId", doc.get("cardId")));
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(true);

		collection.replaceOne(filter, doc, opt);
		return true;
	}

	public Date getCreatedDateBySprintId(String id){
		Bson filter = Filters.eq("sprintId", id);
		Document doc = collection.find(filter).first();

		return doc != null ? doc.getDate("createdAt") : null;
	}

	public <T> T getSprintResultBySprintId(String id, DocumentParser<T> parser){
		Bson filter = Filters.eq("sprintId", id);
		Document doc = getCollection().find(filter).first();

		if( doc != null ){
			T data = parser.parse(doc);
			return data;
		}else{
			return null;
		}
	}

	public <T> List<T> getSprintResultCardsBySprintId(String id, DocumentParser<T> parser){
		Bson filter = Filters.and(
			Filters.eq("sprintId", id),
			Filters.exists("cardId", true));

		FindIterable<Document> cursor = collection.find(filter);

		List<T> cards = new ArrayList<T>();
		for(Document doc : cursor){
			T data = parser.parse(doc);
			if( data != null ) cards.add(data);
		}

		return cards;
	}

	public <T> List<T> getSprintResultsByCardMemberId(String memberId, DocumentParser<T> parser){
		Bson filter = Filters.and(
			Filters.exists("cardId"),
			Filters.eq("memberIds", memberId)
		);

		List<T> result = new ArrayList<T>();
		FindIterable<Document> cursor = collection.find(filter);
		for(Document doc : cursor){
			T data = parser.parse(doc);
			if( data != null ) result.add(data);
		}

		return result;
	}

	public int countCardRemainedTimes(String cardId){
		Bson filter = Filters.and(
			Filters.eq("cardId", cardId),
			Filters.eq("finished", false)
		);

		return (int)collection.count(filter);
	}

	public <T> List<T> getSprintResultsBySprintIds
	(Collection<String> sprintIds, DocumentParser<T> parser){
		Bson filter = Filters.and(
			Filters.exists("cardId", false),
			Filters.in("sprintId", sprintIds));

		List<T> results = new ArrayList<>();
		for(Document doc : collection.find(filter)){
			T data = parser.parse(doc);
			if( data != null ) results.add(data);
		}

		return results;
	}

	public <T> List<T> getFinishedCardsBySprintId(String sprintId, DocumentParser<T> parser) {
		Bson filter = Filters.and(
			Filters.exists("cardId", true),
			Filters.eq("sprintId", sprintId));

		List<T> cards = new ArrayList<T>();
		for(Document doc : collection.find(filter)){
			T data = parser.parse(doc);
			if( data != null ) cards.add(data);
		}

		return cards;
	}
}
