package jp.ac.oit.igakilab.tasks.db;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;

public class SprintsManageDB extends SprintsDB{
	public SprintsManageDB(MongoClient client){
		super(client);
	}

	public String createSprint(String boardId, Date beginDate, Date finishDate, List<String> cardIds)
	throws SprintsDBEditException{
		String newId = null;

		if( isValidPeriod(boardId, beginDate, finishDate) ){
			Sprint sprint = new Sprint();
			sprint.setBoardId(boardId);
			sprint.setBeginDate(beginDate);
			sprint.setFinishDate(finishDate);
			cardIds.forEach(cardId -> sprint.addTrelloCardId(cardId));

			newId = addSprint(sprint, new SprintDocumentConverter());
		}else{
			throw new SprintsDBEditException(
				SprintsDBEditException.INVALID_PERIOD, "無効な期間です");
		}

		return newId;
	}

	public <T> T getCurrentSprint(String boardId, Date point, DocumentParser<T> converter){
		Bson filter = Filters.and(
			Filters.eq("boardId", boardId),
			Filters.lte("beginDate", point),
			Filters.gte("finishDate", point),
			Filters.eq("isClosed", false)
		);
		Bson sorting = Sorts.ascending("beginDate");

		//System.out.println(new SimpleDateFormat("yyyy/MM/dd").format(point));
		for(Document doc : getCollection().find(filter)){
			System.out.println(doc.toJson());
		}

		Document doc = getCollection().find(filter).sort(sorting).first();
		if( doc == null ) return null;

		return converter.parse(doc);
	}

	public <T> T getCurrentSprint(String boardId, DocumentParser<T> converter){
		Date rounded = Sprint.roundDate(Calendar.getInstance().getTime()).getTime();
		return getCurrentSprint(boardId, rounded, converter);
	}

	public boolean closeSprint(String sprintId){
		Date nowTime = Calendar.getInstance().getTime();
		Bson filter = Filters.eq("id", sprintId);
		Bson updates = Updates.combine(
			Updates.set("isClosed", true),
			Updates.set("closedDate", Sprint.roundDate(nowTime).getTime()));

		UpdateResult result = getCollection().updateOne(filter, updates);

		return result.getModifiedCount() > 0;
	}
}
