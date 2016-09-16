package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class DocumentTrelloActionParser {
	static int DATA_BOARD = 101;
	static int DATA_LIST = 102;
	static int DATA_CARD = 103;
	static int DATA_UNKNOWN = 104;

	public static int getDataType(String type){
		if( type.equals("createBoard") ||
			type.equals("updateBoard") ||
			type.equals("addMemberToBoard") ||
			type.equals("removeMemberFromBoard") ){
			return DATA_BOARD;
		}else
		if( type.equals("createList") ||
			type.equals("updateList") ){
			return DATA_LIST;
		}else
		if( type.equals("createCard") ||
			type.equals("updateCard") ||
			type.equals("addMemberToCard") ||
			type.equals("removeMemberFromCard") ||
			type.equals("deleteCard") ){
			return DATA_CARD;
		}
		return DATA_UNKNOWN;
	}

	public static boolean isMemberData(String type){
		return (
			type.equals("addMemberToBoard") ||
			type.equals("removeMemberFromBoard") ||
			type.equals("addMemberToCard") ||
			type.equals("removeMemberFromCard") );
	}

	public static Map<String,String> parseActionData(String type, Document doc){
		int dataType = getDataType(type);
		boolean isMember = isMemberData(type);
		Map<String,String> data = new HashMap<String,String>();

		Document target = null;
		if( dataType == DATA_CARD ){
			target = (Document) ((Document)doc.get("data")).get("card");
		}else if( dataType == DATA_LIST ){
			target = (Document) ((Document)doc.get("data")).get("list");
		}else if( dataType == DATA_BOARD ){
			target = (Document) ((Document)doc.get("data")).get("board");
		}

		if( target != null ){
			for(Entry<String,Object> entry : target.entrySet()){
				try{
					String val = (String)entry.getValue();
					data.put(entry.getKey(), val);
				}catch(ClassCastException e0){}
			}
		}

		if( isMember ){
			try{
				data.put("idMember", target.getString("idMember"));
			}catch(ClassCastException e0){}
		}

		return data;
	}

	public static TrelloAction getTrelloActionInstance(Document doc){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		TrelloAction action = new TrelloAction();

		//parse type
		if( !doc.containsKey("type") ) return null;
		action.setType(doc.getString("type"));

		//parse date
		if( !doc.containsKey("date") ) return null;
		try{ action.setDate(df.parse(doc.getString("date"))); }
		catch(ParseException e0){ return null; }

		//parse data
		action.setData(parseActionData(action.getType(), doc));

		//parse memberCreator
		if( doc.containsKey("memberCreator") ){
			for(Entry<String,Object> entry : ((Document)doc.get("memberCreator")).entrySet()){
				try{
					action.getMemberCreator().put(
						entry.getKey(), (String)entry.getValue());
				}catch(ClassCastException e0){}
			}
		}

		//set raw text
		action.setRawText(doc.toJson());

		return action;
	}

	public DocumentTrelloActionParser(){};
}
