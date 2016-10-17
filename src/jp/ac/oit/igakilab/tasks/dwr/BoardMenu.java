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
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardActionsUpdater;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardData;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardDataFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardUrl;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class BoardMenu {
	/*
	 * ボードデータの一覧を取得します。
	 * trelloboardsのデータベースに入っているボードを配列で返却します。
	 * ボードの基本データに最終更新日と参加メンバーのidを付加します
	 */
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

	/*
	 * trelloボードのアクションを強制的に同期します
	 */
	public boolean updateTrelloBoardActions(String boardId){
		//ボードの存在を確認
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		if( !bdb.boardIdExists(boardId) ){
			return false;
		}

		//情報をアップデート
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);
		updater.updateBoardActions(boardId, bdb.getLastUpdateDate(boardId));

		client.close();
		return true;
	}

	/*
	 * trelloのボードurlからボードidやボード名を取得します
	 */
	public TrelloBoardDataForm getBoardDataByUrl(String url)
	throws ExcuteFailedException{
		//urlを解析
		TrelloBoardUrl burl = new TrelloBoardUrl(url);
		if( !burl.isValid() ){
			throw new ExcuteFailedException("不正なURLです");
		}

		//データを取得
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardData data = burl.fetchTrelloBoardData(api);
		if( data == null ){
			throw new ExcuteFailedException("ボードデータの取得に失敗しました");
		}

		return TrelloBoardDataForm.getInstance(data);
	}


	/*
	 * ボードIDで指定されたボードを、新たに監視ボードに追加します。
	 * 追加時にボード情報の動機を自動的に行います
	 */
	public boolean addTrelloBoard(String boardId)
	throws ExcuteFailedException{
		//ボード情報の取得(ボードが取得できるかどうかテスト)
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardDataFetcher fetcher = new TrelloBoardDataFetcher(api);
		TrelloBoardData data = fetcher.getTrelloBoardData(boardId);
		if( data == null ){
			throw new ExcuteFailedException("ボードデータの取得に失敗しました");
		}

		//データベースにボードの新規登録
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		boolean res = bdb.addBoard(boardId);

		//ボード情報を同期
		TrelloBoardActionsUpdater updater = new TrelloBoardActionsUpdater(client, api);
		updater.updateBoardActions(boardId);

		client.close();
		return res;
	}
}
