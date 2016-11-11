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
import jp.ac.oit.igakilab.tasks.trello.TrelloDateFormat;
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

	public SprintManager(MongoClient dbClient, TrelloApi<Object> api){
		this.dbClient = dbClient;
		this.trelloApi = api;
		this.converter = new SprintDocumentConverter();
	}

	public String createSprint(String boardId,
		Date beginDate, Date finishDate, List<TrelloCardMembers> cardAndMembers)
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
		for(TrelloCardMembers cm : cardAndMembers){
			//カードIDを読み出し
			String cardId= cm.getCardId();

			//担当者設定、mdbからtrelloIdを取得し設定
			for(String memberId : cm.getMemberIds()){
				String trelloId = mtable.getTrelloId(memberId);
				if( trelloId != null ){
					if( DEBUG )
						System.out.format("TCEDIT: addMember %s(%s) to %s\n",
								memberId, trelloId, cardId);
					tceditor.addMember(cardId, trelloId);
				}else{
					if( DEBUG )
						System.out.format("TCEDIT: addMember FAILED {memberId:%s} to %s\n",
								memberId, cardId);
				}
			}

			//期限を設定
			if( DEBUG )
				System.out.format("TCEDIT: addDue %s to %s",
					new TrelloDateFormat().format(dueDate.getTime()), cardId);
			tceditor.setDueDate(cardId, dueDate.getTime());
		}

		return newId;
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
