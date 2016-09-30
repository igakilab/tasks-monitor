package jp.ac.oit.igakilab.tasks.trello;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TrelloMemberInfo {
	private TrelloApi<Object> client;

	public TrelloMemberInfo(TrelloApi<Object> api){
		client = api;
	}

	public String getUserIdByUserName(String username){
		Object reply = client.rget("/1/members/" + username).getData();

		if( reply != null ){
			JSONObject obj = (JSONObject)reply;
			return (String)obj.get("id");
		}else{
			return null;
		}
	}
}
