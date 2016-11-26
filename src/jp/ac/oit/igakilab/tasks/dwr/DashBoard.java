package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.DashBoardForms;
import jp.ac.oit.igakilab.tasks.dwr.forms.KanbanForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloNumberedCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class DashBoard {

	public DashBoardForms.DashBoardData getDashBoardData(String boardId)
	throws ExcuteFailedException{
		//クライアントの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		//dbの操作クラスを生成
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		SprintsManageDB sdb = new SprintsManageDB(client);

		/* ボードを生成 */

		//アクション一覧を取得
		List<TrelloAction> actions = adb.getTrelloActions(boardId, new TrelloActionDocumentParser());
		//アクションの有無をチェック、ボードがない場合
		if( actions.size() <= 0 ){
			client.close();
			throw new ExcuteFailedException("ボードのデータがありません");
		}

		//ボードのデータ構造クラスを生成、アクションを登録、buildでボードを内部で構築
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		/* スプリントを取得 */

		//現在日時から期間内のスプリントを取得
		Sprint sprint = sdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		/* フォームに変換 */

		DashBoardForms.DashBoardData form =
			DashBoardForms.DashBoardData.getInstance(board, sprint);

		client.close();
		return form;
	}


	//ボードのタスクをtodo,doing,doneの形式で取得する
	//もし目的のボードがない場合はerrorが返却される
	public KanbanForm getKanban(String boardId)
	throws ExcuteFailedException{
		//クライアントの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		//アクションdbの操作クラスを生成
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		//アクション一覧を取得
		List<TrelloAction> actions = adb.getTrelloActions(boardId, new TrelloActionDocumentParser());
		//アクションの有無をチェック、ボードがない場合、要素なしのリストが返されている
		if( actions.size() <= 0 ){
			client.close();
			throw new ExcuteFailedException("ボードのデータがありません");
		}

		//ボードのデータ構造クラスを生成、アクションを登録、buildでボードを内部で構築
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		//フォームに変換
		//ここでボードをtodo,doing,doneのみのボードに成型する
		KanbanForm form = KanbanForm.getInstance(board);

		client.close();
		return form;
	}

	//ボードに設定された現在のスプリントの情報が返却される
	//ボードやスプリントがない場合はnullが返される
	public SprintForm getCurrentSprint(String boardId){
		//クライアントとdb操作クラスを生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在日時から期間内のスプリントを取得
		Sprint sprint = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		//取得できなかった場合はnullを返却
		if( sprint == null ){
			client.close();
			return null;
		}

		//formに変換してreturn
		client.close();
		return SprintForm.getInstance(sprint);
	}

	//カードを新しく作成する
	public TrelloCardForm createCard(String boardId, TrelloCardForm card)
	throws ExcuteFailedException{
		//操作インスタンスを初期化
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api, boardId);

		//データを取得
		if( !fetcher.fetch() ){
			throw new ExcuteFailedException("ボードデータの取得に失敗しました");
		}

		//カードデータ追加
		TrelloBoard board = fetcher.getBoard();
		List<TrelloList> lists = board.getListsByNameMatches(TasksTrelloClientBuilder.REGEX_TODO);
		TrelloNumberedCard ncard;
		if( lists.size() > 0 ){
			ncard = new TrelloNumberedCard(TrelloCardForm.convert(card));
			if( !ncard.isNumbered() ){
				ncard.applyNumber(board.getCards());
			}
			if( !fetcher.addCard(lists.get(0), ncard) ){
				throw new ExcuteFailedException("カードの追加に失敗しました");
			}
		}else{
			throw new ExcuteFailedException("todoのリストがありません");
		}

		return TrelloCardForm.getInstance(ncard);
	}

	//進行中のスプリントを終了する
	public String closeCurrentSprint(String boardId)
	throws ExcuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在進行中のスプリントを取得
		Sprint currSpr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( currSpr == null ){
			client.close();
			throw new ExcuteFailedException("現在進行中のスプリントはありません");
		}

		//クローズ処理
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintManager manager = new SprintManager(client, api);
		SprintResult res = manager.closeSprint(currSpr.getId());

		if( res == null ){
			client.close();
			throw new ExcuteFailedException("スプリントのクローズ処理が失敗しました");
		}

		client.close();
		return currSpr.getId();
	}
}
