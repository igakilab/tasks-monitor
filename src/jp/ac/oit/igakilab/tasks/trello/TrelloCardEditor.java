package jp.ac.oit.igakilab.tasks.trello;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApiConnectionFailedException;

public class TrelloCardEditor {
	private TrelloApi<Object> client;

	public TrelloCardEditor(TrelloApi<Object> api){
		this.client = api;
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
}
