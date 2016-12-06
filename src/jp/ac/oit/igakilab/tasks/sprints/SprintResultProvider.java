package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultCardDocumentConverter;
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

	public SprintResult getSprintResultBySprintId(String sprintId){
		if( srdb.sprintIdExists(sprintId) ){
			SprintResult result = new SprintResult(sprintId);

			//メタ情報の取得
			result.setCreatedAt(srdb.getCreatedDateBySprintId(sprintId));

			//カード情報の取得
			SprintResultCardDocumentConverter parser
				= new SprintResultCardDocumentConverter();
			for(SprintResultCard card : srdb.getSprintResultCardsBySprintId(sprintId, parser)){
				if( card != null ) result.addSprintCard(card);
			}

			return result;
		}else{
			return null;
		}
	}

	public List<SprintDataContainer> getLatestSprintResultsByBoardId
	(String boardId, String originSprintId, int count){
		List<Sprint> sprints = sdb.getLatestFinishedSprintByBoardId(
			boardId, originSprintId, count, new SprintDocumentConverter());

		List<SprintDataContainer> result = new ArrayList<SprintDataContainer>();
		SprintResultDocumentConverter srdc = new SprintResultDocumentConverter(srdb);
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

	public List<SprintDataContainer> getSprintResultsByCardMemberId(String memberId){
		List<String> ids = srdb.getSprintResultIdsByMemberId(memberId);
		List<SprintDataContainer> result = new ArrayList<SprintDataContainer>();

		for(String sid : ids){
			SprintDocumentConverter sdc = new SprintDocumentConverter();
			Sprint spr = sdb.getSprintById(sid, sdc);
			SprintResult res = getSprintResultBySprintId(sid);
			result.add(new SprintDataContainer(spr, res));
		}

		return result;
	}
}
