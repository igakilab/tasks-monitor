package jp.ac.oit.igakilab.tasks.test;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.scripts.TrelloActionCacheProvider;
import jp.ac.oit.igakilab.tasks.scripts.TrelloActionCacheProvider.ActionCacheFetcher;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class TestTrelloActionCacheDB {
	public static void main(String[] args){
		MongoClient client = new MongoClient();
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloActionCacheProvider provider = new TrelloActionCacheProvider(client, api);
		ActionCacheFetcher<Object> fetc =
			TrelloActionCacheProvider.getCardFetcher(api, provider.getDBInstance());

		TrelloCard card = provider.getTrelloActionsCard("57ef25659c448c0e3df1aa07", fetc, false);

		System.out.println(card.toString());

		client.close();
	}
}
