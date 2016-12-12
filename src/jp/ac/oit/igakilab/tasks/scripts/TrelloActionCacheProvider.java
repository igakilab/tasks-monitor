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

	private TrelloApi<Object> api;
	private TrelloActionCacheDB cdb;
	private long timeout;
	public boolean verboseEnabled;

	public TrelloActionCacheProvider(MongoClient client, TrelloApi<Object> api, long timeout){
		this.cdb = new TrelloActionCacheDB(client);
		this.api = api;
		this.timeout = Math.max(timeout, 0);
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
		System.out.println("TrelloBoardCacheProvider: " + msg);
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


	protected <T> boolean updateIfNeeded(String cat, String id, ActionCacheFetcher<T> fetcher, boolean forceUpdate){
		if( fetcher != null && (forceUpdate || isTimeout(cat, id)) ){
			verbose("Update cache... " + cat + " " + id + (forceUpdate ? " (forcedUpdate)" : " (timeout)"));

			Collection<T> actions = fetcher.fetchActions(cat, id);
			if( actions != null ){
				long cnt = cdb.applyActionCache(cat, id, actions, fetcher.getConverter());
				verbose(cnt + " record(s) updated.");
				return true;
			}else{
				return false;
			}

		}else{
			verbose("Update cache passing.");
			return false;
		}
	}


	public TrelloActionsCard getTrelloActionsCard(String cardId, boolean forceReload){
		//更新作業
		if( api != null ){
			ActionCacheFetcher<Object> fetcher = new ActionCacheFetcher<Object>(){
				@Override
				public Collection<Object> fetchActions(String cat, String id) {
					TrelloCardFetcher cf = new TrelloCardFetcher(api);
					List<Object> actions = cf.getCardActions(id).stream()
						.map((jobj) -> (Object)jobj)
						.collect(Collectors.toList());
					return actions;
				}
				@Override
				public DocumentConverter<Object> getConverter() {
					return new JsonDocumentConverter();
				}
			};

			updateIfNeeded(CCARD, cardId, fetcher, forceReload);
		}


		//データビルド
		List<TrelloAction> actions = cdb.findActionCache(CCARD, cardId, new TrelloActionDocumentParser());
		TrelloActionsCard card = new TrelloActionsCard();
		actions.forEach((act -> card.applyAction(act)));

		return card;
	}
}