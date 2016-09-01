package jp.ac.oit.igakilab.tasks.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class TrelloApi {
	public static void main(String[] args){
		String ak = "67ad72d3feb45f7a0a0b3c8e1467ac0b";
		String at =  "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65";
		TrelloApi api = new TrelloApi(ak, at);

		JSONObject obj = (JSONObject)api.get("/1/members/me");
		System.out.println(obj.toString());
		System.out.println("ユーザーID: " + obj.get("id"));

		JSONArray boards = (JSONArray)api.get("/1/members/me/boards");
		System.out.println(boards.toString());
		for(Object t : boards){
			JSONObject board = (JSONObject)t;
			System.out.println("ボード: " + board.get("name"));
		}
	}

	interface TrelloApiErrorHandler{
		public void onHttpNG(int status, HttpResponse response);
		public void onException(Exception e0);
	}

	class Parameters {
		Map<String,String> params;

		public Parameters(){
			params = new HashMap<String,String>();
		}

		public Parameters setParameter(String key, String value){
			params.put(key, value);
			return this;
		}

		public Parameters setParameters(Map<String,String> map){
			for(String key : map.keySet()) setParameter(key, map.get(key));
			return this;
		}

		public String getParameter(String key){
			return params.get(key);
		}

		public Set<String> keySet(){
			return params.keySet();
		}
	}

	private static String URL_HEAD = "http://api.trello.com";

	private String apiKey;
	private String apiToken;
	private TrelloApiErrorHandler errorHandler;

	public TrelloApi(String apiKey, String apiToken){
		this.apiKey = apiKey;
		this.apiToken = apiToken;
	}

	public void setErrorHandler(TrelloApiErrorHandler handler){
		errorHandler = handler;
	}

	private HttpRequest createDefaultHttpRequest(String method, String url, Parameters params){
		HttpRequest request = new HttpRequest(method, URL_HEAD + url);
		request.setParameter("key", apiKey)
			.setParameter("token", apiToken);
		if( params != null ){
			for(String key : params.keySet()){
				request.setParameter(key, params.getParameter(key));
			}
		}
		if( errorHandler != null ){
			request.setErrorHandler(new HttpRequest.ConnectionErrorHandler(){
				public void onError(Exception e0){
					errorHandler.onException(e0);
				}
			});
		}
		return request;
	}

	private Object parseJSON(String json)
	throws ParseException{
		JSONParser parser = new JSONParser();
		return parser.parse(json);
	}

	public Object get(String url){
		return get(url, null);
	}

	public Object get(String url, Parameters params){
		HttpRequest request = createDefaultHttpRequest("GET", url, params);
		HttpResponse response = request.sendRequest();
		if( response != null ){
			Object json;
			try{
				json = parseJSON(response.getResponseText());
			}catch(ParseException e0){
				if( errorHandler != null ){
					errorHandler.onException(e0);
				}
				return null;
			}
			return json;
		}
		return null;
	}

}
