package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardEditor;
import jp.ac.oit.igakilab.tasks.trello.TrelloDateFormat;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class SprintManager {
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
				String trelloId = mtable.get(memberId);
				if( trelloId != null ){
					System.out.format("TCEDIT: addMember %s(%s) to %s\n",
						memberId, trelloId, cardId);
					tceditor.addMember(cardId, trelloId);
				}else{
					System.out.format("TCEDIT: addMember FAILED {memberId:%s} to %s\n",
						memberId, cardId);
				}
			}

			//期限を設定
			System.out.format("TCEDIT: addDue %s to %s",
					new TrelloDateFormat().format(dueDate.getTime()), cardId);
			tceditor.setDueDate(cardId, dueDate.getTime());
		}

		return newId;
	}
}
