package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class DocumentTrelloActionParser {
	public static void main(String[] args){
		Document doc = Document.parse("{ \"textData\" : { \"emoji\" : {  } }, \"dateLastEdited\" : \"2016-09-16T02:24:57.069Z\", \"text\" : \"こうどくとったで コメント変えた\", \"list\" : { \"name\" : \"list3-1\", \"id\" : \"57d3f5ebdda362ae59793c0c\" }, \"card\" : { \"idShort\" : 9, \"name\" : \"task9\", \"id\" : \"57d8c403148aadf180a707d7\", \"shortLink\" : \"OcHflP2B\" }, \"board\" : { \"name\" : \"actions-test\", \"id\" : \"57d3f5cac2c3720549a9b8c1\", \"shortLink\" : \"4GHyumBA\" } }");
		Map<String,String> data = new HashMap<String,String>();
		parseActionData(data, "", doc);

		for(Entry<String,String> entry : data.entrySet()){
			System.out.format("%s: %s\n", entry.getKey(), entry.getValue());
		}

		/*
		 * 空間: d:50, w:55, h:63
		 * 太ももを考慮した椅子の高さ: 48
		 */
	}


	public static void parseActionData(Map<String,String> map, String keyHead, Document doc){
		for(Entry<String,Object> entry : doc.entrySet()){
			String thiskey = keyHead + entry.getKey();
			//in document object
			if( entry.getValue() instanceof Document ){
				parseActionData(map, thiskey + TrelloActionData.KEY_SEPARATOR, (Document)entry.getValue());

			//in other data types
			}else if( entry.getValue() instanceof String ){
				map.put(thiskey, (String)entry.getValue());
			}else if( entry.getValue() instanceof Integer ){
				map.put(thiskey, String.valueOf((Integer)entry.getValue()));

			//in unknown data type
			}else{
				String uskey = TrelloActionData.KEY_SEPARATOR + "unsupported";
				if( !map.containsKey(uskey) ) map.put(uskey, "");
				map.put(uskey, map.get(uskey) + thiskey + ";");
			}
		}
	}

	public static TrelloAction parse(Document doc){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		TrelloAction action = new TrelloAction();

		//parse id
		action.setId(doc.getString("id"));

		//parse type
		if( !doc.containsKey("type") ) return null;
		action.setType(doc.getString("type"));
		action.setTargetType(TrelloAction.parseTargetType(action.getType()));
		action.setActionType(TrelloAction.parseActionType(action.getType()));

		//parse date
		if( !doc.containsKey("date") ) return null;
		try{ action.setDate(df.parse(doc.getString("date"))); }
		catch(ParseException e0){ return null; }

		//parse data
		Map<String,String> data = new HashMap<String,String>();
		parseActionData(data, "", (Document)doc.get("data"));
		TrelloActionData tadata = new TrelloActionData(data);
		action.setData(tadata);

		//parse memberCreator
		action.setMemberCreatorId(((Document)doc.get("memberCreator")).getString("id"));

		//set raw text
		action.setRawText(doc.toJson());

		return action;
	}
}
