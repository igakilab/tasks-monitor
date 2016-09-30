package jp.ac.oit.igakilab.tasks.trello;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.trello.TrelloApi.Parameters;

public class TrelloBoardDataFetcher {
	static String FIELDS = "name,desc,closed,shortLink";

	public static void main(String[] args){
		AppProperties.global.setIfNotHasValue("tasks.trello.key",
				"67ad72d3feb45f7a0a0b3c8e1467ac0b");
		AppProperties.global.setIfNotHasValue("tasks.trello.token",
				"268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
		TrelloApi api = TasksTrelloClientBuilder.createApiClient();
		String shortLink = "4GHyumBA";
		TrelloBoardDataFetcher f = new TrelloBoardDataFetcher(api);
		TrelloBoardData board = f.getTrelloBoardData(shortLink);
		System.out.println(shortLink + " -> " + board.toString());
		System.out.println("members: " + board.getMemberIds());
	}

	private TrelloApi client;

	public TrelloBoardDataFetcher(TrelloApi api){
		client = api;
	}

	private TrelloBoardData toTrelloBoardData(Object jsonObject){
		try{
			JSONObject data = (JSONObject)jsonObject;
			TrelloBoardData board = new TrelloBoardData();
			board.setId((String)data.get("id"));
			board.setName((String)data.get("name"));
			board.setDesc((String)data.get("desc"));
			board.setShortLink((String)data.get("shortLink"));
			board.setClosed((boolean)data.get("closed"));

			JSONArray members = (JSONArray)data.get("members");
			for(Object mt : members){
				JSONObject mo = (JSONObject)mt;
				board.addMemberId((String)mo.get("id"));
			}

			return board;
		}catch(ClassCastException e0){
			return null;
		}
	}

	public TrelloBoardData getTrelloBoardData(String boardId){
		Parameters params = new Parameters();
		params.setParameter("fields", FIELDS);
		params.setParameter("members", "all");

		Object res = client.get("/1/boards/" + boardId, params);
		return toTrelloBoardData(res);
	}
}
