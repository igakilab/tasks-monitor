package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class SprintResultDocumentConverter
implements DocumentParser<SprintResult>, DocumentConverter<SprintResult>{
	SprintDocumentConverter cardConverter;

	public SprintResultDocumentConverter(){
		cardConverter = new SprintDocumentConverter();
	}

	@Override
	public SprintResult parse(Document doc){
		DocumentValuePicker picker = new DocumentValuePicker(doc);
		SprintResult data = new SprintResult(picker.getString("sprintId", null));

		picker.getArray("remainedCards").forEach(
			(card -> data.addRemainedCard(cardConverter.parseTrelloCardMembers((Document)card))));
	}
}
