package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;

public class SprintResultProvider {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintResultProvider prov = new SprintResultProvider(client);
		List<SprintDataContainer> list = prov.getLatestSprintResultsByBoardId
			("57ab33677fd33ec535cc4f28", null, -1);
		list.forEach((c -> System.out.println(c.getSprintId() + " " + c.isClosed())));
		client.close();
	}

	private SprintsDB sdb;
	private SprintResultsDB srdb;

	public SprintResultProvider(MongoClient client){
		sdb = new SprintsDB(client);
		srdb = new SprintResultsDB(client);
	}

	public List<SprintDataContainer> getLatestSprintResultsByBoardId
	(String boardId, String originSprintId, int count){
		List<Sprint> sprints = sdb.getLatestFinishedSprintByBoardId(
			boardId, originSprintId, count, new SprintDocumentConverter());

		List<SprintDataContainer> result = new ArrayList<SprintDataContainer>();
		SprintResultDocumentConverter srdc = new SprintResultDocumentConverter();
		for(int i=0; i<sprints.size(); i++){
			SprintDataContainer c = new SprintDataContainer();
			c.setSprint(sprints.get(i));
			SprintResult res = srdb.getSprintResultBySprintId(c.getSprintId(), srdc);
			c.setSprintResult(res);
			result.add(c);
		}

		return result;
	}

	public List<SprintDataContainer> getSprintResultsByBoardId(String boardId){
		return getLatestSprintResultsByBoardId(boardId, null, -1);
	}
}
