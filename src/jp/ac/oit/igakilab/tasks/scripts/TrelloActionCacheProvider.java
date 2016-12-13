package jp.ac.oit.igakilab.tasks.scripts;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TrelloActionCacheDB;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.JsonDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloActionCacheProvider {
	public static long DEFAULT_TIMEOUT = (30 * 1000);

	public static String CCARD = "CARD";

	public static interface ActionCacheFetcher<T>{
		public Collection<T> fetchActions(String cat, String id);
		public DocumentConverter<T> getConverter();
	}

	public static ActionCacheFetcher<Object> getCardFetcher(TrelloApi<Object> api, TrelloActionCacheDB cdb){
		return new ActionCacheFetcher<Object>(){
			private TrelloCardFetcher fetcher = new TrelloCardFetcher(api);
			private JsonDocumentConverter converter = new JsonDocumentConverter();

			@Override
			public Collection<Object> fetchActions(String cat, String id) {
				if( CCARD.equals(cat) && api != null ){
					Date since = null;
					if( cdb != null ){
						since = cdb.getLastUpdateDate(cat, id);
					}

					List<Object>actions = fetcher.getCardActions(id, since, null).stream()
						.map((jobj) -> (Object)jobj)
						.collect(Collectors.toList());
					return actions;
				}
				return null;
			}

			@Override
			public DocumentConverter<Object> getConverter() {
				return converter;
			}
		};
	}

	private TrelloApi<Object> api;
	private TrelloActionCacheDB cdb;
	private long timeout;
	public boolean verboseEnabled;

	public TrelloActionCacheProvider(MongoClient client, TrelloApi<Object> api, long timeout){
		this.cdb = new TrelloActionCacheDB(client);
		this.api = api;
		this.timeout = Math.max(timeout, 0);
		this.verboseEnabled = false;
	}

	public TrelloActionCacheProvider(MongoClient client, TrelloApi<Object> api){
		this(client, api, DEFAULT_TIMEOUT);
	}

	public TrelloActionCacheProvider(MongoClient client){
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
			System.out.println("TrelloActionCacheProvider: " + msg);
		}
	}

	public TrelloActionCacheDB getDBInstance(){
		return cdb;
	}

	public ActionCacheFetcher<Object> getCardFetcher(){
		return getCardFetcher(api, cdb);
	}


	protected boolean isTimeout(String cat, String id){
		Date last = cdb.getLastUpdateDate(cat, id);

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


	public boolean needsUpdate(String cat, String id, boolean forceUpdate){
		return forceUpdate || isTimeout(cat, id);
	}


	protected <T> boolean updateActionsCache(String cat, String id, ActionCacheFetcher<T> fetcher){
		if( fetcher == null ) return false;

		verbose("UPDATE cache... " + cat + " " + id);

		Collection<T> actions = fetcher.fetchActions(cat, id);
		if( actions != null ){
			long cnt = cdb.applyActionCache(cat, id, actions, fetcher.getConverter());
			verbose(cnt + " record(s) updated.");
			return true;
		}else{
			return false;
		}
	}


	public <T> TrelloActionsCard getTrelloActionsCard
	(String cardId, ActionCacheFetcher<T> fetcher, boolean forceReload){
		long start = System.currentTimeMillis();
		//アップデートを実行
		if( fetcher != null && (needsUpdate(CCARD, cardId, forceReload)) ){
			updateActionsCache(CCARD, cardId, fetcher);
		}

		//データビルド
		verbose("BUILD action card " + cardId);
		List<TrelloAction> actions = cdb.findActionCache(CCARD, cardId, new TrelloActionDocumentParser());
		TrelloActionsCard card = new TrelloActionsCard();
		actions.forEach((act -> card.applyAction(act)));

		long end = System.currentTimeMillis();
		verbose("-- action card built in " + (end - start) + "ms");
		return card;
	}


	public <T> TrelloActionsCard getTrelloActionsCard
	(String cardId, boolean forceReload){
		if( needsUpdate(CCARD, cardId, forceReload) && api != null ){
			return getTrelloActionsCard(cardId, getCardFetcher(), true);

		}else{
			return getTrelloActionsCard(cardId, null, false);
		}
	}
}