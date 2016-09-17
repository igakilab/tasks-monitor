package jp.ac.oit.igakilab.tasks.test;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;
import jp.ac.oit.igakilab.tasks.members.Member;

public class TestMembersDB {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MembersDB mdb = new MembersDB(client);

		Member member = new Member("koike");
		member.setName("koike");
		member.setSlackId("koike");

		mdb.addMember(member, new MemberDocumentConverter());

	}
}
