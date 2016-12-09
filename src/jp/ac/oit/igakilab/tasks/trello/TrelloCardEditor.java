package jp.ac.oit.igakilab.tasks.trello;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApiConnectionFailedException;

public class TrelloCardEditor {
	private TrelloApi<Object> client;

	public TrelloCardEditor(TrelloApi<Object> api){
		this.client = api;
	}

	String commaSeparated(List<String> strings){
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<strings.size(); i++){
			buf.append(strings.get(i));
			if( i < strings.size() - 1 ){
				buf.append(",");
			}
		}
		return buf.toString();
	}

	public boolean addMember(String cardId, String trelloMemberId){
		String url = "/1/cards/" + cardId + "/idMembers";
		Map<String,String> params = new HashMap<String,String>();
		params.put("value", trelloMemberId);

		try{
			client.post(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return false;
		}

		return true;
	}

	public boolean setDueDate(String cardId, Date date){
		String url = "/1/cards/" + cardId + "/due";
		Map<String,String> params = new HashMap<String,String>();

		TrelloDateFormat df = new TrelloDateFormat();
		String dateStr = (date != null ? df.format(date) : null);
		params.put("value", dateStr);

		try{
			client.put(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return false;
		}

		return true;
	}

	public boolean setDueComplete(String cardId, boolean complete){
		String url = "/1/cards/" + cardId + "/dueComplete";
		Map<String, String> params = new HashMap<String,String>();

		params.put("value", (complete ? "true" : "false"));

		try{
			client.put(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return false;
		}

		return true;
	}

	public boolean setDueAndMembers(String cardId, Date due, List<String> trelloMemberIds){
		return setDueAndMembers(cardId, due, trelloMemberIds, false);
	}

	public boolean setDueAndMembers(String cardId, Date due, List<String> trelloMemberIds, boolean dueCompleteRemove){
		String url = "/1/cards/" + cardId;
		Map<String,String> params = new HashMap<String,String>();

		if( due != null ){
			TrelloDateFormat df = new TrelloDateFormat();
			params.put("due", df.format(due));
		}

		if( dueCompleteRemove ){
			params.put("dueComplete", "false");
		}

		if( trelloMemberIds != null && trelloMemberIds.size() > 0 ){
			params.put("idMembers", commaSeparated(trelloMemberIds));
		}

		try{
			client.put(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return false;
		}

		return true;
	}

	public boolean clearDueAndMembers(String cardId){
		String url = "/1/cards/" + cardId;
		Map<String,String> params = new HashMap<String,String>();

		params.put("due", "null");
		params.put("idMembers", "");

		try{
			client.post(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return false;
		}

		return true;
	}
}