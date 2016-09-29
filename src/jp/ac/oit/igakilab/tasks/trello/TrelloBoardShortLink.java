package jp.ac.oit.igakilab.tasks.trello;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.http.TrelloApi;

public class TrelloBoardShortLink {
	public static void main(String[] args){
		AppProperties.global.setIfNotHasValue("tasks.trello.key",
				"67ad72d3feb45f7a0a0b3c8e1467ac0b");
		AppProperties.global.setIfNotHasValue("tasks.trello.token",
				"268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
		TrelloApi api = TasksTrelloClientBuilder.createApiClient();
		String shortLink = "4GHyumBA";
		String id = getBoardIdByShortLink(api, "4GHyumBA");
		System.out.println(shortLink + "->" + id);
	}

	//ショートリンクからボードIDを取得します。
	//失敗した場合はnullを返却します
	public static String getBoardIdByShortLink(TrelloApi api, String shortLink){
		Object obj = api.get("/1/boards/" + shortLink);
		if( obj != null ){
			String id = null;
			try{
				JSONObject json = (JSONObject)obj;
				id = (String)json.get("id");
			}catch(ClassCastException e0){
				return null;
			}
			return id;
		}else{
			return null;
		}
	}
}
