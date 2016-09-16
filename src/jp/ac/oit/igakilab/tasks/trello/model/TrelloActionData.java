package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class TrelloActionData {
	static String KEY_SEPARATOR = ".";

	public static void main(String[] args){
		Document doc = Document.parse("{ \"textData\" : { \"emoji\" : {  } }, \"dateLastEdited\" : \"2016-09-16T02:24:57.069Z\", \"text\" : \"こうどくとったで コメント変えた\", \"list\" : { \"name\" : \"list3-1\", \"id\" : \"57d3f5ebdda362ae59793c0c\" }, \"card\" : { \"idShort\" : 9, \"name\" : \"task9\", \"id\" : \"57d8c403148aadf180a707d7\", \"shortLink\" : \"OcHflP2B\" }, \"board\" : { \"name\" : \"actions-test\", \"id\" : \"57d3f5cac2c3720549a9b8c1\", \"shortLink\" : \"4GHyumBA\" } }");
		Map<String,String> data = new HashMap<String,String>();
		parseData(data, "", doc);

		for(Entry<String,String> entry : data.entrySet()){
			System.out.format("%s: %s\n", entry.getKey(), entry.getValue());
		}
	}

	public static void parseData(Map<String,String> map, String keyHead, Document doc){
		for(Entry<String,Object> entry : doc.entrySet()){
			String thiskey = keyHead + entry.getKey();
			//in document object
			if( entry.getValue() instanceof Document ){
				parseData(map, thiskey + TrelloActionData.KEY_SEPARATOR, (Document)entry.getValue());

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
}
