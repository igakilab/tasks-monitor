package jp.ac.oit.igakilab.tasks.test;

import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.DBEditException;
import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TestMembersDB {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MembersDB mdb = new MembersDB(client);

		Member mdata = new Member("koike");
		mdata.setName("koike");
		mdata.setSlackId("koike");
		mdata.setTrelloId(
			getTrelloIdByUserName("user93461510"));

		try{
			mdb.addMember(mdata, new MemberDocumentConverter());
		}catch(DBEditException e0){
			System.out.println(e0.getMessage());
		}

		mdb.getAllMemberList(new MemberDocumentConverter()).forEach(
			(member) -> System.out.format("%s %s %s %s\n",
				member.getId(), member.getName(),
				member.getTrelloId(), member.getSlackId())
		);
	}

	public static String getTrelloIdByUserName(String userName){
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> trello = TasksTrelloClientBuilder.createApiClient();
		Object reply = trello.rget("/1/members/" + userName).getData();
		if( reply != null ){
			JSONObject obj = (JSONObject)reply;
			return (String)obj.get("id");
		}
		System.err.println("GETTRELLOID: FAILED");
		return null;
	}
}
