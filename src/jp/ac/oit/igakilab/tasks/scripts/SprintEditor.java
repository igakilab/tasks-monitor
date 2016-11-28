package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.hubot.ChannelNotification;
import jp.ac.oit.igakilab.tasks.hubot.HubotSendMessage;
import jp.ac.oit.igakilab.tasks.hubot.NotifyTrelloCard;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.CardMembers;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintEditor {
	public static int DEFAULT_DUE_HOUR = 18;

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
		List<String> trelloMemberIds = (memberIds != null) ? asset.ttb.getTrelloIdAll(memberIds) : null;
		if( trelloMemberIds != null && dueDate != null ){
			if( !asset.tce.setDueAndMembers(cardId, dueDate, trelloMemberIds, true) ){
				return false;
			}
		}else if( trelloMemberIds != null ){
			for(String tmid : trelloMemberIds){
				if( !asset.tce.addMember(cardId, tmid) ){
					return false;
				}
			}
		}else if( dueDate != null ){
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

		//trelloの変更
		if( trelloParamClear ){
			if( !asset.tce.setDueAndMembers(cardId, null, null) ){

			}
		}
	}


	/**
	 * スプリントを新規作成します。
	 * - スプリントデータベースに問い合わせ
	 * - データベースにスプリントを追加
	 * - カードを追加
	 * -   データベースにカードを追加
	 * -   trelloに担当者と期限を設定
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

		//スプリント期間チェック
		SprintsDB sdb = new SprintsDB(dbClient);
		//日付データの正規化
		beginDate = Sprint.roundDate(beginDate).getTime();
		finishDate = Sprint.roundDate(finishDate).getTime();
		if( !sdb.isValidPeriod(boardId, beginDate, finishDate) ){
			throw new SprintEditException("不正な機関です");
		}

		//*****
		//スプリントデータ登録
		//*****

		//カード一覧作成
		List<String> cardIds = new ArrayList<String>();
		sprintCards.forEach(cm -> cardIds.add(cm.getCardId()));
		//データベースに登録
		SprintsManageDB smdb = new SprintsManageDB(dbClient);
		String newId;
		try{
			newId = smdb.createSprint(boardId, beginDate, finishDate, cardIds);
		}catch(SprintsDBEditException e0){
			throw new SprintEditException("DB登録エラー: " + e0.getMessage());
		}

		//*****
		//Trelloに担当者と期限を設定
		//*****

		//オブジェクト初期化
		TrelloCardEditor tceditor = new TrelloCardEditor(trelloApi);
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(dbClient);

		//期限の生成
		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(finishDate);
		dueDate.set(Calendar.HOUR, DEFAULT_DUE_HOUR);

		//Trelloに設定
		List<String> tmp = new ArrayList<String>(); //メンバーID変換時の一時保存配列
		for(CardMembers cm : sprintCards){
			//メンバーID変換
			tmp.clear();
			cm.getMemberIds().forEach((mid) -> {
				String tmp0 = ttb.getTrelloId(mid);
				if( tmp0 != null ) tmp.add(tmp0);
			});

			//設定する
			tceditor.setDueAndMembers(cm.getCardId(), dueDate.getTime(), tmp, true);
		}

		//*****
		//Slackに通知を送信する
		//*****
		if( bdb.getSlackNotifyEnabled(boardId) ){
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
	};
}
