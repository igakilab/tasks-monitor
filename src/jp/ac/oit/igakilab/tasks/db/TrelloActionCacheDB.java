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
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;

/*
 * データベースのデータ構造
 * メタレコード
 * {category:<カテゴリ名>, id:<ID>, lastUpdate:<最終更新日時>}
 * アクションレコード
 * {category:<カテゴリ名>, id:<ID>, actionId:<アクションID>, data:<アクションデータ>}
 *
 * [コンバータ規約]
 * このクラスで指定するDocumentConverterで返されるDocumentには
 * "id"の項目を持たせることが必須になります
 */

public class TrelloActionCacheDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "trello_action_cache";

	private MongoClient client;
	private MongoCollection<Document> collection;

	public TrelloActionCacheDB(MongoClient client){
		this.client = client;
		this.collection = getCollection();
	}

	protected MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}


	protected void setLastUpdateDate(String category, String id, Date date){
		Bson filter = Filters.and(
			Filters.eq("category", category),
			Filters.eq("id", id));
		Bson updates = Updates.set("lastUpdate", date);

		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		collection.updateOne(filter, updates, options);
	}


	public boolean exists(String category, String id){
		Bson filter = Filters.and(
			Filters.eq("category", category),
			Filters.eq("id", id),
			Filters.exists("actionId", false));

		return collection.count(filter) > 0;
	}


	public boolean actionsExists(String category, String id){
		return countActions(category, id) > 0;
	}


	public long countActions(String category, String id){
		Bson filter = Filters.and(
			Filters.eq("category", category),
			Filters.eq("id", id),
			Filters.exists("actionId", true));

		return collection.count(filter);
	}


	public <T> long applyActionCache
	(String category, String id, Date lastUpdate, Collection<T> data, DocumentConverter<T> converter){
		//更新日時を記録
		setLastUpdateDate(category, id, lastUpdate);

		//データを変換
		List<Document> dataDocs = new ArrayList<Document>();
		for(T d : data){
			Document tmp = converter.convert(d);
			if( tmp != null && tmp.get("id") instanceof String ){
				dataDocs.add(tmp);
			}
		}

		//更新作業
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		long cnt = 0;

		for(Document dataDoc : dataDocs){
			String actionId = dataDoc.getString("id");

			Bson filter = Filters.and(
				Filters.eq("category", category),
				Filters.eq("id", id),
				Filters.eq("actionId", actionId));

			Bson updates = Updates.set("data", dataDoc);

			UpdateResult res = collection.updateOne(filter, updates, options);
			cnt += res.getUpsertedId() != null ? 1 : res.getModifiedCount();
		}

		return cnt;
	}


	public <T> long applyActionCache
	(String category, String id, Collection<T> data, DocumentConverter<T> converter){
		return applyActionCache(category, id, Calendar.getInstance().getTime(), data, converter);
	}


	public long removeActionCache(String category, String id){
		Bson filter = Filters.and(
			Filters.eq("category", category),
			Filters.eq("id", id));

		return collection.deleteMany(filter).getDeletedCount();
	}


	public <T> List<T> findActionCache(String category, String id, DocumentParser<T> parser){
		Bson filter = Filters.and(
			Filters.eq("category", category),
			Filters.eq("id", id),
			Filters.exists("actionId", true));

		FindIterable<Document> cursor = collection.find(filter);

		List<T> actions = new ArrayList<T>();
		for(Document doc : cursor){
			if( doc.get("data") instanceof Document ){
				T tmp = parser.parse((Document)doc.get("data"));
				if( tmp != null ){
					actions.add(tmp);
				}
			}
		}

		return actions;
	}


	public Date getLastUpdateDate(String category, String id){
		Bson filter = Filters.and(
			Filters.eq("category", category),
			Filters.eq("id", id),
			Filters.exists("actionId", false));

		Document doc = collection.find(filter).first();

		if( doc != null && doc.get("lastUpdate") instanceof Date ){
			return doc.getDate("lastUpdate");
		}else{
			return null;
		}
	}
}
