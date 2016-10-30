package jp.ac.oit.igakilab.tasks.test;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.DashBoardForms;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class TestListMovedDate {
	public static void main(String[] args){
		String boardId = "57d3f5cac2c3720549a9b8c1";

		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(boardId, new TrelloActionDocumentParser()));
		board.build();

		/* テスト1
		 * for(TrelloCard card : board.getCards()){
			System.out.println(card.toString());
			System.out.println(((TrelloActionsCard)card).getActions().size());
		}*/

		/* テスト2
		 * TrelloActionsCard acard = (TrelloActionsCard)board.getCardByName("task1");
		for(TrelloAction act : acard.getActions()){
			if( act.getData().get("listAfter.name") != null ){
				System.out.println(act.dataString());
			}
		}*/

		//List<DashBoardForms.SprintCard> cards = new ArrayList<DashBoardForms.SprintCard>();
		for(TrelloCard card : board.getCards()){
			TrelloActionsCard acard = (TrelloActionsCard)card;
			DashBoardForms.SprintCard scard =
				DashBoardForms.SprintCard.getInstance(acard, board);

			System.out.println(scard.getName());
			System.out.println("\tdoing: " + scard.getMovedDoingAt());
			System.out.println("\tdone:  " + scard.getMovedDoneAt());
			System.out.println("\tfinished: <" + scard.isFinished() + ">");
		}


	}
}
