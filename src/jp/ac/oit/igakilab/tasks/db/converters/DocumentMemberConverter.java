package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.db.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.DocumentValuePicker;
import jp.ac.oit.igakilab.tasks.members.Member;

public class DocumentMemberConverter
implements DocumentConverter<Member>{
	public Member parse(Document doc){
		if( doc.containsKey("id") ){
			Member m = new Member(doc.getString("id"));
			DocumentValuePicker picker = new DocumentValuePicker(doc);
			m.setName(picker.getString("name", ""));
			m.setTrelloId(picker.getString("trelloId", null));
			m.setSlackId(picker.getString("slackId", null));
			m.setAdmin(picker.getBoolean("isAdmin", false));
			return m;
		}
		return null;
	}

	public Document convert(Member member){
		Document doc = new Document();
		doc.append("id", member.getId());
		doc.append("name", member.getName());
		if( member.getTrelloId() != null )
			doc.append("trelloId", member.getTrelloId());
		if( member.getSlackId() != null )
			doc.append("slackId", member.getSlackId());
		if( member.isAdmin() )
			doc.append("isAdmin", true);
		return doc;
	}
}
