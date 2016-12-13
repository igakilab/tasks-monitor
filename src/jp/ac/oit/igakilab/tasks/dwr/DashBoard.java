package jp.ac.oit.igakilab.tasks.dwr;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.DashBoardForms;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.scripts.SprintEditException;
import jp.ac.oit.igakilab.tasks.scripts.SprintEditor;
import jp.ac.oit.igakilab.tasks.scripts.TasksCacheProviderBuilder;
import jp.ac.oit.igakilab.tasks.scripts.TrelloActionCacheProvider;
import jp.ac.oit.igakilab.tasks.scripts.TrelloActionCacheProvider.ActionCacheFetcher;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardCacheProvider;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloNumberedCard;

public class DashBoard {

	public DashBoardForms.DashBoardData getDashBoardData(String boardId)
	throws ExecuteFailedException{
		//クライアントの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		//dbの操作クラスを生成
		SprintsManageDB sdb = new SprintsManageDB(client);

		/* ボードを生成 */

		//ボードデータをフェッチ
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardCacheProvider bcp = TasksCacheProviderBuilder.getBoardCacheProvider(client, api);
		TrelloBoard board = bcp.getBoard(boardId);
		if( board == null ){
			client.close();
			throw new ExecuteFailedException("ボードのデータがありません");
		}

		/* スプリントを取得 */

		//現在日時から期間内のスプリントを取得
		Sprint sprint = sdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		//スプリントカードのアクションを取得
		if( sprint != null ){
			TrelloActionCacheProvider acp = TasksCacheProviderBuilder.getActionCacheProvider(client, api);
			ActionCacheFetcher<Object> fetcher = acp.getCardFetcher();
			for(String cid : sprint.getTrelloCardIds()){
				TrelloActionsCard card = acp.getTrelloActionsCard(cid, fetcher, false);
				if( card != null ){
					board.replaceCard(card, false);
				}
			}
		}

		/* フォームに変換 */
		DashBoardForms.DashBoardData form =
			DashBoardForms.DashBoardData.getInstance(board, sprint);

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
	throws ExecuteFailedException{
		//操作インスタンスを初期化
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(api, boardId);

		//データを取得
		if( !fetcher.fetch() ){
			throw new ExecuteFailedException("ボードデータの取得に失敗しました");
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
				throw new ExecuteFailedException("カードの追加に失敗しました");
			}
		}else{
			throw new ExecuteFailedException("todoのリストがありません");
		}

		return TrelloCardForm.getInstance(ncard);
	}

	//進行中のスプリントを終了する
	public String closeCurrentSprint(String boardId)
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在進行中のスプリントを取得
		Sprint currSpr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( currSpr == null ){
			client.close();
			throw new ExecuteFailedException("現在進行中のスプリントはありません");
		}

		//クローズ処理
		SprintEditor editor = new SprintEditor(client, api, null);
		try{
			editor.closeSprint(currSpr.getId());
		}catch(SprintEditException e0){
			client.close();
			throw new ExecuteFailedException("スプリントのクローズの処理が失敗しました: " + e0.getMessage());
		}

		client.close();
		return currSpr.getId();
	}
}
