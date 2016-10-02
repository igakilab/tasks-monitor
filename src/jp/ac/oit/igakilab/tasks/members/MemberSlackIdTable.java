package jp.ac.oit.igakilab.tasks.members;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;

public class MemberSlackIdTable {
	MongoClient dbClient;
	Map<String,String> table;

	public MemberSlackIdTable(MongoClient dbClient){
		this.dbClient = dbClient;
		this.table = new HashMap<String,String>();
		build();
	}

	public void build(){
		MembersDB mdb = new MembersDB(dbClient);
		List<Member> members = mdb.getAllMemberList(new MemberDocumentConverter());

		table.clear();
		for(Member m : members){
			table.put(m.getId(), m.getSlackId());
		}
	}

	public String getSlackId(String memberId){
		return table.get(memberId);
	}

	public String getMemberId(String slackId){
		for(Entry<String,String> e : table.entrySet()){
			if( e.getValue().equals(slackId) ){
				return e.getKey();
			}
		}
		return null;
	}
}
