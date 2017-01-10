package jp.ac.oit.igakilab.tasks.hubot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.util.HttpRequest;
import jp.ac.oit.igakilab.tasks.util.HttpResponse;

public class HubotTaskNotify extends HubotSendMessage{
	public static void main(String[] args){
		List<String> cards = Arrays.asList("カード1", "#33 カード2", "カード3 やで");
		HubotTaskNotify notifer = new HubotTaskNotify("");
		try{notifer.taskNotify("test", "あいうえお", cards);}
		catch(IOException e0){}
	}

	public HubotTaskNotify(String hubotUrl){
		super(hubotUrl);
	}

	@SuppressWarnings("unchecked")
	public HttpResponse taskNotify(String room, String message, List<String> cardNames)
	throws IOException{
		//リクエストを生成
		HttpRequest request = new HttpRequest("POST", hubotUrl + "/hubot/task_notify");
		request.setRequestProperty("Content-type", "application/json");

		//送信ボディを構成
		JSONObject body = new JSONObject();
		body.put("room", room);
		body.put("message", message);
		JSONArray cards = new JSONArray();
		cards.addAll(cardNames);
		body.put("cards", cards);
		System.out.println(body.toJSONString());

		//リクエストを送信
		HttpResponse res = request.sendRequest(body.toJSONString());
		//HttpResponse res = null;

		return res;
	}
}
