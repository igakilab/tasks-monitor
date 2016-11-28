package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class SprintManager {
	public static boolean DEBUG = false;

	private MongoClient dbClient;
	private TrelloApi<Object> trelloApi;
	private SprintDocumentConverter converter;

	/**
	 * スプリントマネージャを初期化します
	 * @param dbClient MongoClient
	 * @param api TrelloApiClient
	 */
	public SprintManager(MongoClient dbClient, TrelloApi<Object> api){
		this.dbClient = dbClient;
		this.trelloApi = api;
		this.converter = new SprintDocumentConverter();
	}

	/**
	 * トレロのカードに期限とメンバーを一度に設定します
	 * @param tce
	 * @param trelloCardId
	 * @param dueDate
	 * @param memberIds
	 * @param ttb
	 * @return
	 */
	protected boolean setTrelloCardDueAndMembers
	(TrelloCardEditor tce, String trelloCardId,
	Date dueDate, List<String> memberIds, MemberTrelloIdTable ttb){
		//メンバーid変換
		List<String> trelloMemberIds = new ArrayList<String>();
		memberIds.forEach((mid) -> {
			String tmid = ttb.getTrelloId(mid);
			if( tmid != null ){
				trelloMemberIds.add(tmid);
			}
		});
		//処理
		return tce.setDueAndMembers(trelloCardId, dueDate, trelloMemberIds);
	}

	/**
	 * 新しくスプリントを作成し、dbに登録、trelloにカードを設定します
	 * @param boardId
	 * @param beginDate
	 * @param finishDate
	 * @param cardAndMembers
	 * @return
	 * @throws SprintManagementException
	 */
	public String createSprint(String boardId,
		Date beginDate, Date finishDate, List<CardMembers> cardAndMembers)
	throws SprintManagementException
	{
		TrelloBoardsDB bdb = new TrelloBoardsDB(dbClient);
		SprintsManageDB smdb = new SprintsManageDB(dbClient);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			throw new SprintManagementException("ボードが登録されていません");
		}

		//期間のチェック
		beginDate = Sprint.roundDate(beginDate).getTime();
		finishDate = Sprint.roundDate(finishDate).getTime();
		if( !smdb.isValidPeriod(boardId, beginDate, finishDate) ){
			throw new SprintManagementException("不正な期間です");
		}

		//Sprintのデータを登録
		List<String> cardIds = new ArrayList<String>();
		cardAndMembers.forEach(tcm -> cardIds.add(tcm.getCardId()));
		String newId;
		try{
			newId = smdb.createSprint(boardId, beginDate, finishDate, cardIds);
		}catch(SprintsDBEditException e0){
			throw new SprintManagementException("DB登録エラー: " + e0.getMessage());
		}

		//Trelloの担当者と期限を設定
		TrelloCardEditor tceditor = new TrelloCardEditor(trelloApi);
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(dbClient);
		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(finishDate);
		dueDate.set(Calendar.HOUR, 18);
		for(CardMembers cm : cardAndMembers){
			//カードIDを読み出し
			String cardId= cm.getCardId();
			//カードに期限と担当者を設定
			setTrelloCardDueAndMembers(tceditor, cardId, dueDate.getTime(), cm.getMemberIds(), mtable);
			//dueCompleteを解除
			tceditor.setDueComplete(cardId, false);
		}

		return newId;
	}


	public String updateSprint(String sprintId, Date finishDate, List<CardMembers> cardAndMembers)
	throws SprintManagementException
	{
		SprintsManageDB smdb = new SprintsManageDB(dbClient);
		SprintDocumentConverter converter = new SprintDocumentConverter();

		//スプリントのチェック
		Sprint sprint = smdb.getSprintById(sprintId, converter);
		if( sprint == null ){
			throw new SprintManagementException("既存スプリントが見つかりません");
		}

		//終了日の設定
		if( finishDate != null ){
			if( sprint.getFinishDate().compareTo(Sprint.roundDate(finishDate).getTime()) != 0 ){
				//終了日のチェックには期間の有効判定が必要である
				throw new SprintManagementException("終了日の変更はサポートしていません");
			}
		}

		//カードの設定
		if( cardAndMembers != null ){
			sprint.clearTrelloCardId();
			cardAndMembers.forEach((cam -> sprint.addTrelloCardId(cam.getCardId())));
		}

		//Sprintのデータを登録
		smdb.updateSprintCards(sprint.getId(), sprint.getTrelloCardIds());


		//Trelloの担当者と期限を設定
		TrelloCardEditor tceditor = new TrelloCardEditor(trelloApi);
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(dbClient);
		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(sprint.getFinishDate());
		dueDate.set(Calendar.HOUR, 18);
		for(CardMembers cm : cardAndMembers){
			//カードIDを読み出し
			String cardId= cm.getCardId();
			//カードに期限と担当者を設定
			setTrelloCardDueAndMembers(tceditor, cardId, dueDate.getTime(), cm.getMemberIds(), mtable);
			//dueCompleteを解除
			tceditor.setDueComplete(cardId, false);
		}

		return sprint.getId();
	}

	public SprintResult closeSprint(String sprintId){
		SprintsManageDB smdb = new SprintsManageDB(dbClient);
		SprintResultsDB resdb = new SprintResultsDB(dbClient);
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(dbClient);

		//現在のスプリントの情報を取得
		Sprint currSpr = smdb.getSprintById(sprintId, converter);
		if( currSpr == null ){
			//throw new SprintManagementException("スプリントが見つかりません");
			return null;
		}
		if( currSpr.isClosed() ){
			//throw new SprintManagementException("スプリントはすでにクローズされています");
			return null;
		}

		//TrelloBoardを取得
		List<TrelloAction> actions = adb.getTrelloActions(currSpr.getBoardId(), new TrelloActionDocumentParser());
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();
		if( !currSpr.getBoardId().equals(board.getId()) ){
			//throw new SprintManagementException("ボードのビルドに失敗しました");
			return null;
		}

		//sprintResultを生成
		SprintResult result = new SprintResult(currSpr.getId());

		//カードをtrelloCardMembersに変換、完了カードとそれ以外に振り分け
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(dbClient);
		for(String cardId : currSpr.getTrelloCardIds()){
			TrelloCard c = board.getCardById(cardId);
			if( c != null ){
				TrelloList list = board.getListById(c.getListId());
				if( list != null ){
					if( list.getName().matches(TasksTrelloClientBuilder.REGEX_DONE) ){
						result.addSprintCard(
							CardResult.getInstance(c, mtable, true));
					}else if(
						list.getName().matches(TasksTrelloClientBuilder.REGEX_DOING) ||
						list.getName().matches(TasksTrelloClientBuilder.REGEX_TODO)
					){
						result.addSprintCard(
							CardResult.getInstance(c, mtable, false));
					}
				}
			}
		}

		//完了カードにdueCompleteを付加する
		TrelloCardEditor editor = new TrelloCardEditor(trelloApi);
		result.getAllCards().forEach((cr) -> {
			if( cr.isFinished() ){
				editor.setDueComplete(cr.getCardId(), true);
			}
		});

		//スプリントをクローズ
		smdb.closeSprint(currSpr.getId());

		//SprintResultを記録
		result.setCreatedAt(Calendar.getInstance().getTime());
		resdb.addSprintResult(result, new SprintResultDocumentConverter());

		return result;
	}

	public List<Sprint> getSprintsByBoardId(String boardId){
		SprintsDB sdb = new SprintsDB(dbClient);

		return sdb.getSprintsByBoardId(boardId, converter);
	}

	public List<SprintResult> getSprintResultsByBoardId(String boardId){
		SprintsDB sdb = new SprintsDB(dbClient);
		SprintResultsDB srdb = new SprintResultsDB(dbClient);

		//sprintsDBからボードIDに該当するスプリントIDの一覧を取得し、
		//sprintResultsDBからSprintResultを取得する
		List<SprintResult> results = srdb.getSprintResultsBySprintIds(
			sdb.getSprintIdsByBoardId(boardId), new SprintResultDocumentConverter());

		return results;
	}
}
