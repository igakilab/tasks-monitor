package jp.ac.oit.igakilab.tasks.dwr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB.SprintsDBEditException;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;

public class SprintRegister {
	public String testCreateSprint(String boardId, String bDateStr, String fDateStr, List<String> cardIds)
	throws SprintsDBEditException, ExecuteFailedException, ParseException{
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		Date beginDate = df.parse(bDateStr);
		Date finishDate = df.parse(fDateStr);
		if( beginDate == null || finishDate == null ){
			throw new ExecuteFailedException("dateの変換に失敗");
		}

		Sprint sprint = new Sprint();
		sprint.setBoardId(boardId);
		sprint.setBeginDate(beginDate);
		sprint.setFinishDate(finishDate);
		if( cardIds != null ) cardIds.forEach(cardId -> sprint.addTrelloCardId(cardId));

		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsDB sdb = new SprintsDB(client);

		String newID = sdb.addSprint(sprint, new SprintDocumentConverter());

		client.close();
		return newID;
	}

	public List<SprintForm> getSprintListByBoardId(String boardId){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsDB sdb = new SprintsDB(client);

		List<SprintForm> sprints = new ArrayList<SprintForm>();
		sdb.getSprintsByBoardId(boardId, new SprintDocumentConverter()).forEach(
			(data -> sprints.add(SprintForm.getInstance(data)))
		);

		client.close();
		return sprints;
	}
}
