package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class TrelloAction {
	protected String type;
	protected Date date;
	protected Map<String,Object> data;
	protected Map<String,Object> memberCreator;
	protected String rawText;

	public static TrelloAction getInstance(Document doc){
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
		if( doc.containsKey("data") ){
			for(Entry<String,Object> entry : ((Document)doc.get("data")).entrySet()){
				action.data.put(entry.getKey(), entry.getValue());
			}
			if( action.getType().es)
		}
		
		//parse memberCreator
		if( doc.containsKey("memberCreator") ){
			
		}
		
	}
	
	public TrelloAction(){
		init();
	}

	public void init(){
		type = null;
		date = null;
		data = new HashMap<String,String>();
		memberCreator = new HashMap<String,String>();
		rawText = null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public Map<String, String> getMemberCreator() {
		return memberCreator;
	}

	public void setMemberCreator(Map<String, String> memberCreator) {
		this.memberCreator = memberCreator;
	}

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}
}
