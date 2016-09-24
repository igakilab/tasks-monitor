package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import jp.ac.oit.igakilab.tasks.util.RandomIdGenerator;

public class SprintsDB {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsDB sdb = new SprintsDB(client);
		/*Sprint spr = new Sprint();
		spr.setBoardId("52e");
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 8, 8);
		spr.setBeginDate(cal.getTime());
		cal.set(2016, 8, 15);
		spr.setFinishDate(cal.getTime());
		sdb.addSprint(spr, new SprintDocumentConverter());*/
		Calendar begin = Calendar.getInstance();
		begin.set(2016, 8, 15);
		Calendar finish = Calendar.getInstance();
		finish.set(2016, 8, 22);
		boolean result = sdb.isValidPeriod("135", begin.getTime(), finish.getTime());
		System.out.println(result);

		client.close();
	}

	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "sprints";

	public static int ID_LENGTH = 14;
	public static int OVERFLOW = 10;

	private MongoClient client;

	public SprintsDB(MongoClient client){
		this.client = client;
	}

	protected MongoCollection<Document> getCollection(){
		return client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public boolean sprintIdExists(String id){
		if( id == null ) return false;
		Bson filter = Filters.eq("id", id);
		return getCollection().count(filter) > 0;
	}

	public boolean isValidPeriod(String boardId, Date begin, Date finish){
		List<Bson> query = Arrays.asList(
			Aggregates.match(Filters.eq("boardId", boardId)),
			Aggregates.group(null, Accumulators.max("lastDate", "$finishDate")));

		Document doc = getCollection().aggregate(query).first();
		if( doc != null && doc.get("lastDate") != null ){
			Date last = doc.getDate("lastDate");
			return last.compareTo(begin) <= 0;
		}

		System.out.println(doc);

		return true;
	}

	public <T> String addSprint(T data, DocumentConverter<T> converter){
		Document doc = converter.convert(data);
		if( doc == null ) return null;

		RandomIdGenerator gen = new RandomIdGenerator(RandomIdGenerator.CHARSET_HEX);
		String newId = gen.generate(ID_LENGTH, OVERFLOW, (str ->
			!sprintIdExists(str)));
		if( newId == null ) return null;

		doc.append("id", newId);
		getCollection().insertOne(doc);

		return newId;
	}

	public <T> T getSprintById(String id, DocumentConverter<T> converter){
		Bson filter = Filters.eq("id", id);
		Document doc = getCollection().find(filter).first();

		if( doc != null ){
			T data = converter.parse(doc);
			return data;
		}else{
			return null;
		}
	}

	public <T> List<T> getAllSprints(DocumentConverter<T> converter){
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
