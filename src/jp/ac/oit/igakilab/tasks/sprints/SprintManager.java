package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
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
import jp.ac.oit.igakilab.tasks.trello.TrelloCardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionRawData;

public class SprintManager {
	public static boolean DEBUG = false;

	private MongoClient dbClient;
	private SprintDocumentConverter converter;

	/**
	 * スプリントマネージャを初期化します
	 * @param dbClient MongoClient
	 * @param api TrelloApiClient
	 */
	public SprintManager(MongoClient dbClient){
		this.dbClient = dbClient;
		this.converter = new SprintDocumentConverter();
	}

	/**
	 * 新しくスプリントを作成し、dbに登録します
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

		return newId;
	}


	public void updateSprint(String sprintId, Date finishDate, List<CardMembers> cardAndMembers)
	throws SprintManagementException
	{
		//DBインスタンス初期化
		SprintsDB sdb = new SprintsDB(dbClient);

		//スプリント存在確認
		if( !sdb.sprintIdExists(sprintId) ){
			throw new SprintManagementException("スプリントIDが不正です");
		}


		//*****
		//期日変更

		//日付変更チェック
		if( finishDate != null ){
			//日付正規化
			finishDate = Sprint.roundDate(finishDate).getTime();
			//DBチェック
			if( !sdb.canUpdateFinishDate(sprintId, finishDate) ){
				throw new SprintManagementException("期間が不正です");
			}
		}

		//期日の変更
		if( finishDate != null ){
			if( !sdb.updateFinishDate(sprintId, finishDate) ){
				throw new SprintManagementException("期日の変更に失敗しました");
			}
		}


		//*****
		//カード変更

		if( cardAndMembers != null ){
			List<String> cardIds = new ArrayList<String>();
			cardAndMembers.forEach((sc -> cardIds.add(sc.getCardId())));

			sdb.updateSprintCards(sprintId, cardIds);
		}
	}

	/**
	 * スプリントをクローズします
	 * - sprintDBのクローズ
	 * - sprintResultの生成
	 * @param sprintId
	 * @return
	 */
	public boolean closeSprint(TrelloApi<Object> trelloApi, String sprintId){
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
