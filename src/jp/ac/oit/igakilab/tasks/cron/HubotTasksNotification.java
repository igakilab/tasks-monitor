package jp.ac.oit.igakilab.tasks.cron;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.http.HttpRequest;
import jp.ac.oit.igakilab.tasks.http.HttpRequest.ConnectionErrorHandler;
import jp.ac.oit.igakilab.tasks.http.HttpResponse;

public class HubotTasksNotification {


	private String hubotUrl;

	public HubotTasksNotification(String hubotUrl){
		this.hubotUrl = hubotUrl;
	}

	public String sendMessage(String room, String message){
		HttpRequest request = new HttpRequest("POST", hubotUrl + "/hubot/send_message");
		request.setErrorHandler(new ConnectionErrorHandler(){
			public void onError(Exception e0){
				e0.printStackTrace();
			}
		});
		Document json = new Document();
		json.append("room", room).append("message", message);
		String body = json.toJson();
		//System.out.println(body);
		request.setRequestProperty("Content-type", "application/json");
		HttpResponse res = request.sendRequest(body);
		return (res != null) ? res.getResponseText() : "null";
	}

	public void run(){
	}
}
