package jp.ac.oit.igakilab.tasks.hubot;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.model.Card;
import jp.ac.oit.igakilab.tasks.util.HttpRequest;
import jp.ac.oit.igakilab.tasks.util.HttpResponse;

public class HubotTaskNotify extends HubotSendMessage{
	public HubotTaskNotify(String hubotUrl){
		super(hubotUrl);
	}

	@SuppressWarnings("unchecked")
	public HttpResponse taskNotify(String dest, String message, List<Card> cards)
	throws IOException{
		//リクエストを生成
		HttpRequest request = new HttpRequest("POST", hubotUrl + "/hubot/task_notify");
		request.setRequestProperty("Content-type", "application/json");

		//送信ボディを構成
		JSONObject body = new JSONObject();
		body.put("room", dest);
		body.put("message", message);

		//リクエストを送信
		HttpResponse res = request.sendRequest(body.toJSONString());

		return res;
	}
}
