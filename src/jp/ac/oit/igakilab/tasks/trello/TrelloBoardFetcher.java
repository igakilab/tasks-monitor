package jp.ac.oit.igakilab.tasks.trello;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApiConnectionFailedException;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.util.JSONObjectValuePicker;

public class TrelloBoardFetcher{
	public static void main(String[] args){
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api, "580f2dc6cf8021fb4e915e19");
		fetcher.fetch();
		System.out.println(fetcher.board.toString());
		fetcher.board.printListsAndCards(System.out);

		TrelloCard card = new TrelloCard();
		card.setName("task7");
		card.setDesc("fetcher.addCardのテストだよー");
		System.out.println(fetcher.addCard(fetcher.board.getLists().get(0), card));
	}

	private TrelloApi<Object> api;
	private String boardId;
	private TrelloBoard board;
	private boolean autoFetch;

	public TrelloBoardFetcher(TrelloApi<Object> api, String boardId){
		this.api = api;
		this.boardId = boardId;
		this.board = null;
		this.autoFetch = true;
	}

	public String getBoardId(){
		return boardId;
	}

	public void setAutoFetch(boolean b){
		this.autoFetch = b;
	}

	private void applyBoardData(JSONObject boardData){
		//ボードインスタンスの生成
		if( board == null ){
			board = new TrelloBoard();
		}

		//ボードデータの更新
		JSONObjectValuePicker picker = new JSONObjectValuePicker(boardData);
		board.setId(picker.getString("id"));
		board.setName(picker.getString("name"));
		board.setDesc(picker.getString("desc"));
		board.setShortLink(picker.getString("shortUrl"));
		board.setClosed(picker.getBoolean("closed"));

		//リストの更新
		JSONArray lists = picker.getJSONArray("lists");
		TrelloList[] prelists = board.getLists().toArray(new TrelloList[0]);
		board.clearLists();
		for(Object objList : lists){
			//インスタンスを初期化等
			JSONObjectValuePicker pickerl = new JSONObjectValuePicker(objList);
			String lid = pickerl.getString("id");

			//既存リストインスタンスがないかチェック
			TrelloList list = null;
			for(TrelloList pl : prelists){
				if( lid != null && pl.getId().equals(lid) ){
					list = pl;
					break;
				}
			}
			//見つからなかった場合はリストを生成
			list = new TrelloList();

			//値を設定
			list.setId(lid);
			list.setName(pickerl.getString("name"));
			list.setClosed(pickerl.getBoolean("closed"));

			//ボードに追加
			board.addList(list);
		}

		//カードの解析
		TrelloDateFormat df = new TrelloDateFormat();
		JSONArray cards = picker.getJSONArray("cards");
		TrelloCard[] precards = board.getCards().toArray(new TrelloCard[0]);
		for(Object objCard : cards){
			//インスタンス初期化
			JSONObjectValuePicker pickerc = new JSONObjectValuePicker(objCard);
			String cid = pickerc.getString("id");

			//既存カードインスタンスがないかチェック
			TrelloCard card = null;
			for(TrelloCard pc : precards){
				if( cid != null && pc.getId().equals(cid) ){
					card = pc;
					break;
				}
			}
			//見つからなかった場合はカードを生成
			card = new TrelloCard();

			//値を設定
			card.setId(pickerc.getString("id"));
			card.setListId(pickerc.getString("idList"));
			card.setName(pickerc.getString("name"));
			card.setDesc(pickerc.getString("desc"));
			card.setClosed(pickerc.getBoolean("closed"));
			//値を設定(期限)
			String dueString = pickerc.getString("due");
			if( dueString != null ){
				try{
					Date due = df.parse(dueString);
					card.setDue(due);
				}catch(ParseException e0){}
			}
			//値を設定(担当者trelloid)
			for(Object mid : pickerc.getJSONArray("idMembers")){
				if( mid instanceof String ){
					card.addMemberId((String)mid);
				}
			}

			//ボードに追加
			board.addCard(card);
		}
	}

	public boolean fetch(){
		String url = "/1/boards/" + boardId;
		Map<String,String> params = new HashMap<String,String>();
		params.put("cards", "all");
		params.put("lists", "all");

		try{
			Object res = api.get(url, params);
			if( res instanceof JSONObject ){
				applyBoardData((JSONObject)res);
			}else{
				return false;
			}
		}catch(TrelloApiConnectionFailedException e0){
			return false;
		}

		return true;
	}

	public TrelloBoard getBoard(){
		return board;
	}

	public boolean addCard(TrelloList list, TrelloCard card){
		String lid = list.getId();
		if( lid == null ) return false;

		String url = "/1/cards";
		Map<String,String> params = new HashMap<String,String>();

		params.put("idList", lid);

		String tmp0;
		if( (tmp0 = card.getName()) != null ){
			params.put("name", tmp0);
		}
		if( (tmp0 = card.getDesc()) != null ){
			params.put("desc", tmp0);
		}

		Date tmp1 = card.getDue();
		if( tmp1 != null ){
			TrelloDateFormat df = new TrelloDateFormat();
			params.put("due", df.format(tmp1));
		}

		Set<String> tmp2 = card.getMemberIds();
		if( tmp2.size() > 0 ){
			String tmp3 = "";
			for(String mid : tmp2){
				tmp3 += (tmp3.length() > 0 ? "," : "") + mid;
			}
			params.put("idMembers", tmp3);
		}

		System.out.println(params.toString());

		try{
			api.post(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			e0.printStackTrace();
			return false;
		}

		if( autoFetch ){
			fetch();
		}

		return true;
	}
}
