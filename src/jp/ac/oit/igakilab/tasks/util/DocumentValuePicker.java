package jp.ac.oit.igakilab.tasks.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

public class DocumentValuePicker {
	public static boolean DEFAULT_PRINT_STACK_TRACE = true;

	private Document doc;
	private boolean stackTraceEnabled;

	public DocumentValuePicker(Document doc){
		this.doc = doc;
		this.stackTraceEnabled = DEFAULT_PRINT_STACK_TRACE;
	}

	public Boolean getBoolean(String key, boolean defaultValue){
		boolean val = defaultValue;
		if( doc.containsKey(key) ){
			try{
				val = doc.getBoolean(key);
			}catch(ClassCastException e0){
				if( stackTraceEnabled ) e0.printStackTrace();
			}
		}
		return val;
	}

	public String getString(String key, String defaultValue){
		String val = defaultValue;
		if( doc.containsKey(key) ){
			try{
				val = doc.getString(key);
			}catch(ClassCastException e0){
				if( stackTraceEnabled ) e0.printStackTrace();
			}
		}
		return val;
	}

	public Date getDate(String key, Date defaultValue){
		Date val = defaultValue;
		if( doc.containsKey(key) ){
			try{
				val = doc.getDate(key);
			}catch(ClassCastException e0){
				if( stackTraceEnabled ) e0.printStackTrace();
			}
		}
		return val;
	}

	@SuppressWarnings("unchecked")
	public List<Object> getArray(String key){
		if( doc.containsKey(key) ){
			Object obj = doc.get(key);
			try{
				List<Object> list = (List<Object>)obj;

				return list;
			}catch(ClassCastException e0){
				if( stackTraceEnabled ) e0.printStackTrace();
				return new ArrayList<Object>();
			}
		}
		return new ArrayList<Object>();
	}

	public List<Document> getDocumentArray(String key){
		List<Document> array = new ArrayList<Document>();

		for(Object obj : getArray(key)){
			try{
				Document tmp = (Document)obj;
				array.add(tmp);
			}catch(ClassCastException e0){
				if( stackTraceEnabled ) e0.printStackTrace();
			}
		}

		return array;
	}

	public List<String> getStringArray(String key){
		List<String> array = new ArrayList<String>();

		for(Object obj : getArray(key)){
			try{
				String tmp = (String)obj;
				array.add(tmp);
			}catch(ClassCastException e0){
				if( stackTraceEnabled ) e0.printStackTrace();
			}
		}

		return array;
	}
}
