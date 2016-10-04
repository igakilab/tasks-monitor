package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.KanbanForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardTreeForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardTreeForm.TrelloListTreeForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class DashBoard {
	public KanbanForm getKanban(String boardId)
	throws ExcuteFailedException{
		//クライアントの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		//アクションdbの操作クラスを生成
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		//アクション一覧を取得
		List<TrelloAction> actions = adb.getTrelloActions(boardId, new DocumentTrelloActionParser());
		//アクションの有無をチェック、ボードがない場合、要素なしのリストが返されている
		if( actions.size() <= 0 ){
			throw new ExcuteFailedException("ボードのデータがありません");
		}

		//ボードのデータ構造クラスを生成、アクションを登録、buildでボードを内部で構築
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		//フォームに変換
		//ここでボードをtodo,doing,doneのみのボードに成型する
		KanbanForm form = KanbanForm.getInstance(board);

		return form;
	}

	public TrelloBoardTreeForm getSampleKanban(){
		TrelloBoardTreeForm form = new TrelloBoardTreeForm();
		form.setId("33");

		form.setLists(new TrelloListTreeForm[3]);
		form.getLists()[0] = new TrelloListTreeForm();
		form.getLists()[0].setId("55");

		return form;
	}


}
