package jp.ac.oit.igakilab.tasks.test;

import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class TestTrelloBoardFetcher {
	public static String BOARD_ID = "57ab33677fd33ec535cc4f28";

	public static void main(String[] args){
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api, BOARD_ID);
		TrelloBoard board = fetcher.getBoard();
		fetcher.fetch();

		System.out.println("=== BOARD ===");
		System.out.format("%s: %s\n", board.getId(), board.getName());

		System.out.println("\n=== LISTS ===");
		for(TrelloList list : board.getLists()){
			System.out.format("%s: %s\n", list.getId(), list.getName());
		}

		System.out.println("\n=== CARDS ===");
		for(TrelloCard card : board.getCards()){
			System.out.format("%s: %s\n", card.getId(), card.getName());
		}
	}
}
