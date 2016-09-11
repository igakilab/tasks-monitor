package jp.ac.oit.igakilab.tasks.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.MongoTrelloBoardActionUpdater;

public class TrelloBoardActionsUpdater implements Runnable{
	static class Board{
		static Board toBoard(Document doc){
			return new Board(
				doc.getString("id"),
				doc.getDate("lastUpdate"));
		}

		private String boardId;
		private Date lastUpdate;

		Board(String boardId, Date lastUpdate){
			this.boardId = boardId;
			this.lastUpdate = lastUpdate;
		}

		String getId(){return boardId;}
		Date getLastUpdate(){return lastUpdate;}
	}

	private MongoClient client;

	public TrelloBoardActionsUpdater(MongoClient client){
		this.client = client;
	}

	private List<Board> getBoardList(BoardDBDriver bdb){
		List<Board> list = new ArrayList<Board>();
		for(Document doc : bdb.getBoardList()){
			list.add(Board.toBoard(doc));
		}
		return list;
	}

	private boolean updateBoardLastUpdateDate
	(BoardDBDriver bdb, String boardId, Date lastdate){
		return bdb.updateLastUpdateDate(boardId, lastdate) > 0;
	}

	public void run(){
		Calendar cal = Calendar.getInstance();

		BoardDBDriver bdb = new BoardDBDriver(client);
		MongoTrelloBoardActionUpdater tadb =
			new MongoTrelloBoardActionUpdater(client);

		List<Board> boards = getBoardList(bdb);

		for(Board board : boards){

		}
	}
}
