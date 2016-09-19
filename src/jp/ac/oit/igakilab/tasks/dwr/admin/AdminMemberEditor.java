package jp.ac.oit.igakilab.tasks.dwr.admin;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.MemberForm;
import jp.ac.oit.igakilab.tasks.members.Member;

public class AdminMemberEditor {
	public List<MemberForm> getAllMemberList(){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MembersDB mdb = new MembersDB(client);

		List<Member> members = mdb.getAllMemberList(new MemberDocumentConverter());
		List<MemberForm> forms = new ArrayList<MemberForm>();
		members.forEach((data) -> forms.add(MemberForm.getInstance(data)));

		return forms;
	}
}
