package jp.ac.oit.igakilab.tasks.trello;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApiConnectionFailedException;

public class TrelloCardFetcher {
	public static void main(String[] args)
	throws ParseException{
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloCardFetcher gateway = new TrelloCardFetcher(TasksTrelloClientBuilder.createApiClient());

		List<JSONObject> actions = gateway.getCardActions("57ef256a69065dce81075b1d");

		actions.forEach((act) -> System.out.println(act.toJSONString()));
	}

	/**
	 *  api: APIのインスタンスを保持します
	 */
	private TrelloApi<Object> api;


	/**
	 * コンストラクタです
	 * @param api
	 */
	public TrelloCardFetcher(TrelloApi<Object> api){
		this.api = api;
	}


	/**
	 * getCardActions(String, Date, Date)のフィルタを無効にしたメソッドです
	 * @param cardId
	 * @return
	 */
	public List<JSONObject> getCardActions(String cardId){
		return getCardActions(cardId, null, null);
	}


	/**
	 * cardIdで指定したカードのアクションを取得します
	 * sinceとbeforeで取得期間をフィルタリングでき、nullではそれぞれのフィルタが無効になります。
	 * since < 取得されるデータ < before
	 * @param cardId
	 * @param since 期間の開始位置
	 * @param before　期間の終了位置
	 * @return
	 */
	public List<JSONObject> getCardActions(String cardId, Date since, Date before){
		//urlとparamの設定
		String url = "/1/card/" + cardId + "/actions";

		Map<String,String> params = new HashMap<>();
		params.put("limit", "1000");
		if( since != null || before != null ){
			TrelloDateFormat tdf = new TrelloDateFormat();
			if( since != null ) params.put("since", tdf.format(since));
			if( before != null ) params.put("before", tdf.format(before));
		}

		//データ取得
		Object reply;
		try{ reply = api.get(url, params); }
		catch(TrelloApiConnectionFailedException e0){ return null; }

		//データ変換
		List<JSONObject> actions = new ArrayList<JSONObject>();
		if( reply instanceof JSONArray ){
			JSONArray array = (JSONArray)reply;
			for(Object obj : array){
				if( obj instanceof JSONObject ){
					actions.add((JSONObject)obj);
				}
			}
		}else{
			return null;
		}

		return actions;
	}
}
