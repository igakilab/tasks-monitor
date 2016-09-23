package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardInfoForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

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

	public TrelloBoardForm getBoardData(String boardId)
	throws ExcuteFailedException{
		if( boardId == null ){
			throw new ExcuteFailedException("ボードidを指定してください");
		}

		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		List<TrelloAction> actions =
			adb.getTrelloActions(boardId, new DocumentTrelloActionParser());

		if( actions.size() > 0 ){
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			board.build();

			client.close();
			return TrelloBoardForm.getInstance(board);

		}else{
			client.close();
			throw new ExcuteFailedException("ボードがみつかりません");
		}
	}

	public List<TrelloBoardInfoForm> getBoardInfoList(){
		MongoClient client = TasksMongoClientBuilder.createClient();

		BoardDBDriver bdb = new BoardDBDriver(client);
		List<TrelloBoardInfoForm> forms = new ArrayList<TrelloBoardInfoForm>();
		bdb.getBoardList().forEach((board) -> {
			TrelloBoardInfoForm form = new TrelloBoardInfoForm();
			form.setId(board.getId());
			form.setLastUpdate(board.getLastUpdate());
			forms.add(form);
		});

		client.close();
		return forms;
	}
}
