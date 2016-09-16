package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class DocumentTrelloActionParser {

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
		Map<String,String> data = new HashMap<String,String>();
		parseActionData(data, "", (Document)doc.get("data"));
		action.setData(data);

		//parse memberCreator
		action.setMemberCreatorId(((Document)doc.get("memberCreator")).getString("id"));

		//set raw text
		action.setRawText(doc.toJson());

		return action;
	}

	public DocumentTrelloActionParser(){};
}
