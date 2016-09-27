package jp.ac.oit.igakilab.tasks.dwr;

import java.util.Calendar;
import java.util.Date;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;

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
	//現在進行中のスプリントがあった場合、自動的にクローズされる
	public String createSprint(String boardId, Date finishDate)
	throws ExcuteFailedException{
		//DBのクライアントと操作クラスの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//進行中スプリントの取得
		Sprint current = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		//進行中スプリントがあった場合、クローズする
		if( current != null ){
			boolean closeRes = smdb.closeSprint(current.getId());
			//もしクローズに失敗したら、例外をスロー
			if( !closeRes ){
				client.close();
				throw new ExcuteFailedException("スプリントのクローズに失敗しました");
			}
		}

		//日付の取得
		Date today = Sprint.roundDate(Calendar.getInstance().getTime()).getTime();
		//DB登録
		String newId = null;
		try{
			newId = smdb.createSprint(boardId, today, finishDate);
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
}
