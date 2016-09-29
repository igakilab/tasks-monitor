package jp.ac.oit.igakilab.tasks.dwr.admin;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.DBEditException;
import jp.ac.oit.igakilab.tasks.db.MembersDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.MemberDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.ExcuteFailedException;
import jp.ac.oit.igakilab.tasks.dwr.forms.MemberForm;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.TrelloMemberInfo;

public class AdminMemberEditor {
	public static String ERR_INVALID_DATA = "データが不正です";
	public static String ERR_INVALID_MEMBERID = "このメンバーidは使用できません";

	public List<MemberForm> getAllMembersList(){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MembersDB mdb = new MembersDB(client);

		List<Member> members = mdb.getAllMemberList(new MemberDocumentConverter());
		List<MemberForm> forms = new ArrayList<MemberForm>();
		members.forEach((data) -> forms.add(MemberForm.getInstance(data)));

		client.close();
		return forms;
	}

	public boolean canAddMemberId(String mid){
		if( mid != null && mid.length() > 0 ){
			MongoClient client = TasksMongoClientBuilder.createClient();
			MembersDB mdb = new MembersDB(client);

			boolean result = !mdb.memberIdExists(mid);

			client.close();
			return result;
		}
		return false;
	}

	public void addMember(MemberForm form)
	throws ExcuteFailedException{
		if( form != null ){
			//データ変換
			Member member = MemberForm.convertToMember(form);
			if( member == null ) throw new ExcuteFailedException(ERR_INVALID_DATA);

			//DBのオープン
			MongoClient client = TasksMongoClientBuilder.createClient();
			MembersDB mdb = new MembersDB(client);

			//登録可能チェック
			if( !canAddMemberId(member.getId()) ){
				throw new ExcuteFailedException(ERR_INVALID_MEMBERID);
			}

			//登録動作
			try{
				mdb.addMember(member, new MemberDocumentConverter());
			}catch(DBEditException e0){
				throw new ExcuteFailedException(e0.getMessage());
			}finally{
				client.close();
			}
		}else{
			throw new ExcuteFailedException(ERR_INVALID_DATA);
		}
	}

	public void updateMember(MemberForm form)
	throws ExcuteFailedException{
		if( form != null ){
			//データ変換
			Member member = MemberForm.convertToMember(form);
			if( member == null ) throw new ExcuteFailedException(ERR_INVALID_DATA);

			//DBのオープン
			MongoClient client = TasksMongoClientBuilder.createClient();
			MembersDB mdb = new MembersDB(client);

			//更新動作
			System.out.println("UPDATE MEMBER: " + member.getId());
			try{
				mdb.updateMember(member, new MemberDocumentConverter());
			}catch(DBEditException e0){
				if( e0.getType() == DBEditException.ID_NOTDEFINED ){
					throw new ExcuteFailedException(ERR_INVALID_DATA);
				}else if( e0.getType() == DBEditException.ID_NOT_REGISTED ){
					throw new ExcuteFailedException(ERR_INVALID_MEMBERID);
				}else{
					throw new ExcuteFailedException(e0.getMessage());
				}
			}finally{
				client.close();
			}
		}else{
			throw new ExcuteFailedException(ERR_INVALID_DATA);
		}
	}

	public void deleteMemberById(String mid)
	throws ExcuteFailedException{
		if( mid != null ){
			//DBオープン
			MongoClient client = TasksMongoClientBuilder.createClient();
			MembersDB mdb = new MembersDB(client);

			//削除動作
			try{
				mdb.deleteMemberById(mid);
			}catch(DBEditException e0){
				if( e0.getType() == DBEditException.ID_NOTDEFINED ){
					throw new ExcuteFailedException(ERR_INVALID_DATA);
				}else if( e0.getType() == DBEditException.ID_NOT_REGISTED ){
					throw new ExcuteFailedException(ERR_INVALID_MEMBERID);
				}else{
					throw new ExcuteFailedException(e0.getMessage());
				}
			}finally{
				client.close();
			}
		}else{
			throw new ExcuteFailedException(ERR_INVALID_DATA);
		}
	}

	public String getTrelloIdByTrelloUserName(String username){
		TrelloApi api = TasksTrelloClientBuilder.createApiClient();
		TrelloMemberInfo trello = new TrelloMemberInfo(api);

		String userId = trello.getUserIdByUserName(username);
		return userId;
	}
}
