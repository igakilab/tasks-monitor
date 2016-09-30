package jp.ac.oit.igakilab.tasks.trello;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.simple.JSONArray;

import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class BoardActionFetcher {
	public static DebugLog logger = new DebugLog("BoardActionFetcher");

	private TrelloApi<Object> client;
	private String boardId;
	private Object rawData;

	public BoardActionFetcher(TrelloApi<Object> client, String boardId){
		this.client = client;
		this.boardId = boardId;
	}

	private Object getBoardActionFromServer(Calendar since){
		String url = "/1/boards/" + boardId + "/actions";
		Map<String,String> params = new HashMap<String,String>();
		params.put("limit", "1000");
		if( since != null ){
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			params.put("since", df.format(since.getTime()));
		}

		return client.rget(url, params).getData();
	}

	public boolean fetch(Calendar since){
		rawData = getBoardActionFromServer(since);
		return rawData != null;
	}

	public boolean fetch(Date since){
		Calendar cal = null;
		if( since != null ){
			cal = Calendar.getInstance();
			cal.setTime(since);
		}
		return fetch(cal);
	}

	public boolean fetch(){
		return fetch((Calendar)null);
	}

	public Object getRawData(){
		return rawData;
	}

	public JSONArray getJSONArrayData(){
		return (JSONArray)rawData;
	}
}
