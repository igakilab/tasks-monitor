package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultCardDocumentConverter;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardFetcher;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionRawData;

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

	/**
	 * スプリントをクローズします
	 * - sprintDBのクローズ
	 * - sprintResultの生成
	 * @param sprintId
	 * @return
	 */
	public boolean closeSprint(String sprintId){
		SprintsManageDB smdb = new SprintsManageDB(dbClient);
		SprintResultsDB resdb = new SprintResultsDB(dbClient);
		//現在のスプリントの情報を取得
		Sprint currSpr = smdb.getSprintById(sprintId, converter);
		if( currSpr == null ){
			//throw new SprintManagementException("スプリントが見つかりません");
			return false;
		}
		if( currSpr.isClosed() ){
			//throw new SprintManagementException("スプリントはすでにクローズされています");
			return false;
		}

		//TrelloBoardを取得
		TrelloBoardFetcher bfetcher = new TrelloBoardFetcher(trelloApi, currSpr.getBoardId());
		TrelloBoard board = bfetcher.getBoard();
		if( !bfetcher.fetch() ){
			//throw new SprintManagementException("ボードのビルドに失敗しました");
			return false;
		}

		//sprintResultを生成
		resdb.createSprintResult(sprintId, null);

		//カードを検査し、DBに追加
		TrelloCardFetcher cfetcher = new TrelloCardFetcher(trelloApi);
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(dbClient);
		SprintResultCardDocumentConverter srConverter =
			new SprintResultCardDocumentConverter();
		for(String cid : currSpr.getTrelloCardIds()){
			TrelloCard card = board.getCardById(cid);
			TrelloList list = card != null ? board.getListById(card.getListId()) : null;

			if( card != null && list != null ){
				//メンバーIDと完了フラグの解析
				List<String> memberIds = ttb.getMemberIdAll(card.getMemberIds());
				boolean finished = list.getName().matches(TasksTrelloClientBuilder.REGEX_DONE);

				//アクションデータの解析
				List<JSONObject> actions = cfetcher.getCardActions(cid);
				List<TrelloActionRawData> converted = new ArrayList<TrelloActionRawData>();
				if( actions != null ){
					actions.forEach(
						(act -> converted.add(new TrelloActionRawData.JSONObjectModel(act))));
				}else{
					System.err.println("カードアクションが取得できませんでした: " + card.toString());
					continue;
				}

				//データ生成とDB登録
				SprintResultCard srCard = new SprintResultCard();
				srCard.setSprintId(sprintId);
				srCard.setCardId(cid);
				srCard.setFinished(finished);
				srCard.setMemberIds(memberIds);
				srCard.setTrelloActions(converted);

				resdb.addSprintResultCard(srCard, srConverter);
			}
		}

		//スプリントをクローズ
		smdb.closeSprint(currSpr.getId());

		return true;
	}

	public List<Sprint> getSprintsByBoardId(String boardId){
		SprintsDB sdb = new SprintsDB(dbClient);

		return sdb.getSprintsByBoardId(boardId, converter);
	}

	@Deprecated
	public List<SprintResult> getSprintResultsByBoardId(String boardId){
		SprintResultProvider provider = new SprintResultProvider(dbClient);

		List<SprintResult> list = new ArrayList<>();
		provider.getSprintResultsByBoardId(boardId).forEach((c -> list.add(c.getSprintResult())));

		return list;
	}
}
