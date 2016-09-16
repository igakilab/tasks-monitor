package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.HashMap;
import java.util.Map;

public class TrelloActionData extends HashMap<String,String>{
	static final String KEY_SEPARATOR = ".";

	private static String parentKeyRegex(String parentKey){
		if( parentKey == null ) return null;
		return "^" + parentKey.replaceAll("\\.", "\\\\.") + "\\..*";
	}

	public TrelloActionData(){}

	public TrelloActionData(Map<String,String> map){
		super(map);
	}

	public Map<String,String> getChildMap(String classKey){
		String regex = parentKeyRegex(classKey);
		Map<String,String> childs = new HashMap<String,String>();
		for(Entry<String,String> entry : entrySet()){
			if( entry.getKey().matches(regex) ){
				childs.put(entry.getKey().replaceFirst(regex, ""), entry.getValue());
			}
		}
		return childs;
	}
}
