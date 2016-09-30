package jp.ac.oit.igakilab.tasks.sprints;

import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.BoardDBDriver;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class SprintManager {
	private MongoClient dbClient;
	//private TrelloApi<Object> trelloApi;

	public SprintManager(MongoClient dbClient, TrelloApi<Object> api){
		this.dbClient = dbClient;
		//this.trelloApi = api;
	}

	public String createSprint(String boardId, Date beginDate, Date finishDate, List<String> cardIds)
	throws SprintManagementException{
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
		try{
			smdb.createSprint(boardId, beginDate, finishDate, cardIds);
		}catch(SprintsDBEditException e0){
			throw new SprintManagementException("DB登録エラー: " + e0.getMessage());
		}

		//Trelloの担当者と期限を設定
		//TrelloCardEditor tceditor = new TrelloCardEditor(trelloApi);
		//for(String cardId : cardIds){
		//}


		//TODO 未完成
		return null;


	}



}
