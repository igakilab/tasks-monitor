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
			System.out.println("ボード: " + board.get("name") + " (" + ((String)board.get("id")).substring(0, 7) + ")");
		}

		/*
		Parameters params = new Parameters();
		params.setParameter("name", "test10");
		Object ret = api.post("/1/boards", params);
		System.out.println("reply: " + ret.toString());
		*/
	}

	public interface TrelloApiErrorHandler{
		public void onHttpNG(int status, HttpResponse response);
		public void onException(Exception e0);
	}

	public static class SimpleErrorHandler
	implements TrelloApiErrorHandler{
		@Override
		public void onHttpNG(int status, HttpResponse response){
			System.err.println("TrelloApi[HTTP-NG] status:" + status);
		}
		@Override
		public void onException(Exception e0){
			e0.printStackTrace();
		}
	}

	public static class Parameters {
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
	public static boolean DEBUG = false;

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

	private Object sendRequestAndParseJson(String method, String url, Parameters params){
		HttpRequest request = createDefaultHttpRequest(method, url, params);
		if( DEBUG ){
			System.out.println("TrelloApi[debug]: send:" + method + " " + url);
			for(String key : params.keySet()){
				System.out.println("TrelloApi[debug]: send:param " +
					key + "=" + params.getParameter(key));
			}
		}
		HttpResponse response = request.sendRequest();
		if( response != null ){
			if( DEBUG ){
				System.out.println("TrelloApi[debug]: receive:" + response.getStatus());
				System.out.println("TrelloApi[debug]: receive:" + response.getResponseText());
			}
			if( response.getStatus() == HttpRequest.HTTP_OK ){
				Object replyJson;
				try{
					replyJson = parseJSON(response.getResponseText());
				}catch(ParseException e0){
					if( errorHandler != null ){
						errorHandler.onException(e0);
					}
					return null;
				}
				return replyJson;
			}else{
				if( errorHandler != null ){
					errorHandler.onHttpNG(response.getStatus(), response);
				}
				return null;
			}
		}else{
			return null;
		}
	}

	public Object get(String url){
		return get(url, null);
	}

	public Object get(String url, Parameters params){
		return sendRequestAndParseJson("GET", url, params);
	}

	public Object post(String url){
		return post(url, null);
	}

	public Object post(String url, Parameters params){
		return sendRequestAndParseJson("POST", url, params);
	}

	public Object put(String url){
		return put(url, null);
	}

	public Object put(String url, Parameters params){
		return sendRequestAndParseJson("PUT", url, params);
	}

	public Object delete(String url){
		return delete(url, null);
	}

	public Object delete(String url, Parameters params){
		return sendRequestAndParseJson("DELETE", url, params);
	}

}
