package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.db.DocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;

public class SprintDocumentConverter
implements DocumentConverter<Sprint>{

	@Override
	public Sprint parse(Document doc) {
		Sprint sprint = new Sprint(doc.getString("id"));
		sprint.setBoardId(doc.getString("boardId"));
		sprint.setBeginDate(doc.getDate("beginDate"));
		sprint.setFinishDate(doc.getDate("finishDate"));
		sprint.setClosed(doc.getBoolean("isClosed", false));

		return sprint;
	}

	@Override
	public Document convert(Sprint data) {
		Document doc = new Document();
		doc.append("id", data.getId());
		doc.append("boardId", data.getBoardId());
		doc.append("beginDate", data.getBeginDate());
		doc.append("finishDate", data.getFinishDate());
		doc.append("isClosed", data.isClosed());

		return doc;
	}

}
