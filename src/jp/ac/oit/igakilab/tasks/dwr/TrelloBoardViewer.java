package jp.ac.oit.igakilab.tasks.dwr;

import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class TrelloBoardViewer {
	public TrelloBoardForm getSampleData(){
		TrelloBoard board = new TrelloBoard();
		board.setId("18a2");
		board.setName("sample-board");
		TrelloList list1 = new TrelloList();
		list1.setId("33fc");
		list1.setName("todo");
		board.addList(list1);
		TrelloList list2 = new TrelloList();
		list2.setId("33fd");
		list2.setName("doing");
		board.addList(list2);
		TrelloCard card1 = new TrelloCard();
		card1.setId("6f23");
		card1.setListId(list1.getId());
		card1.setName("task1");
		board.addCard(card1);
		TrelloCard card2 = new TrelloCard();
		card2.setId("6f24");
		card2.setListId(list2.getId());
		card2.setName("task2");
		card2.addMemberId("473e");
		board.addCard(card2);
		TrelloCard card3 = new TrelloCard();
		card3.setId("6f25");
		card3.setListId(list1.getId());
		card3.setName("task3");
		board.addCard(card3);

		TrelloBoardForm form = TrelloBoardForm.getInstance(board);
		return form;
	}

	/*
	public TrelloBoardForm getBoardData(String boardId){
		MongoClient client = TasksMongoClientBuilder.createClient();
	}
	*/
}
