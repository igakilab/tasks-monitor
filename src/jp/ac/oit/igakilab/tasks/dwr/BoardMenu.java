package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB.Board;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.BoardMenuForms.BoardInfo;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardDataForm;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardData;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class BoardMenu {
	public List<BoardInfo> getBoardList(){
		MongoClient client = TasksMongoClientBuilder.createClient();

		//ボードリスト取得
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		List<Board> boardDatas = bdb.getBoardList();

		//trelloidメンバー変換テーブル生成
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(client);

		//アクションdbの初期化
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		DocumentParser<TrelloAction> parser = new TrelloActionDocumentParser();

		List<BoardInfo> boards = new ArrayList<BoardInfo>();
		for(Board bd : boardDatas){
			//ボードデータ取得
			List<TrelloAction> actions = adb.getTrelloActions(bd.getId(), parser);
			if( actions.size() > 0 ){
				//ボードデータのビルド
				TrelloActionsBoard board = new TrelloActionsBoard();
				board.addActions(actions);
				TrelloBoardData data = board.buildBoardData();

				//メンバーidの変換
				List<String> members = new ArrayList<String>();
				board.getMemberIds().forEach((tmid) ->{
					String mid = mtable.getMemberId(tmid);
					if( mid != null ) members.add(mid);
				});

				//ボード情報の構築
				BoardInfo inf = new BoardInfo();
				inf.setData(TrelloBoardDataForm.getInstance(data));
				inf.setMembers(members);
				inf.setLastUpdate(bd.getLastUpdate());

				boards.add(inf);
			}
		}

		client.close();
		return boards;
	}
}
