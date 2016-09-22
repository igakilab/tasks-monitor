package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardTreeForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloBoardTreeForm.TrelloListTreeForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class DashBoard {
	public TrelloBoardTreeForm getKanban(String boardId)
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

		//ボードから余計なリストを排除「todo,doing,done」のみのボードに成型する
		//dTodo, dDoing, dDoneはリストが重複して残されないように発見されたらtrueにしておく変数
		//toArrayで配列をコピーして置くことで、リストをfor文中で削除しても順序が変更されないようにする
		List<TrelloList> lists = board.getLists();
		boolean dTodo = false, dDoing = false, dDone = false;
		for(TrelloList list : lists.toArray(new TrelloList[lists.size()])){
			if( list.getName().matches("(?i)to\\s*do") && !dTodo ){
				dTodo = true;
			}else if( list.getName().matches("(?i)doing") && !dDoing ){
				dDoing = true;
			}else if( list.getName().matches("(?i)done") && !dDone ){
				dDone = true;
			}else{
				board.removeList(list.getId());
			}
		}

		//フォームに変換
		TrelloBoardTreeForm form = TrelloBoardTreeForm.getInstance(board);

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
