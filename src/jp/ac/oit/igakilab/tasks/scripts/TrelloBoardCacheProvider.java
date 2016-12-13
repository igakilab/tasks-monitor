package jp.ac.oit.igakilab.tasks.scripts;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
		this.verboseEnabled = false;
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
		if( verboseEnabled ){
			System.out.println("TrelloBoardCacheProvider: " + msg);
		}
	}


	public boolean exists(String boardId){
		return cdb.boardIdExists(boardId);
	}


	protected boolean isTimeout(String boardId){
		Date last = cdb.getLastUpdateDate(boardId);

		if( last == null ){
			verbose("initial update");
			return true;
		}else{
			if( timeout > 0 ){
				Date now = Calendar.getInstance().getTime();
				verbose(String.format("cache elapsed %.1f min",
					(double)(now.getTime() - last.getTime())/60/1000));
				return (now.getTime() - last.getTime()) >= timeout;
			}else{
				return true;
			}
		}
	}


	protected TrelloBoard buildBoard(String boardId){
		return cdb.findBoardCache(boardId, new TrelloBoardCacheBuilder());
	}


	public boolean updateBoardCache(String boardId){
		if( api != null ){
			JSONObject reply = TrelloBoardFetcher.sendFetchRequest(api, boardId);
			if( reply != null ){
				return cdb.updateBoardCache(boardId, reply, new JsonDocumentConverter());
			}else{
				return false;
			}
		}else{
			return false;
		}
	}


	public TrelloBoard getBoard(String boardId, boolean forceUpdate){
		//必要に応じてアップデートを行う
		long start = System.currentTimeMillis();
		if( api != null && (forceUpdate || isTimeout(boardId)) ){
			verbose("UPDATE Update cache... " + boardId + (forceUpdate ? " (forcedUpdate)" : " (timeout)"));
			updateBoardCache(boardId);
		}

		//ボードをビルドする
		verbose("BUILD Build board... " + boardId);

		long end = System.currentTimeMillis();
		verbose("-- board built in " + (end - start) + "ms");
		return buildBoard(boardId);
	}


	public TrelloBoard getBoard(String boardId){
		return getBoard(boardId, false);
	}


	public List<TrelloBoardCacheDB.BoardCacheInfo> getCacheList(){
		return cdb.getBoardCacheList();
	}
}
