package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Calendar;
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
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;

public class SprintResultsDB{
	public static void main(String[] args){
		SprintResult result = new SprintResult("135");
		result.setCreatedAt(Calendar.getInstance().getTime());
		TrelloCardMembers mc1 = new TrelloCardMembers("4f2c");
		mc1.addMemberId("koike");
		result.addRemainedCard(mc1);
		TrelloCardMembers mc2 = new TrelloCardMembers("33a1");
		mc2.addMemberId("shimizu");
		result.addFinishedCard(mc2);

		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintResultsDB resdb = new SprintResultsDB(client);
		boolean res = resdb.addSprintResult(result, new SprintResultDocumentConverter());
		System.out.println("res: " + res);

		SprintResult dbret = resdb.getSprintResultBySprintId("135", new SprintResultDocumentConverter());
		System.out.format("%s, %d, %d\n",
			dbret.getSprintId(), dbret.getRemainedCards().size(), dbret.getFinishedCards().size());

		client.close();
	}

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

	public <T> List<T> getSprintResultsBySprintIds(List<String> sprintIds, DocumentParser<T> parser){
		Bson filter = Filters.in("sprintId", sprintIds);

		List<T> result = new ArrayList<T>();
		for(Document doc : collection.find(filter)){
			T data = parser.parse(doc);
			if( data != null ) result.add(data);
		}

		return result;
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
