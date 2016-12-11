package jp.ac.oit.igakilab.tasks.dwr;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.HubotApiForms.CurrentSprint;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class HubotApi {
	public CurrentSprint getCurrentSprint(String boardId)
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();

		//ボードを検索
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			throw new ExecuteFailedException("ボードが見つかりません");
		}

		//ボードを取得
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		TrelloBoard board = builder.buildTrelloActionsBoardFromTrelloActions(boardId);

		//現在進行中のスプリントを取得
		SprintsManageDB smdb = new SprintsManageDB(client);
		Sprint spr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		//フォームを生成
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(client);
		CurrentSprint form = CurrentSprint.getInstance(spr, board, ttb);

		client.close();
		return form;
	}
}
