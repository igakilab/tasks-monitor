package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.TrelloDateFormat;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class SprintManager {
	public static boolean DEBUG = false;

	private MongoClient dbClient;
	private TrelloApi<Object> trelloApi;

	public SprintManager(MongoClient dbClient, TrelloApi<Object> api){
		this.dbClient = dbClient;
		this.trelloApi = api;
	}

	public String createSprint(String boardId,
		Date beginDate, Date finishDate, List<TrelloCardMembers> cardAndMembers)
	throws SprintManagementException
	{
		BoardDBDriver bdb = new BoardDBDriver(dbClient);
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

	public boolean closeCurrentSprint(String boardId){
		BoardDBDriver bdb = new BoardDBDriver(dbClient);
		SprintsManageDB smdb = new SprintsManageDB(dbClient);
		SprintResultsDB resdb = new SprintResultsDB(dbClient);
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(dbClient);

		//ボードの存在チェック
		if( !bdb.boardIdExists(boardId) ){
			//throw new SprintManagementException("ボードが登録されていません");
			return false;
		}

		//現在のスプリントの情報を取得
		Sprint currSpr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( currSpr == null ){
			//throw new SprintManagementException("現在進行中のスプリントはありません");
			return false;
		}


		//TrelloBoardを取得
		List<TrelloAction> actions = adb.getTrelloActions(boardId, new DocumentTrelloActionParser());
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(actions);
		board.build();

		//sprintResultを生成
		SprintResult result = new SprintResult(currSpr.getId());

		//カードをtrelloCardMembersに変換、完了カードとそれ以外に振り分け
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(dbClient);
		for(String cardId : currSpr.getTrelloCardIds()){
			TrelloCard c = board.getCardById(cardId);
			if( c != null ){
				TrelloList list = board.getListById(c.getListId());
				if( list != null ){
					if( list.getName().matches("(?i)done") ){
						result.addFinishedCard(TrelloCardMembers.getInstance(board, mtable, c.getId()));
					}else if(
						list.getName().matches("(?i)doing") ||
						list.getName().matches("(?i)to\\s*do")
					){
						result.addFinishedCard(TrelloCardMembers.getInstance(board, mtable, c.getId()));
					}
				}
			}
		}

		//スプリントをクローズ
		smdb.closeSprint(currSpr.getId());

		//SprintResultを記録
		result.setCreatedAt(Calendar.getInstance().getTime());
		resdb.addSprintResult(result, new SprintResultDocumentConverter());

		return true;
	}
}
