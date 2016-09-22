package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class DashBoard {
	public TrelloBoardForm getKanban(String boardId)
	throws ExcuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		List<TrelloAction> actions = adb.getTrelloActions(boardId, new DocumentTrelloActionParser());
		if( actions.size() <= 0 ){
			throw new ExcuteFailedException("ボードのデータがありません");
		}

		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		List<TrelloList> lists = board.getLists();
		for(TrelloList list : lists){
			if(
				list.getName().matches("(?i)todo")
			){}
		}

	}


}
