package jp.ac.oit.igakilab.tasks.db;

import static jp.ac.oit.igakilab.tasks.db.DBEditException.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;


public class MembersDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "members";

	private MongoClient client;

	public MembersDB(MongoClient client){
		this.client = client;
	}

	public MongoCollection<Document> getCollection(){
		return client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public boolean memberIdExists(String mid){
		long cnt = getCollection().count(Filters.eq("id", mid));
		return cnt > 0;
	}

	public <T> void addMember(T data, DocumentConverter<T> converter)
	throws DBEditException{
		Document doc = converter.convert(data);

		//追加できるかどうかテスト
		if( doc.containsKey("id") ){
			String mid = doc.getString("id");
			if( memberIdExists(mid) ){
				throw new DBEditException(ID_ALSO_REGISTED, "idがすでに登録されています");
			}
		}else{
			throw new DBEditException(ID_NOTDEFINED, "idが指定されていません");
		}

		//追加操作
		getCollection().insertOne(doc);
	}

	public <T> void updateMember(T data, DocumentConverter<T> converter)
	throws DBEditException{
		Document doc = converter.convert(data);

		//更新できるかどうかテスト
		if( doc.containsKey("id") ){
			String mid = doc.getString("id");
			if( !memberIdExists(mid) ){
				throw new DBEditException(ID_NOT_REGISTED, "idが登録されていません");
			}
		}else{
			throw new DBEditException(ID_NOTDEFINED, "idが指定されていません");
		}

		//更新操作
		Bson filter = Filters.eq("id", doc.getString("id"));
		getCollection().replaceOne(filter, doc);
	}

	public void deleteMemberById(String mid)
	throws DBEditException{
		if( mid == null ) throw new DBEditException(ID_NOTDEFINED, "idが指定されていません");

		//データチェックと削除動作
		if( memberIdExists(mid) ){
			Bson filter = Filters.eq("id", mid);
			getCollection().deleteOne(filter);
		}else{
			throw new DBEditException(ID_NOT_REGISTED, "idが登録されていません");
		}
	}

	public <T> List<T> getAllMemberList(DocumentParser<T> converter){
		List<T> list = new ArrayList<T>();
		for(Document doc : getCollection().find()){
			list.add(converter.parse(doc));
		}
		return list;
	}
}
