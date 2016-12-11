package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultCardDocumentConverter;
import jp.ac.oit.igakilab.tasks.hubot.ChannelNotification;
import jp.ac.oit.igakilab.tasks.hubot.HubotSendMessage;
import jp.ac.oit.igakilab.tasks.hubot.NotifyTrelloCard;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.CardMembers;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManagementException;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultCard;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintEditor {
	public static int DEFAULT_DUE_HOUR = 18;
	public static boolean CLEAR_TRELLO_REMOVED_CARD = true;

	private static DebugLog logger = new DebugLog("SprintEditor", System.out);

	private MongoClient dbClient;
	private TrelloApi<Object> trelloApi;
	private HubotSendMessage hubotMsg;

	public SprintEditor(MongoClient client, TrelloApi<Object> api, HubotSendMessage msg){
		dbClient = client;
		trelloApi = api;
		hubotMsg = msg;
	}


//	private boolean addSprintCard(CardEditAsset asset, String cardId, Date dueDate, List<String> memberIds){
//		//trelloに登録
//		//メンバーID変換
//		List<String> trelloMemberIds = (memberIds != null) ? asset.ttb.getTrelloIdAll(memberIds) : null;
//		if( trelloMemberIds != null && dueDate != null ){
//			//日付、担当者両方設定
//			if( !asset.tce.setDueAndMembers(cardId, dueDate, trelloMemberIds, true) ){
//				return false;
//			}
//		}else if( trelloMemberIds != null ){
//			//担当者のみ設定
//			for(String tmid : trelloMemberIds){
//				if( !asset.tce.addMember(cardId, tmid) ){
//					return false;
//				}
//			}
//		}else if( dueDate != null ){
//			//期日のみ設定
//			if( !asset.tce.setDueDate(cardId, dueDate) ){
//				return false;
//			}
//			if( !asset.tce.setDueComplete(cardId, false) ){
//				return false;
//			}
//		}
//
//		return true;
//	}

//	private boolean removeSprintCard(CardEditAsset asset, String cardId, boolean trelloParamClear){
//		//データベース変更
//		if( asset.sdb.isTrelloCardIdRegisted(asset.sprintId, cardId) ){
//			if( !asset.sdb.removeTrelloCardId(asset.sprintId, cardId) ){
//				return false;
//			}
//		}
//
//		//trelloのデータクリア
//		if( trelloParamClear ){
//			if( !asset.tce.setDueAndMembers(cardId, null, Arrays.asList()) ){
//				return false;
//			}
//		}
//
//		return true;
//	}


	/**
	 * スプリントを新規作成します。
	 * - スプリントデータベースに問い合わせ
	 * - データベースにスプリントを追加
	 * - カードを追加
	 * -   データベースにカードを追加
	 * -   trelloに担当者と期限を設定
	 * - slack通知
	 * @param boardId
	 * @param beginDate
	 * @param endDate
	 * @param trelloCardIds
	 * @return
	 */
	public String createSprint
	(String boardId, Date beginDate, Date finishDate, List<CardMembers> sprintCards)
	throws SprintEditException{
		//*****
		//データベースに問い合わせ
		//*****
		logger.log("createSprint", "スプリントを新規登録します (BoardId:" + boardId + ")");

		//ボード存在チェック
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		if( !bdb.boardIdExists(boardId) ){
			throw new SprintEditException("ボードが登録されていません");
		}

		//スプリント進行中チェック
		SprintsManageDB smdb = new SprintsManageDB(dbClient);
		if( smdb.getCurrentSprint(boardId, new SprintDocumentConverter()) != null ){
			throw new SprintEditException("進行中のスプリントがあります");
		}

		//スプリント期間チェック
		//日付データの正規化
		beginDate = Sprint.roundDate(beginDate).getTime();
		finishDate = Sprint.roundDate(finishDate).getTime();
		if( !smdb.isValidPeriod(boardId, beginDate, finishDate) ){
			throw new SprintEditException("不正な機関です");
		}


		//*****
		//スプリントデータ登録
		//*****

		//データベースに登録
		SprintManager manager = new SprintManager(dbClient);
		String newId;
		try{
			newId = manager.createSprint(boardId, beginDate, finishDate, sprintCards);
		}catch(SprintManagementException e0){
			throw new SprintEditException("DB登録エラー: " + e0.getMessage());
		}


		//*****
		//Trelloに期限等を追加
		//*****
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(dbClient);
		TrelloCardEditor tce = new TrelloCardEditor(trelloApi);

		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(finishDate);
		dueDate.set(Calendar.HOUR, 18);

		for(CardMembers card : sprintCards){
			List<String> trelloMemberIds = ttb.getTrelloIdAll(card.getMemberIds());

			tce.setDueAndMembers(card.getCardId(), dueDate.getTime(), trelloMemberIds, true);
		}


		//*****
		//Slackに通知を送信する
		//*****
		if( hubotMsg != null && bdb.getSlackNotifyEnabled(boardId) ){
			//ボードデータを取得
			TrelloBoardFetcher fetcher = new TrelloBoardFetcher(trelloApi, boardId);
			TrelloBoard board = fetcher.getBoard();
			if( !fetcher.fetch() ) System.err.println("ボードデータの同期に失敗しました");

			//notificationの初期化
			ChannelNotification notifer = new ChannelNotification(hubotMsg);

			//通知カードの整理
			List<NotifyTrelloCard> ncards = new ArrayList<NotifyTrelloCard>();
			sprintCards.forEach((cm) -> {
				TrelloCard c = board.getCardById(cm.getCardId());
				if( c != null ) ncards.add(NotifyTrelloCard.getInstance(c, board, ttb));
			});

			//通知
			notifer.sprintBeginNotification(board.getName(), beginDate, finishDate, ncards);
		}

		//処理終了
		//スプリントのIDを返却する
		logger.log("createSprint", "処理が完了しました (BoardId:" + boardId + ", SprintId:" + newId + ")");
		return newId;
	}


	/**
	 * 指定されたスプリントの期日と対象カードを変更します
	 * - db問い合わせ
	 * - 期日の変更(必要なら)
	 * - カードの追加と削除(必要なら)
	 * @param sprintId
	 * @param finishedDate
	 * @param sprintCards
	 * @throws SprintEditException
	 */
	public void updateSprint
	(String sprintId, Date finishDate, List<CardMembers> sprintCards)
	throws SprintEditException{
		//*****
		//DB登録
		//*****
		logger.log("updateSprint", "スプリントを更新します (SprintId:" + sprintId + ")");

		SprintManager manager = new SprintManager(dbClient);
		try{
			manager.updateSprint(sprintId, finishDate, sprintCards);
		}catch(SprintManagementException e0){
			throw new SprintEditException("DB登録エラー: " + e0.getMessage());
		}


		//*****
		//カードの追加と削除
		//*****

		SprintsDB sdb = new SprintsDB(dbClient);
		TrelloCardEditor tce = new TrelloCardEditor(trelloApi);
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(dbClient);
		if( sprintCards != null ){
			//削除カードを分別
			Sprint sprint = sdb.getSprintById(sprintId, new SprintDocumentConverter());
			List<String> removed = getRemovedCards(sprint.getTrelloCardIds(), sprintCards);

			//カードを追加
			for(CardMembers cm : sprintCards){
				List<String> trelloMemberIds = ttb.getTrelloIdAll(cm.getMemberIds());
				tce.setDueAndMembers(cm.getCardId(), finishDate, trelloMemberIds, true);
			}

			//カードを削除
			for(String cid : removed){
				tce.clearDueAndMembers(cid);
			}
		}

		//処理終了
		logger.log("updateSprint", "処理が完了しました (SprintId:" + sprintId + ")");
		return;
	}


	/**
	 * 指定されたスプリントIDのスプリントをクローズします
	 * - db問い合わせ
	 * - dbクローズ処理
	 * - trelloにcompleteを貼る
	 * @param id
	 * @return
	 * @throws SprintEditException
	 */
	public boolean closeSprint(String sprintId)
	throws SprintEditException{
		//*****
		//データベースに問い合わせ
		//*****
		logger.log("closeSprint", "スプリントをクローズします (SprintId:" + sprintId + ")");

		//dbインスタンス初期化
		SprintsDB sdb = new SprintsDB(dbClient);

		Sprint sprint = sdb.getSprintById(sprintId, new SprintDocumentConverter());

		//スプリント存在チェック
		if( sprint == null ){
			throw new SprintEditException("スプリントが存在しません");
		}
		//スプリント進行中チェック
		if( sprint.isClosed() ){
			throw new SprintEditException("スプリントはすでに閉じられています");
		}


		//*****
		//クローズ処理
		//*****

		//dbインスタンス初期化
		SprintManager manager = new SprintManager(dbClient);
		TrelloBoardFetcher fetcher = new TrelloBoardFetcher(trelloApi, sprint.getBoardId());
		TrelloBoard board = fetcher.getBoard();
		TrelloCardFetcher cf = new TrelloCardFetcher(trelloApi);

		//クローズ処理
		fetcher.fetch();
		if( !manager.closeSprint(board, cf, sprint.getId()) ){
			throw new SprintEditException("リザルトの登録に失敗しました");
		}



		//*****
		//TrelloにCompleteをつける
		//*****

		//クライアント初期化
		TrelloCardEditor tce = new TrelloCardEditor(trelloApi);

		//完了カードを取得
		SprintResultsDB srdb = new SprintResultsDB(dbClient);
		SprintResultCardDocumentConverter converter = new SprintResultCardDocumentConverter();
		List<SprintResultCard> finishedCards =
			srdb.getFinishedCardsBySprintId(sprint.getId(), converter);
		//System.out.println("FINISHED CARDS");
		//finishedCards.forEach((sr -> System.out.println(sr.getCardId())));

		//completeを付加
		for(SprintResultCard cr : finishedCards){
			tce.setDueComplete(cr.getCardId(), true);
		}


		//処理終了
		logger.log("closeSprint", "処理が完了しました (SprintId:" + sprintId + ")");
		return true;
	}


	/**
	 * cardsに指定されたカードと担当者情報を進行中のスプリントに追加します
	 * @param sprintId
	 * @param cards
	 * @return
	 * @throws SprintEditException
	 */
	public boolean addSprintCards(String sprintId, List<CardMembers> cards)
	throws SprintEditException{
		//スプリント取得
		SprintsDB sdb = new SprintsDB(dbClient);
		Sprint sprint = sdb.getSprintById(sprintId, new SprintDocumentConverter());
		if( sprint == null || sprint.isClosed() ){
			throw new SprintEditException("対象のスプリントが見つかりません: " + sprintId);
		}

		//データベース登録
		SprintManager manager = new SprintManager(dbClient);
		try{
			if( !manager.addSprintCards(sprintId, cards) ){
				throw new SprintEditException("DB登録に失敗しました");
			}
		}catch(SprintManagementException e0){
			throw new SprintEditException("DB登録エラー: " + e0.getMessage());
		}

		//Trelloに設定
		TrelloCardEditor tce = new TrelloCardEditor(trelloApi);
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(dbClient);
		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(sprint.getFinishDate());
		dueDate.set(Calendar.HOUR, 18);
		cards.forEach((cm) -> {
			List<String> trelloIds = ttb.getTrelloIdAll(cm.getMemberIds());
			tce.setDueAndMembers(cm.getCardId(), dueDate.getTime(), trelloIds);
		});

		return true;
	}


	public List<String> getRemovedCards
	(List<String> registedCards, List<CardMembers> newCards){
		//削除されたカードを検索
		List<String> removed = new ArrayList<String>();
		for(String cid : registedCards){
			boolean flg = false;
			for(CardMembers cm : newCards){
				if( cid.equals(cm.getCardId()) ){
					flg = true; break;
				}
			}
			if( !flg ) removed.add(cid);
		}

		return removed;
	}
}
