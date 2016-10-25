package jp.ac.oit.igakilab.tasks.trello;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApiConnectionFailedException;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.util.JSONObjectValuePicker;

public class TrelloBoardFetcher {
	public static void main(String[] args){
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api);
		TrelloBoard board = fetcher.fetch("57ab33677fd33ec535cc4f28");
		System.out.println(board.toString());
		board.printListsAndCards(System.out);
	}

	private TrelloApi<Object> api;

	public TrelloBoardFetcher(TrelloApi<Object> api){
		this.api = api;
	}

	private TrelloBoard buildTrelloBoard(JSONObject boardData){
		//各インスタンスの準備
		JSONObjectValuePicker picker = new JSONObjectValuePicker(boardData);
		TrelloBoard board = new TrelloBoard();

		//ボードのデータの解析
		board.setId(picker.getString("id"));
		board.setName(picker.getString("name"));
		board.setDesc(picker.getString("desc"));
		board.setShortLink(picker.getString("shortUrl"));
		board.setClosed(picker.getBoolean("closed"));

		//リストの解析
		JSONArray lists = picker.getJSONArray("lists");
		for(Object objList : lists){
			JSONObjectValuePicker pickerl = new JSONObjectValuePicker(objList);
			TrelloList list = new TrelloList();
			list.setId(pickerl.getString("id"));
			list.setName(pickerl.getString("name"));
			list.setClosed(pickerl.getBoolean("closed"));
			board.addList(list);
		}

		//カードの解析
		TrelloDateFormat df = new TrelloDateFormat();
		JSONArray cards = picker.getJSONArray("cards");
		for(Object objCard : cards){
			JSONObjectValuePicker pickerc = new JSONObjectValuePicker(objCard);
			TrelloCard card = new TrelloCard();
			card.setId(pickerc.getString("id"));
			card.setListId(pickerc.getString("idList"));
			card.setName(pickerc.getString("name"));
			card.setDesc(pickerc.getString("desc"));
			card.setClosed(pickerc.getBoolean("closed"));

			String dueString = pickerc.getString("due");
			if( dueString != null ){
				try{
					Date due = df.parse(dueString);
					card.setDue(due);
				}catch(ParseException e0){}
			}

			for(Object mid : pickerc.getJSONArray("idMembers")){
				if( mid instanceof String ){
					card.addMemberId((String)mid);
				}
			}

			board.addCard(card);
		}

		return board;
	}

	public TrelloBoard fetch(String boardId){
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

		if( res != null && res instanceof JSONObject){
			return buildTrelloBoard((JSONObject)res);
		}else{
			return null;
		}
	}
}
