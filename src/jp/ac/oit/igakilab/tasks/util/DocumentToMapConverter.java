package jp.ac.oit.igakilab.tasks.util;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class DocumentToMapConverter {
	public static void main(String[] args){
		Document doc = Document.parse("{ \"textData\" : { \"emoji\" : null}, \"dateLastEdited\" : \"2016-09-16T02:24:57.069Z\", \"text\" : \"こうどくとったで コメント変えた\", \"list\" : { \"name\" : \"list3-1\", \"id\" : \"57d3f5ebdda362ae59793c0c\" }, \"card\" : { \"idShort\" : 9, \"name\" : \"task9\", \"id\" : \"57d8c403148aadf180a707d7\", \"shortLink\" : \"OcHflP2B\" }, \"board\" : { \"name\" : \"actions-test\", \"id\" : \"57d3f5cac2c3720549a9b8c1\", \"shortLink\" : \"4GHyumBA\" } }");

		Map<String,Value> map = convertToMap(doc);
		printMap(map, System.out);

		System.out.println("---");
		Map<String,String> smap = convertToStringMap(doc);
		smap.forEach((key, value) -> System.out.println(key + ": " + value));

		System.out.println("---");
		Map<String,String> card = filterChildValues(smap, "card");
		card.forEach((key, value) -> System.out.println(key + ": " + value));

	}


	public static final int STRING = 101;
	public static final int INTEGER = 102;
	public static final int LONG = 103;
	public static final int DOUBLE = 104;
	public static final int BOOLEAN = 105;
	public static final int DATE = 106;
	public static final int DOCUMENT = 201;
	public static final int NULL = 202;
	public static final int UNKNOWN = 291;

	public static class Value{
		public static final int STRING = DocumentToMapConverter.STRING;
		public static final int INTEGER = DocumentToMapConverter.INTEGER;
		public static final int LONG = DocumentToMapConverter.LONG;
		public static final int DOUBLE = DocumentToMapConverter.DOUBLE;
		public static final int BOOLEAN = DocumentToMapConverter.BOOLEAN;
		public static final int DATE = DocumentToMapConverter.DATE;
		public static final int NULL = DocumentToMapConverter.NULL;
		public static final int UNKNOWN = DocumentToMapConverter.UNKNOWN;

		private Object value;
		private int type;

		public Value(int t0, Object v0){
			value = v0;
			type = t0;
		}

		public int getType(){
			return type;
		}

		public Object getValue(){
			return value;
		}

		public boolean isString(){ return type == STRING; }
		public boolean isInteger(){ return type == INTEGER; }
		public boolean isLong(){ return type == LONG; }
		public boolean isDouble(){ return type == DOUBLE; }
		public boolean isBoolean(){ return type == BOOLEAN; }
		public boolean isDate(){ return type == DATE; }

		public String toString(){
			String typeLabel = (
				type == NULL ? "NULL" :
				type == STRING ? "STRING" :
				type == INTEGER ? "INTEGER" :
				type == LONG ? "LONG" :
				type == DOUBLE ? "DOUBLE" :
				type == BOOLEAN ? "BOOLEAN" :
				type == DATE ? "DATE" :
				type == DOCUMENT ? "DOCUMENT" : "UNKNOWN");
			return String.format("%s %s", typeLabel,
				(type != NULL) ? value.toString() : "null");
		}
	}

	private static String keySeparator(){
		return ".";
	}

	private static String upperKeyMatchRegex(String upperKey){
		return "^" + upperKey.replaceAll("\\.", "\\\\.") + "\\..*";
	}

	private static String upperKeyReplaceRegex(String upperKey){
		return "^" + upperKey.replaceAll("\\.", "\\\\.") + "\\.";
	}



	private static int getDataType(Object value){
		if( value == null ) return NULL;
		if( value instanceof String ){
			return STRING;
		}else
		if( value instanceof Integer ){
			return INTEGER;
		}else
		if( value instanceof Long ){
			return LONG;
		}else
		if( value instanceof Double ){
			return DOUBLE;
		}else
		if( value instanceof Boolean ){
			return BOOLEAN;
		}else
		if( value instanceof Date ){
			return DATE;
		}else
		if( value instanceof Document ){
			return DOCUMENT;
		}
		return UNKNOWN;
	}

	private static void parseDocument(Map<String,Value> map, String keyHead, Document doc){
		for(Entry<String,Object> entry : doc.entrySet()){
			String thiskey = keyHead + entry.getKey();
			int dataType = getDataType(entry.getValue());

			if( dataType == DOCUMENT ){
				parseDocument(map, thiskey + keySeparator(), (Document)entry.getValue());
			}else{
				map.put(thiskey, new Value(dataType, entry.getValue()));
			}
		}
	}

	public static Map<String,Value> convertToMap(Document doc){
		Map<String,Value> map = new HashMap<String,Value>();
		parseDocument(map, "", doc);
		return map;
	}

	public static Map<String,String> convertToStringMap(Document doc){
		Map<String,String> smap = new HashMap<String,String>();

		for(Entry<String,Value> entry : convertToMap(doc).entrySet()){
			Value val = entry.getValue();
			String strValue = null;

			if( val.isString() ){
				strValue = (String)val.getValue();
			}else if( val.isInteger() ){
				strValue = String.valueOf((Integer)val.getValue());
			}else if( val.isLong() ){
				strValue = String.valueOf((Long)val.getValue());
			}else if( val.isDouble() ){
				strValue = String.valueOf((Double)val.getValue());
			}else if( val.isBoolean() ){
				strValue = String.valueOf((Boolean)val.getValue());
			}else if( val.getType() == NULL ){
				strValue = "null";
			}

			if( strValue != null ){
				smap.put(entry.getKey(), strValue);
			}else{
				String uskey = keySeparator() + "unsupported";
				if( !smap.containsKey(uskey) ) smap.put(uskey, "");
				smap.put(uskey, smap.get(uskey) + entry.getKey());
			}
		}

		return smap;
	}

	public static void printMap(Map<String,Value> map, PrintStream stream){
		for(Entry<String,Value> entry : map.entrySet()){
			stream.println(entry.getKey() + ": " + entry.getValue().toString());
		}
	}

	public static <V> Map<String,V> filterChildValues(Map<String,V> map, String parentKey){
		String regex = upperKeyMatchRegex(parentKey);
		String rRegex = upperKeyReplaceRegex(parentKey);
		Map<String,V> childs = new HashMap<String,V>();

		for(Entry<String,V> entry : map.entrySet()){
			if( entry.getKey().matches(regex) ){
				String ckey = entry.getKey().replaceFirst(rRegex, "");
				childs.put(ckey, entry.getValue());
			}
		}

		return childs;
	}
}
