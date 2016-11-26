package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;
import jp.ac.oit.igakilab.tasks.util.RandomIdGenerator;

public class SprintsDB {
	public class SprintsDBEditException extends DBEditException{
		public static final int INVALID_PERIOD = 2001;
		public static final int CARDID_REGISTED = 2002;

		public SprintsDBEditException(int code, String msg){
			super(code, msg);
		}
	}

	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsDB sdb = new SprintsDB(client);
		Sprint spr = new Sprint();
		spr.setBoardId("135");
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 8, 19);
		spr.setBeginDate(cal.getTime());
		cal.set(2016, 8, 29);
		spr.setFinishDate(cal.getTime());

		String id = null;
		try{
			id = sdb.addSprint(spr, new SprintDocumentConverter());
		}catch(DBEditException e0){
			System.out.println(e0.getMessage());
		}
		System.out.println(id);





		client.close();
	}

	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "sprints";

	public static int ID_LENGTH = 14;
	public static int OVERFLOW = 10;

	protected static Bson FILTER_NOT_CLOSED = Filters.or(
		Filters.exists("closedDate", false),
		Filters.eq("closedDate", null));

	private MongoClient client;
	private MongoCollection<Document> collection;

	public SprintsDB(MongoClient client){
		this.client = client;
		collection = getCollection();
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
		if( boardId == null || begin == null || finish == null ){
			return false;
		}
		if( begin.compareTo(finish) > 0 ){
			return false;
		}

		List<Bson> query = Arrays.asList(
			Aggregates.match(Filters.and(
				Filters.eq("boardId", boardId),
				FILTER_NOT_CLOSED)),
			Aggregates.group(null, Accumulators.max("lastDate", "$finishDate")));

		Document doc = getCollection().aggregate(query).first();
		if( doc != null && doc.get("lastDate") != null ){
			Date last = doc.getDate("lastDate");
			return last.compareTo(begin) <= 0;
		}

		return true;
	}

	public <T> String addSprint(T data, DocumentConverter<T> converter)
	throws SprintsDBEditException{
		Document doc = converter.convert(data);
		if( doc == null ) throw new SprintsDBEditException(
			DBEditException.INVALID_DATA, "不正なデータです");

		RandomIdGenerator gen = new RandomIdGenerator(RandomIdGenerator.CHARSET_HEX);
		String newId = gen.generate(ID_LENGTH, OVERFLOW, (str ->
			!sprintIdExists(str)));
		if( newId == null ) throw new SprintsDBEditException(
			DBEditException.ID_GEN_OVERFLOW, "idの割り当てに失敗しました");

		if( !isValidPeriod(
			doc.getString("boardId"),
			doc.getDate("beginDate"),
			doc.getDate("finishDate"))
		){
			throw new SprintsDBEditException(
				SprintsDBEditException.INVALID_PERIOD, "不正な期間です");
		}

		doc.append("id", newId);
		getCollection().insertOne(doc);

		return newId;
	}

	public boolean updateSprintCards(String id, List<String> cardIds){
		Bson filter = Filters.eq("id", id);
		Bson update = Updates.set("trelloCardIds", cardIds);

		UpdateResult res = getCollection().updateOne(filter, update);

		return res.getModifiedCount() > 0;
	}

	public String getBoardIdBySprintId(String id){
		Bson filter = Filters.eq("id", id);

		Document doc = getCollection().find(filter).first();
		DocumentValuePicker picker = new DocumentValuePicker(doc);

		return doc == null ? null : picker.getString("boardId", null);
	}

	public <T> T getSprintById(String id, DocumentParser<T> converter){
		Bson filter = Filters.eq("id", id);
		Document doc = getCollection().find(filter).first();

		if( doc != null ){
			T data = converter.parse(doc);
			return data;
		}else{
			return null;
		}
	}

	public <T> List<T> getSprintsByBoardId(String boardId, DocumentParser<T> converter){
		Bson filter = Filters.eq("boardId", boardId);
		List<T> result = new ArrayList<T>();

		for(Document doc : getCollection().find(filter)){
			T tmp = converter.parse(doc);
			result.add(tmp);
		}

		return result;
	}

	public List<String> getSprintIdsByBoardId(String boardId){
		Bson filter = Filters.eq("boardId", boardId);
		List<String> result = new ArrayList<String>();

		for(Document doc : getCollection().find(filter)){
			result.add(doc.getString("id"));
		}

		return result;
	}

	/**
	 * すでに終了しているスプリントを新しい順に取得します。
	 * 引数に各フィルタを指定して、目的のデータを取得します
	 * @param boardId 指定されたボードidのスプリントのデータが取得されます(必須)
	 * @param originSprintId 起点となるスプリントで、これ以降に終了されたスプリントは対象になりません。　
	 *                       このスプリントが閉じられていない場合や、nullが指定された場合は、無効になるフィルタです。
	 * @param cnt 最新のスプリントをいくつ取得するか指定します 0未満の数字を指定するとこの制限はなくなります
	 * @param parser ドキュメントパーザーです(必須)
	 * @return 対象のスプリントのリスト
	 */
	public <T> List<T> getLatestFinishedSprintByBoardId
	(String boardId, String originSprintId, int cnt, DocumentParser<T> parser){
		//originSprintを取得する
		Sprint originSprint = null;
		if( originSprintId != null ){
			originSprint = getSprintById(originSprintId, new SprintDocumentConverter());
		}

		//フィルタを形成する
		List<Bson> elements = new ArrayList<>();
		elements.add(Filters.eq("boardId", boardId));
		elements.add(Filters.ne("closedDate", null));
		if( originSprint != null && originSprint.getClosedDate() != null ){
			elements.add(Filters.lte("closedDate", originSprint.getClosedAt()));
		}
		Bson filter = Filters.and(elements);

		//データを取得する
		Bson sorts = Sorts.descending("closedDate");
		FindIterable<Document> cur = collection.find(filter).sort(sorts);

		//データをぱーずする
		List<T> result = new ArrayList<>();
		Iterator<Document> itr = cur.iterator();
		for(int i=0; (itr.hasNext() && (cnt < 0 || i < cnt)); i++){
			Document doc = itr.next();
			result.add(parser.parse(doc));
		}

		return result;
	}

	public <T> List<T> getAllSprints(DocumentParser<T> converter){
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

	public boolean addTrelloCardId(String id, String cardId){
		if( !sprintIdExists(id) ) return false;

		MongoCollection<Document> col = getCollection();

		Bson checkRegistedFilter = Filters.and(
			Filters.eq("id", id),
			Filters.eq("trelloCardIds", cardId));
		if( col.count(checkRegistedFilter) > 0 ) return false;

		Bson filter = Filters.eq("id", id);
		Bson updates = Updates.push("trelloCardIds", cardId);
		UpdateResult result = col.updateOne(filter, updates);

		return result.getModifiedCount() == 1;
	}

	public boolean removeTrelloCardId(String id, String cardId){
		Bson filter = Filters.eq("id", id);
		Bson updates = Updates.pull("trelloCardIds", cardId);

		UpdateResult result = getCollection().updateOne(filter, updates);

		return result.getModifiedCount() == 1;
	}
}
