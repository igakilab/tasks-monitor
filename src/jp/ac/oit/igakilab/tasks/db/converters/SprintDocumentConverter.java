package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class SprintDocumentConverter
implements DocumentConverter<Sprint>, DocumentParser<Sprint>{

	@Override
	public Sprint parse(Document doc) {
		Sprint sprint = new Sprint(doc.getString("id"));
		DocumentValuePicker picker = new DocumentValuePicker(doc);
		sprint.setBoardId(picker.getString("boardId", null));
		sprint.setBeginDate(picker.getDate("beginDate", null));
		sprint.setFinishDate(picker.getDate("finishDate", null));
		sprint.setClosedDate(picker.getDate("closedDate", null));
		picker.getStringArray("trelloCardIds").forEach((cardId) ->
			sprint.addTrelloCardId(cardId));

		return sprint;
	}

	@Override
	public Document convert(Sprint data) {
		Document doc = new Document();
		doc.append("id", data.getId());
		doc.append("boardId", data.getBoardId());
		doc.append("beginDate", data.getBeginDate());
		doc.append("finishDate", data.getFinishDate());
		if( data.getClosedDate() != null )
			doc.append("closedDate", data.getClosedDate());
		doc.append("trelloCardIds", data.getTrelloCardIds());

		return doc;
	}

}
