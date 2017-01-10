package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;

public class SprintResultsDB{
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "sprint_results";

	private MongoClient client;
	protected MongoCollection<Document> collection;

	/**
	 * コンストラクタです
	 * @param client MongoClientインスタンス
	 */
	public SprintResultsDB(MongoClient client){
		this.client = client;
		collection = getCollection();
	}


	protected MongoCollection<Document> getCollection(){
		return client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	/**
	 * 指定されたidのスプリントが登録されているかどうかを返却します
	 * @param id スプリントID文字列
	 * @return スプリントリザルトが登録されているときtrue
	 */
	public boolean sprintIdExists(String id){
		if( id == null ) return false;
		Bson filter = Filters.eq("sprintId", id);
		return getCollection().count(filter) > 0;
	}

	/**
	 * スプリントリザルトを新しく生成します。
	 * @param sprintId スプリントのID
	 * @param createdAt リザルト作成時間、指定しない場合は自動的に現在時刻となる
	 * @return 登録に成功したらtrue
	 */
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

//	@Deprecated
//	public <T> boolean addSprintResult(T data, DocumentConverter<T> converter){
//		Document doc = converter.convert(data);
//		if( doc == null || !doc.containsKey("sprintId") ){
//			return false;
//		}
//
//		Bson filter = Filters.eq("sprintId", doc.get("sprintId"));
//		UpdateOptions options = new UpdateOptions();
//		options.upsert(true);
//
//		collection.replaceOne(filter, doc, options);
//		return true;
//	}

	/**
	 * スプリントリザルトにカードの結果データを追加します
	 * @param data データ本体
	 * @param converter データコンバータ
	 * @return 登録に成功したらtrue
	 */
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

	/**
	 * スプリントリザルトのカードデータにタグを新しく設定します。
	 * @param sprintId スプリントのID
	 * @param cardId カードのID
	 * @param tags 設定するタグの配列(文字列の配列)
	 * @return 登録に成功したらtrue
	 */
	public <T> boolean setTagsToSprintCard(String sprintId, String cardId, List<String> tags){
		Bson filter = Filters.and(
			Filters.eq("sprintId", sprintId),
			Filters.eq("cardId", cardId));
		Bson updates = Updates.set("tags", tags);

		return collection.updateOne(filter, updates).getModifiedCount() > 0;
	}

	/**
	 * カードIDに指定されたカードが達成されなかった回数を返却します。
	 * (これまでのスプリントリザルトの中で、対象カードが未達成になった回数)
	 * @param cardId 対象のカードID
	 * @return 回数
	 */
	public int countCardRemainedTimes(String cardId){
		Bson filter = Filters.and(
			Filters.eq("cardId", cardId),
			Filters.eq("finished", false)
		);

		return (int)collection.count(filter);
	}

	/**
	 * スプリントリザルトが生成された日時を返却します
	 * @param id スプリントID
	 * @return 生成日時(スプリントリザルトが見つからない場合はnull)
	 */
	public Date getCreatedDateBySprintId(String id){
		Bson filter = Filters.eq("sprintId", id);
		Document doc = collection.find(filter).first();

		return doc != null ? doc.getDate("createdAt") : null;
	}

	/**
	 * スプリントリザルトを取得します
	 * @param id スプリントID
	 * @param parser データパーザー
	 * @return 変換されたスプリントリザルトのデータ
	 */
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

	/**
	 * 指定されたスプリントリザルトのカードを一覧で取得します
	 * @param id スプリントID
	 * @param parser データパーザー(スプリントカード)
	 * @return 変換された素プリンリザルトカードの配列
	 */
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

	/**
	 * メンバーIDに指定されたメンバーが所属していたスプリントIDの一覧を取得します
	 * @param memberId メンバーID
	 * @return スプリントIDの配列
	 */
	public List<String> getSprintResultIdsByMemberId(String memberId){
		List<Bson> query = Arrays.asList(
			Aggregates.match(Filters.eq("memberIds", memberId)),
			Aggregates.group("$sprintId"));

		List<String> ids = new ArrayList<String>();
		for(Document doc : collection.aggregate(query)){
			ids.add(doc.getString("_id"));
		}

		return ids;
	}

	/**
	 * 指定されたスプリントIDの中で、完了したスプリントカードの一覧を取得します
	 * @param sprintId スプリントID
	 * @param parser データパーザー(スプリントカード)
	 * @return 変換されたスプリントリザルトカードの配列
	 */
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

	public <T> List<T> getResultCardsByMemberIds
	(Collection<String> memberId, DocumentParser<T> parser){
		List<Bson> idFilter = new ArrayList<Bson>();
		memberId.forEach((mid -> idFilter.add(Filters.eq("memberIds", mid))));
		Bson filter = Filters.and(
			Filters.exists("cardId", true),
			Filters.or(idFilter)
		);

		List<T> cards = new ArrayList<T>();
		for(Document doc : collection.find(filter)){
			T data = parser.parse(doc);
			if( data != null ) cards.add(data);
		}

		return cards;
	}
}
