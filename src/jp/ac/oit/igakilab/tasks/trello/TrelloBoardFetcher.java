package jp.ac.oit.igakilab.tasks.trello;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApiConnectionFailedException;

public class TrelloBoardFetcher {
	public static void main(String[] args){
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api);
		JSONObject obj = fetcher.fetch("57ab33677fd33ec535cc4f28");
		System.out.println(obj.toJSONString());
	}

	private TrelloApi<Object> api;

	public TrelloBoardFetcher(TrelloApi<Object> api){
		this.api = api;
	}

	/*private TrelloBoard buildTrelloBoard(JSONObject boardData){

	}*/

	public JSONObject fetch(String boardId){
		String url = "/1/boards/" + boardId;
		Map<String,String> params = new HashMap<String,String>();
		params.put("cards", "all");
		params.put("lists", "all");

		Object res = null;
		try{
			res = api.get(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return null;
		}
		return res instanceof JSONObject ? (JSONObject)res : null;
	}
}
