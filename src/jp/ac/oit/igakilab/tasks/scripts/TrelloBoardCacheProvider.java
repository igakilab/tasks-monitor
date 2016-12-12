package jp.ac.oit.igakilab.tasks.scripts;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloBoardCacheDB;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class TrelloBoardCacheProvider {
	private TrelloApi<Object> api;
	private TrelloBoardCacheDB cdb;
	private long timeout;

	public TrelloBoardCacheProvider(MongoClient client, TrelloApi<Object> api, long timeout){
		this.cdb = client != null ? new TrelloBoardCacheDB(client) : null;
		this.api = api != null ? api : null;
		this.timeout = Math.max(timeout, 0);
	}

	public TrelloBoardCacheProvider(MongoClient client, TrelloApi<Object> api){
		this(client, api, -1);
	}

	public TrelloBoardCacheProvider(MongoClient client){
		this(client, null, -1);
	}

	public void setTimeout(long timeout){
		this.timeout = Math.max(timeout, 0);
	}


	public TrelloBoard buildBoard(){


		return null;

	}

}
