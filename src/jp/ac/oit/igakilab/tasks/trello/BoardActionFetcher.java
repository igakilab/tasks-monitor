package jp.ac.oit.igakilab.tasks.trello;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;

import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.http.HttpResponse;
import jp.ac.oit.igakilab.tasks.http.TrelloApi;
import jp.ac.oit.igakilab.tasks.http.TrelloApi.Parameters;
import jp.ac.oit.igakilab.tasks.http.TrelloApi.TrelloApiErrorHandler;

public class BoardActionFetcher {
	public static DateFormat isodateFormatter =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	public static DebugLog logger = new DebugLog("BoardActionFetcher");

	private TrelloApi client;
	private String boardId;
	private Object rawData;

	public BoardActionFetcher(TrelloApi client, String boardId){
		this.client = client;
		setTrelloApiErrorHandler();
		this.boardId = boardId;
	}

	private void setTrelloApiErrorHandler(){
		client.setErrorHandler(new TrelloApiErrorHandler(){
			@Override
			public void onHttpNG(int status, HttpResponse res){
				logger.log(DebugLog.LS_ERROR, "http request failed : " + status);
			};
			@Override
			public void onException(Exception e0){
				logger.log(DebugLog.LS_EXCEPTION, e0.getMessage());
				e0.printStackTrace();
			}
		});
	}

	private Object getBoardActionFromServer(Date since){
		String url = "/1/boards/" + boardId + "/actions";
		Parameters params = new Parameters();
		if( since != null ){
			params.setParameter("since", isodateFormatter.format(since));
		}

		return client.get(url, params);
	}

	public boolean fetch(Date since){
		rawData = getBoardActionFromServer(since);
		return rawData != null;
	}

	public Object getRawData(){
		return rawData;
	}

	public JSONArray getJSONArrayData(){
		return (JSONArray)rawData;
	}
}
