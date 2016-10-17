package jp.ac.oit.igakilab.tasks.members;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;

public class MemberTrelloIdTable{
	MongoClient dbClient;
	Map<String,Member> table;

	public MemberTrelloIdTable(MongoClient dbClient){
		this.dbClient = dbClient;
		this.table = new HashMap<String,Member>();
		build();
	}

	public void build(){
		MembersDB mdb = new MembersDB(dbClient);
		List<Member> members = mdb.getAllMemberList(new MemberDocumentConverter());

		table.clear();
		for(Member m : members){
			table.put(m.getId(), m);
		}
	}

	public String getTrelloId(String memberId){
		return table.get(memberId).getTrelloId();
	}

	public Member getMember(String trelloId){
		for(Entry<String,Member> e : table.entrySet()){
			if( e.getValue().getTrelloId().equals(trelloId) ){
				return e.getValue();
			}
		}
		return null;
	}

	public String getMemberId(String trelloId){
		Member m = getMember(trelloId);
		return m != null ? m.getId() : null;
	}
}
