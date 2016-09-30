package jp.ac.oit.igakilab.tasks.members;

import java.util.HashMap;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;

public class MemberTrelloIdTable extends HashMap<String,String>{
	MongoClient dbClient;

	public MemberTrelloIdTable(MongoClient dbClient){
		this.dbClient = dbClient;
		build();
	}

	public void build(){
		MembersDB mdb = new MembersDB(dbClient);
		List<Member> members = mdb.getAllMemberList(new MemberDocumentConverter());

		for(Member m : members){
			put(m.getId(), m.getTrelloId());
		}
	}
}
