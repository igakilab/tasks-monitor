package jp.ac.oit.igakilab.tasks.test;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard.ListMovement;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class TestListMovement {
	public static void main(String[] args){
		TasksTrelloClientBuilder.setTestApiKey();
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		String boardId = "57d3f5cac2c3720549a9b8c1";

		TrelloBoard board = builder.buildTrelloBoardFromTrelloActions(boardId);
		if( board instanceof TrelloActionsBoard ){
			TrelloActionsBoard aboard = (TrelloActionsBoard)board;
			TrelloActionsCard card = (TrelloActionsCard)aboard.getCardByName("task1");
			List<ListMovement> movements = card.getListMovement();
			movements.forEach((m) -> {
				System.out.println(m.toString());
				System.out.format("%s: [%s] -> [%s]\n", m.getTimestamp(),
					aboard.getListById(m.getListIdBefore()).getName(),
					aboard.getListById(m.getListIdAfter()).getName());
			});
		}

	}
}
