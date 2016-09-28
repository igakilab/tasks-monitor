package jp.ac.oit.igakilab.tasks.trello;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.http.TrelloApi;

public class TrelloMemberInfo {
	private TrelloApi client;

	public TrelloMemberInfo(TrelloApi api){
		client = api;
	}

	public String getUserIdByUserName(String username){
		Object reply = client.get("/1/members/" + username);

		if( reply != null ){
			JSONObject obj = (JSONObject)reply;
			return (String)obj.get("id");
		}else{
			return null;
		}
	}
}
