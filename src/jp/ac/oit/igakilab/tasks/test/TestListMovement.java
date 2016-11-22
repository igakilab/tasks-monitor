package jp.ac.oit.igakilab.tasks.test;

import static jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard.ListMovement;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class TestListMovement {
	public static void main(String[] args){
		TasksTrelloClientBuilder.setTestApiKey();
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		String boardId = "57d3f5cac2c3720549a9b8c1";

		Calendar begin = Calendar.getInstance();
		begin.set(2016, 9, 23, 0, 0, 0);
		Calendar end = Calendar.getInstance();
		end.set(2016, 9, 30, 20, 0, 0);

		TrelloBoard board = builder.buildTrelloBoardFromTrelloActions(boardId);
		if( board instanceof TrelloActionsBoard ){
			TrelloActionsBoard aboard = (TrelloActionsBoard)board;
			TrelloActionsCard card = (TrelloActionsCard)aboard.getCardByName("task1");
			List<ListMovement> movements = card.getListMovement(begin.getTime(), end.getTime());
			movements.forEach((m) -> {
				System.out.println(m.toString());
				System.out.format("%s: [%s] -> [%s]\n", m.getDate(),
					aboard.getListById(m.getListIdBefore()).getName(),
					aboard.getListById(m.getListIdAfter()).getName());
			});

			System.out.println("----------------------");
			showDoingDoneMove(aboard, movements, null, null);
		}

		client.close();
	}

	public static void showDoingDoneMove
	(TrelloBoard board, List<ListMovement> moves, Date begin, Date end){
		int i = 0;

		System.out.println("BGN: " + (begin != null ? begin.toString() : "null"));
		System.out.println("END: " + (end != null ? end.toString() : "null"));

		ListMovement toDoing = null;
		ListMovement toDone = null;

		while(
			i < moves.size()
			&& begin != null
			&& begin.compareTo(moves.get(i).getDate()) > 0
		){ i++; }

		while(
			i < moves.size()
			&& (end == null
			|| end.compareTo(moves.get(i).getDate()) >= 0)
		){
			ListMovement m = moves.get(i);
			System.out.format("%s: [%s] -> [%s]\n", m.getDate(),
				board.getListById(m.getListIdBefore()).getName(),
				board.getListById(m.getListIdAfter()).getName());

			TrelloList after = board.getListById(m.getListIdAfter());
			if( after != null ){
				if(
					after.getName().matches(REGEX_DOING) &&
					toDoing == null
				){
					toDoing = m;
				}
				if(
					after.getName().matches(REGEX_DONE) &&
					toDoing != null
				){
					toDone = m;
				}
			}

			i++;
		}

		System.out.println("toDoing: " + (toDoing != null ? toDoing.getDate().toString() : "null"));
		System.out.println("toDone: " + (toDone != null ? toDone.getDate().toString() : "null"));

		if( toDoing != null && toDone != null ){
			long elapse = toDone.getDate().getTime() - toDoing.getDate().getTime();

			System.out.println("時間: " + (elapse / 1000 / 60) + "分");


		}
	}
}
