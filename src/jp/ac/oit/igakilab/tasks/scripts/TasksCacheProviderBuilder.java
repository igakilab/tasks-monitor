package jp.ac.oit.igakilab.tasks.scripts;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TasksCacheProviderBuilder {
	static final String BC_TIMEOUT_KEY = "tasks.boardcache.timeout";
	static final int BC_TIMEOUT_DEFAULT = 30;
	static final String AC_TIMEOUT_KEY = "tasks.actioncache.timeout";
	static final int AC_TIMEOUT_DEFAULT = 30;

	static boolean VERBOSE_ENABLED = true;

	private static int loadCacheTimeout(String key, int defaultValue){
		int sec = defaultValue;
		if( AppProperties.global.containsKey(key) ){
			try{
				sec = Integer.parseInt(AppProperties.global.get(key, null));
			}catch(NumberFormatException e0){
				sec = defaultValue;
			}
		}
		return sec;
	}

	public static TrelloActionCacheProvider getActionCacheProvider
	(MongoClient client, TrelloApi<Object> api, int timeoutInSec){
		TrelloActionCacheProvider p = new TrelloActionCacheProvider(client, api, timeoutInSec * 1000);
		p.verboseEnabled = VERBOSE_ENABLED;
		return p;
	}

	public static TrelloActionCacheProvider getActionCacheProvider
	(MongoClient client, TrelloApi<Object> api){
		return getActionCacheProvider(client, api, loadCacheTimeout(AC_TIMEOUT_KEY, AC_TIMEOUT_DEFAULT));
	}

	public static TrelloActionCacheProvider getActionCacheProvider
	(MongoClient client, int timeoutInSec){
		return getActionCacheProvider(client, null, timeoutInSec);
	}

	public static TrelloActionCacheProvider getActionCacheProvider
	(MongoClient client){
		return getActionCacheProvider(client, null, loadCacheTimeout(AC_TIMEOUT_KEY, AC_TIMEOUT_DEFAULT));
	}

	public static TrelloBoardCacheProvider getBoardCacheProvider
	(MongoClient client, TrelloApi<Object> api, int timeoutInSec){
		TrelloBoardCacheProvider p = new TrelloBoardCacheProvider(client, api, timeoutInSec * 1000);
		p.verboseEnabled = VERBOSE_ENABLED;
		return p;
	}

	public static TrelloBoardCacheProvider getBoardCacheProvider
	(MongoClient client, TrelloApi<Object> api){
		return getBoardCacheProvider(client, api, loadCacheTimeout(BC_TIMEOUT_KEY, BC_TIMEOUT_DEFAULT));
	}

	public static TrelloBoardCacheProvider getBoardCacheProvider
	(MongoClient client, int timeoutInSec){
		return getBoardCacheProvider(client, null, timeoutInSec);
	}

	public static TrelloBoardCacheProvider getBoardCacheProvider
	(MongoClient client){
		return getBoardCacheProvider(client, null, loadCacheTimeout(BC_TIMEOUT_KEY, BC_TIMEOUT_DEFAULT));
	}
}
