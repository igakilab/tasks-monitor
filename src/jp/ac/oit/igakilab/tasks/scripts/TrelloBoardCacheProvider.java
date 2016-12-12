package jp.ac.oit.igakilab.tasks.scripts;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloBoardCacheDB;
import jp.ac.oit.igakilab.tasks.db.converters.JsonDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloBoardCacheBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class TrelloBoardCacheProvider {
	public static long DEFAULT_TIMEOUT = (30 * 1000);

	private TrelloApi<Object> api;
	private TrelloBoardCacheDB cdb;
	private long timeout;
	public boolean verboseEnabled;

	public TrelloBoardCacheProvider(MongoClient client, TrelloApi<Object> api, long timeout){
		this.cdb = new TrelloBoardCacheDB(client);
		this.api = api;
		this.timeout = Math.max(timeout, 0);
	}

	public TrelloBoardCacheProvider(MongoClient client, TrelloApi<Object> api){
		this(client, api, DEFAULT_TIMEOUT);
	}

	public TrelloBoardCacheProvider(MongoClient client){
		this(client, null, DEFAULT_TIMEOUT);
	}


	public void setTrelloApi(TrelloApi<Object> api){
		this.api = api;
	}

	public void setTimeout(long timeout){
		this.timeout = Math.max(timeout, 0);
	}

	public long getTimeout(){
		return this.timeout;
	}

	protected void verbose(String msg){
		System.out.println("TrelloBoardCacheProvider: " + msg);
	}


	protected boolean isTimeout(String boardId){
		Date last = cdb.getLastUpdateDate(boardId);

		if( last == null ){
			verbose("initial update");
			return true;
		}else{
			if( timeout > 0 ){
				Date now = Calendar.getInstance().getTime();
				if( verboseEnabled ){
					verbose(String.format("cache elapsed %.1f min",
						(double)(now.getTime() - last.getTime())/60/1000));
				}
				return (now.getTime() - last.getTime()) >= timeout;
			}else{
				return true;
			}
		}
	}


	protected TrelloBoard buildBoard(String boardId){
		return cdb.findBoardData(boardId, new TrelloBoardCacheBuilder());
	}


	protected boolean updateBoardCache(String boardId){
		if( api != null ){
			JSONObject reply = TrelloBoardFetcher.sendFetchRequest(api, boardId);
			if( reply != null ){
				return cdb.updateBoardData(boardId, reply, new JsonDocumentConverter());
			}else{
				return false;
			}
		}else{
			return false;
		}
	}


	public TrelloBoard getBoard(String boardId, boolean forceUpdate){
		//必要に応じてアップデートを行う
		if( api != null && (forceUpdate || isTimeout(boardId)) ){
			verbose("Update cache... " + boardId + (forceUpdate ? " (forcedUpdate)" : " (timeout)"));
			updateBoardCache(boardId);
		}else{
			verbose("Update cache passing.");
		}

		//ボードをビルドする
		verbose("Build board... " + boardId);
		return buildBoard(boardId);
	}


	public TrelloBoard getBoard(String boardId){
		return getBoard(boardId, false);
	}
}
