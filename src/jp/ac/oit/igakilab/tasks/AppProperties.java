package jp.ac.oit.igakilab.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AppProperties {
	private static String parentKeyRegex(String parentKey){
		if( parentKey == null ) return null;
		return "^" + parentKey.replaceAll("\\.", "\\\\.") + "\\..*";
	}

	public static final AppProperties global = new AppProperties();

	public static boolean globalIsValid(){
		return global != null;
	}

	public static String globalGet(String key, String defaultValue){
		return globalIsValid() ?
			global.get(key, defaultValue) : null;
	}

	public static String globalGet(String key){
		return globalGet(key, null);
	}

	public static void globalSet(String key, String value){
		if( globalIsValid() ){
			global.set(key, value);
		}
	}


	private Map<String,String> properties;

	public AppProperties(){
		properties = new HashMap<String,String>();
	}

	public void clear(){
		properties.clear();
	}

	public boolean containsKey(String key){
		return properties.containsKey(key);
	}

	public boolean hasValue(String key){
		return containsKey(key) && get(key).length() > 0;
	}

	public String get(String key, String defaultValue){
		String value = properties.get(key);
		return value != null ? value : defaultValue;
	}

	public String get(String key){
		return get(key, null);
	}

	public void set(String key, String value){
		properties.put(key, value);
	}

	public boolean setIfNotHasValue(String key, String value){
		if( !hasValue(key) ){
			set(key, value);
			return true;
		}else{
			return false;
		}
	}

	public Map<String,String> getChildProperties(String parentKey){
		String regex = parentKeyRegex(parentKey);
		Map<String,String> childs = new HashMap<String,String>();
		for(Entry<String,String> entry : properties.entrySet()){
			if( regex != null && entry.getKey().matches(regex) ){
				childs.put(entry.getKey(), entry.getValue());
			}
		}
		return childs;
	}

	public void importPropertiesMap(Map<String,String> map, String filter){
		String regex = parentKeyRegex(filter);
		for(Entry<String,String> entry : map.entrySet()){
			if( regex == null || entry.getKey().matches(regex) ){
				properties.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public void importPropertiesMap(Map<String,String> map){
		importPropertiesMap(map, null);
	}

	public Map<String,String> getProperties(){
		return properties;
	}
}
