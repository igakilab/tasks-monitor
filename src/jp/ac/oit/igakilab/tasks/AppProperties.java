package jp.ac.oit.igakilab.tasks;

import java.util.HashMap;
import java.util.Map;

public class AppProperties {
	static Map<String, String> properties = null;

	public static void init(){
		properties = new HashMap<String, String>();
	}

	public static void loadSystemProperties(){
		for(Object keyObj : System.getProperties().keySet()){
			try{
				String key = (String)keyObj;
				set(key, System.getProperty(key));
			}catch(ClassCastException e0){}
		}
	}

	public static String get(String key){
		if( properties != null ){
			return properties.get(key);
		}else{
			return null;
		}
	}

	public static String get(String key, String defaultValue){
		String value = get(key);
		return value != null ? value : defaultValue;
	}

	public static void set(String key, String value){
		if( properties != null ){
			properties.put(key, value);
		}
	}

	public static Map<String,String> getChildProperties(String upperKey){
		Map<String,String> childs = new HashMap<String,String>();
		String regex = "^" + upperKey.replaceAll("\\.", "\\\\.") + "\\..*";
		if( properties != null ){
			for(Map.Entry<String,String> entry : properties.entrySet()){
				if( entry.getKey().matches(regex) ){
					childs.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return childs;
	}

	public static Map<String,String> getPropertiesMap(){
		return properties;
	}
}
