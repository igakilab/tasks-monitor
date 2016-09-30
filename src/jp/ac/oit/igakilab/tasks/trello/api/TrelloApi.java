package jp.ac.oit.igakilab.tasks.trello.api;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jp.ac.oit.igakilab.tasks.util.HttpRequest;
import jp.ac.oit.igakilab.tasks.util.HttpResponse;


public class TrelloApi<T>{
	public static void main(String[] args){
		String ak = "67ad72d3feb45f7a0a0b3c8e1467ac0b";
		String at = "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65";
		TrelloApi<Object> api = new TrelloApi<Object>(ak, at, new SimpleJsonResponseTextParser());

		JSONObject obj = (JSONObject)api.rget("/1/members/me").getData();
		System.out.println(obj.toString());
		System.out.println("ユーザーID: " + obj.get("id"));

		JSONArray boards = (JSONArray)api.rget("/1/members/me/boards").getData();
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

	public interface ResponseTextParser<T>{
		public T parse(String responseText);
	}

	public static class SimpleJsonResponseTextParser implements ResponseTextParser<Object>{
		@Override
		public Object parse(String responseText){
			Object parsed = null;
			JSONParser parser = new JSONParser();
			try{
				parsed = parser.parse(responseText);
			}catch(ParseException e0){
				return null;
			}
			return parsed;
		}
	}

	public static class TrelloResult<T>{
		private String errorMessage;
		private T data;

		TrelloResult(T data, String errMsg){
			errorMessage = errMsg;
			this.data = data;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public T getData() {
			return data;
		}
	}

	private static String URL_HEAD = "http://api.trello.com";
	public static boolean DEBUG = false;

	private String apiKey;
	private String apiToken;
	private ResponseTextParser<T> parser;

	public TrelloApi(String apiKey, String apiToken, ResponseTextParser<T> parser){
		this.apiKey = apiKey;
		this.apiToken = apiToken;
		this.parser = parser;
	}

	private HttpRequest createDefaultHttpRequest(String method, String url, Map<String,String> params){
		HttpRequest request = new HttpRequest(method, URL_HEAD + url);
		request.setParameter("key", apiKey)
			.setParameter("token", apiToken);
		if( params != null ){
			params.forEach((key, value) -> request.setParameter(key, value));
		}
		return request;
	}

	private T sendRequestAndParseJson(String method, String url, Map<String,String> params)
	throws TrelloApiConnectionFailedException{
		//HTTPリクエストの初期化
		HttpRequest request = createDefaultHttpRequest(method, url, params);

		// DEBUG リクエストを表示
		if( DEBUG ){
			System.out.println("TrelloApi[debug]: send:" + method + " " + url);
			params.forEach((key, value) ->
				System.out.println("TrelloApi[debug]: send:param " + key + " = " + value));
		}

		//APIに接続、情報を取得
		HttpResponse response = null;
		try{
			response = request.sendRequest();
		}catch(IOException e0){
			throw new TrelloApiConnectionFailedException(0, false, e0.getMessage());
		}

		// DEBUG レスポンスを表示
		if( DEBUG ){
			System.out.println("TrelloApi[debug]: receive:" + response.getStatus());
			System.out.println("TrelloApi[debug]: receive:" + response.getResponseText());
			response.getHeaders().forEach((key, value) ->
				System.out.format("  %s: %s\n", key, value));
		}

		//ステータスコード確認
		if( response.getStatus() != HttpRequest.HTTP_OK ){
			throw new TrelloApiConnectionFailedException(
				response.getStatus(), true, "HTTP接続エラー: " + response.getStatus());
		}

		//データを変換
		T data = parser.parse(response.getResponseText());
		if( data == null ){
			throw new TrelloApiConnectionFailedException(0, false, "データ変換エラー");
		}

		return data;
	}

	public T get(String url)
	throws TrelloApiConnectionFailedException{
		return get(url, null);
	}

	public T get(String url, Map<String,String> params)
	throws TrelloApiConnectionFailedException{
		return sendRequestAndParseJson("GET", url, params);
	}

	public TrelloResult<T> rget(String url){
		return rget(url, null);
	}

	public TrelloResult<T> rget(String url, Map<String,String> params){
		T data;
		try{ data = get(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return new TrelloResult<T>(null, e0.getMessage());
		}
		return new TrelloResult<T>(data, null);
	}

	public T post(String url)
	throws TrelloApiConnectionFailedException{
		return post(url, null);
	}

	public T post(String url, Map<String,String> params)
	throws TrelloApiConnectionFailedException{
		return sendRequestAndParseJson("POST", url, params);
	}

	public TrelloResult<T> rpost(String url){
		return rpost(url, null);
	}

	public TrelloResult<T> rpost(String url, Map<String,String> params){
		T data;
		try{ data = post(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return new TrelloResult<T>(null, e0.getMessage());
		}
		return new TrelloResult<T>(data, null);
	}

	public T put(String url)
	throws TrelloApiConnectionFailedException{
		return put(url, null);
	}

	public T put(String url, Map<String,String> params)
	throws TrelloApiConnectionFailedException{
		return sendRequestAndParseJson("PUT", url, params);
	}

	public TrelloResult<T> rput(String url){
		return rput(url, null);
	}

	public TrelloResult<T> rput(String url, Map<String,String> params){
		T data;
		try{ data = put(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return new TrelloResult<T>(null, e0.getMessage());
		}
		return new TrelloResult<T>(data, null);
	}

	public T delete(String url)
	throws TrelloApiConnectionFailedException{
		return delete(url, null);
	}

	public T delete(String url, Map<String,String> params)
	throws TrelloApiConnectionFailedException{
		return sendRequestAndParseJson("DELETE", url, params);
	}

	public TrelloResult<T> rdelete(String url){
		return rdelete(url, null);
	}

	public TrelloResult<T> rdelete(String url, Map<String,String> params){
		T data;
		try{ data = delete(url, params);
		}catch(TrelloApiConnectionFailedException e0){
			return new TrelloResult<T>(null, e0.getMessage());
		}
		return new TrelloResult<T>(data, null);
	}
}
