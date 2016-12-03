package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
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
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultCard;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintEditor {
	public static int DEFAULT_DUE_HOUR = 18;
	public static boolean CLEAR_TRELLO_REMOVED_CARD = true;

	static class CardEditAsset{
		public SprintsDB sdb;
		public TrelloCardEditor tce;
		public MemberTrelloIdTable ttb;
		public String sprintId;

		public CardEditAsset
		(SprintsDB sdb, TrelloCardEditor tce, MemberTrelloIdTable ttb, String sprintId){
			this.sdb = sdb;
			this.tce = tce;
			this.ttb = ttb;
			this.sprintId = sprintId;
		}
	}

	private MongoClient dbClient;
	private TrelloApi<Object> trelloApi;
	private HubotSendMessage hubotMsg;

	public SprintEditor(MongoClient client, TrelloApi<Object> api, HubotSendMessage msg){
		dbClient = client;
		trelloApi = api;
		hubotMsg = msg;
	}


	private boolean addSprintCard(CardEditAsset asset, String cardId, Date dueDate, List<String> memberIds){
		//データベース登録
		if( !asset.sdb.isTrelloCardIdRegisted(asset.sprintId, cardId) ){
			if( !asset.sdb.addTrelloCardId(asset.sprintId, cardId) ){
				return false;
			}
		}

		//trelloに登録
		//メンバーID変換
		List<String> trelloMemberIds = (memberIds != null) ? asset.ttb.getTrelloIdAll(memberIds) : null;
		if( trelloMemberIds != null && dueDate != null ){
			//日付、担当者両方設定
			if( !asset.tce.setDueAndMembers(cardId, dueDate, trelloMemberIds, true) ){
				return false;
			}
		}else if( trelloMemberIds != null ){
			//担当者のみ設定
			for(String tmid : trelloMemberIds){
				if( !asset.tce.addMember(cardId, tmid) ){
					return false;
				}
			}
		}else if( dueDate != null ){
			//期日のみ設定
			if( !asset.tce.setDueDate(cardId, dueDate) ){
				return false;
			}
			if( !asset.tce.setDueComplete(cardId, false) ){
				return false;
			}
		}

		return true;
	}

	private boolean removeSprintCard(CardEditAsset asset, String cardId, boolean trelloParamClear){
		//データベース変更
		if( asset.sdb.isTrelloCardIdRegisted(asset.sprintId, cardId) ){
			if( !asset.sdb.removeTrelloCardId(asset.sprintId, cardId) ){
				return false;
			}
		}

		//trelloのデータクリア
		if( trelloParamClear ){
			if( !asset.tce.setDueAndMembers(cardId, null, Arrays.asList()) ){
				return false;
			}
		}

		return true;
	}


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

		//カード一覧作成
		List<String> cardIds = new ArrayList<String>();
		sprintCards.forEach(cm -> cardIds.add(cm.getCardId()));
		//データベースに登録
		String newId;
		try{
			newId = smdb.createSprint(boardId, beginDate, finishDate, cardIds);
		}catch(SprintsDBEditException e0){
			throw new SprintEditException("DB登録エラー: " + e0.getMessage());
		}


		//*****
		//データベースにカードを追加
		//*****

		//オブジェクト初期化
		TrelloCardEditor tceditor = new TrelloCardEditor(trelloApi);
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(dbClient);
		CardEditAsset asset = new CardEditAsset(smdb, tceditor, ttb, newId);

		//期限の生成
		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(finishDate);
		dueDate.set(Calendar.HOUR, DEFAULT_DUE_HOUR);

		//スプリントにカードを追加
		for(CardMembers cm : sprintCards){
			if( !addSprintCard(asset, cm.getCardId(), dueDate.getTime(), cm.getMemberIds()) ){
				throw new SprintEditException("スプリントカードの追加に失敗しました");
			}
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
		//DB問い合わせ
		//*****

		//DBインスタンス初期化
		SprintsDB sdb = new SprintsDB(dbClient);

		//スプリント存在確認
		if( !sdb.sprintIdExists(sprintId) ){
			throw new SprintEditException("スプリントIDが不正です");
		}

		//日付変更チェック
		if( finishDate != null ){
			//日付正規化
			finishDate = Sprint.roundDate(finishDate).getTime();
			//DBチェック
			if( !sdb.canUpdateFinishDate(sprintId, finishDate) ){
				throw new SprintEditException("期間が不正です");
			}
		}


		//*****
		//期日の変更
		//*****

		//DBに登録
		if( finishDate != null ){
			if( !sdb.updateFinishDate(sprintId, finishDate) ){
				throw new SprintEditException("期日の変更に失敗しました");
			}
		}


		//*****
		//カードの追加と削除
		//*****

		if( sprintCards != null ){
			//追加カードと削除カードを分別
			Sprint sprint = sdb.getSprintById(sprintId, new SprintDocumentConverter());
			List<String> removed = new ArrayList<String>();
			CardEditAsset asset =
				new CardEditAsset(sdb, new TrelloCardEditor(trelloApi), new MemberTrelloIdTable(dbClient), sprintId);
			for(String cid : sprint.getTrelloCardIds()){
				boolean flg = false;
				for(CardMembers cm : sprintCards){
					if( cid.equals(cm.getCardId()) ){
						flg = true; break;
					}
				}
				if( !flg ) removed.add(cid);
			}

			//カードを追加
			for(CardMembers cm : sprintCards){
				addSprintCard(asset, cm.getCardId(), finishDate, cm.getMemberIds());
			}

			//カードを削除
			for(String cid : removed){
				removeSprintCard(asset, cid, CLEAR_TRELLO_REMOVED_CARD);
			}
		}

		//処理終了
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
		SprintManager manager = new SprintManager(dbClient, trelloApi);

		//クローズ処理
		if( !manager.closeSprint(sprint.getId()) ){
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

		//completeを付加
		for(SprintResultCard cr : finishedCards){
			tce.setDueComplete(cr.getCardId(), true);
		}


		//処理終了
		return true;
	}
}
