package jp.ac.oit.igakilab.tasks.db;

import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;

public class SprintsManageDB extends SprintsDB{
	public SprintsManageDB(MongoClient client){
		super(client);
	}

	public String createSprint(String boardId, Date beginDate, Date finishDate)
	throws SprintsDBEditException{
		String newId = null;

		if( isValidPeriod(boardId, beginDate, finishDate) ){
			Sprint sprint = new Sprint();
			sprint.setBoardId(boardId);
			sprint.setBeginDate(beginDate);
			sprint.setFinishDate(finishDate);

			newId = addSprint(sprint, new SprintDocumentConverter());
		}else{
			throw new SprintsDBEditException(
				SprintsDBEditException.INVALID_PERIOD, "無効な期間です");
		}

		return newId;
	}

	public <T> T getCurrentSprint(String boardId, Date point, DocumentConverter<T> converter){
		Bson filter = Filters.and(
			Filters.eq("boardId", boardId),
			Filters.and(
				Filters.gte("beginDate", point),
				Filters.lte("finishDate", point)
			),
			Filters.eq("isClosed", true)
		);
		Bson sorting = Sorts.ascending("beginDate");

		Document doc = getCollection().find(filter).sort(sorting).first();

		return converter.parse(doc);
	}

	public boolean closeSprint(String sprintId){
		Bson filter = Filters.eq("id", sprintId);
		Bson updates = Updates.set("isClosed", true);

		UpdateResult result = getCollection().updateOne(filter, updates);

		return result.getModifiedCount() > 0;
	}
}
