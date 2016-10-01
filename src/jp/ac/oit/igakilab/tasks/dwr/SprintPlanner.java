package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.DocumentTrelloActionParser;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class SprintPlanner {
	//ボードに設定された現在のスプリントの情報が返却される
	//ボードやスプリントがない場合はnullが返される
	public SprintForm getCurrentSprint(String boardId){
		//クライアントとdb操作クラスを生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在日時から期間内のスプリントを取得
		Sprint sprint = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		//取得できなかった場合はnullを返却
		if( sprint == null ){
			client.close();
			return null;
		}

		//DBをクローズ、formに変換してreturn
		client.close();
		return SprintForm.getInstance(sprint);
	}

	//スプリントを新しく生成
	//現在進行中のスプリントがあった場合、エラーを投げる
	public String createSprint(String boardId, Date finishDate, List<String> cardIds)
	throws ExcuteFailedException{
		//DBのクライアントと操作クラスの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//進行中スプリントの取得
		Sprint current = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		//進行中スプリントがあった場合、クローズする
		if( current != null ){
			throw new ExcuteFailedException("現在進行中のスプリントがあります");
		}

		//日付の取得
		Date today = Sprint.roundDate(Calendar.getInstance().getTime()).getTime();
		//DB登録
		String newId = null;
		try{
			newId = smdb.createSprint(boardId, today, finishDate,
				(cardIds != null ? cardIds : new ArrayList<String>()));
		}catch(SprintsDBEditException e0){
			client.close();
			throw new ExcuteFailedException("スプリント登録に失敗しました: " + e0.getMessage());
		}

		//DBをクローズ、登録されたidを返却
		client.close();
		return newId;
	}

	//boardIdより現在進行中のスプリントをクローズする
	//スプリントがない場合や、更新に失敗した場合はfalseを返却する
	public boolean closeCurrentSprint(String boardId){
		//DBのクライアントと操作クラスの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//進行中のスプリントを取得
		Sprint current = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		//進行中スプリントがある場合はスプリントをクローズ
		boolean res = false;
		if( current != null ){
			res = smdb.closeSprint(current.getId());
		}

		//DBをクローズ、結果を返却
		client.close();
		return res;
	}

	//ボードにあるtodoのカードリストを返却する
	//ボードやリストがない場合は空のリストが返却される
	public List<TrelloCardForm> getTodoTrelloCardList(String boardId){
		//dbのクライアントを生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		//アクションを取得
		List<TrelloAction> actions =
			adb.getTrelloActions(boardId, new DocumentTrelloActionParser());

		//カードリストの初期化
		List<TrelloCardForm> forms = new ArrayList<TrelloCardForm>();

		//ボードの解析
		if( actions.size() > 0 ){
			//ボードを生成・ビルド
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			board.build();

			//正規表現でマッチするリストのカードを取得
			board.getCardsByListNameMatches("(?i)to\\s*do").forEach(
				(card -> forms.add(TrelloCardForm.getInstance(card))));
		}

		//結果を返却
		client.close();
		return forms;
	}
}
