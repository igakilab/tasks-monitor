package jp.ac.oit.igakilab.tasks.util;

import java.util.Date;

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
}
