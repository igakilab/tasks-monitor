package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintResultForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintManager;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class SprintFinisher {
	public SprintResultForm closeCurrentSprint(String boardId)
	throws ExcuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在進行中のスプリントを取得
		Sprint currSpr = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		if( currSpr == null ){
			throw new ExcuteFailedException("現在進行中のスプリントはありません");
		}

		//クローズ処理
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintManager manager = new SprintManager(client, api);
		SprintResult res = manager.closeSprint(currSpr.getId());

		if( res == null ){
			throw new ExcuteFailedException("スプリントのクローズ処理が失敗しました");
		}

		client.close();
		return SprintResultForm.getInstance(res);
	}

	public List<SprintResultForm> getSprintResultsByBoardId(String boardId){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintManager manager = new SprintManager(client, null);

		//返却する配列を初期化
		List<SprintResultForm> forms = new ArrayList<SprintResultForm>();

		//結果を取得、変換して返却配列に格納
		manager.getSprintResultsByBoardId(boardId).forEach((result ->
			forms.add(SprintResultForm.getInstance(result))));

		//結果を返却
		client.close();
		return forms;
	}
}
