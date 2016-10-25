package jp.ac.oit.igakilab.tasks.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONObjectValuePicker {
	private JSONObject obj;

	public JSONObjectValuePicker(JSONObject obj){
		this.obj = obj != null ? obj : new JSONObject();
	}

	public JSONObjectValuePicker(Object obj){
		this(obj instanceof JSONObject ? (JSONObject)obj : null);
	}

	public String getString(String key){
		Object val = obj.get(key);
		return val instanceof String ? (String)val : null;
	}

	public boolean getBoolean(String key){
		Object val = obj.get(key);
		return val instanceof Boolean ? (Boolean)val : null;
	}

	public JSONArray getJSONArray(String key){
		Object ary = obj.get(key);
		return ary instanceof JSONArray ? (JSONArray)ary : new JSONArray();
	}
}
