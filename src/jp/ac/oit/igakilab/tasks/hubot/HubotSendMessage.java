package jp.ac.oit.igakilab.tasks.hubot;

import java.io.IOException;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.util.HttpRequest;
import jp.ac.oit.igakilab.tasks.util.HttpResponse;

public class HubotSendMessage {
	public static void main(String[] args){
		HubotSendMessage msg = new HubotSendMessage("http://igakilabot.herokuapp.com");
		try{
			msg.send("koike", "test message");
		}catch(IOException e0){
			e0.printStackTrace();
		}
	}

	private String hubotUrl;

	public HubotSendMessage(String hubotUrl){
		this.hubotUrl = hubotUrl;
	}

	public String getHubotUrl() {
		return hubotUrl;
	}

	public void setHubotUrl(String hubotUrl) {
		this.hubotUrl = hubotUrl;
	}

	@SuppressWarnings("unchecked")
	public HttpResponse send(String room, String message)
	throws IOException{
		//リクエストを生成
		HttpRequest request = new HttpRequest("POST", hubotUrl + "/hubot/send_message");
		request.setRequestProperty("Content-type", "application/json");

		//送信ボディを構成
		JSONObject body = new JSONObject();
		body.put("room", room);
		body.put("message", message);

		//リクエストを送信
		HttpResponse res = request.sendRequest(body.toJSONString());

		return res;
	}
}
