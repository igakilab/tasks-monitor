package jp.ac.oit.igakilab.tasks.db.converters;

import java.util.ArrayList;
import java.util.List;

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
		data.setCreatedAt(picker.getDate("createdAt", null));

		picker.getArray("remainedCards").forEach(
			(card -> data.addRemainedCard(cardConverter.parseTrelloCardMembers((Document)card))));
		picker.getArray("finishedCards").forEach(
			(card -> data.addFinishedCard(cardConverter.parseTrelloCardMembers((Document)card))));

		return data;
	}

	@Override
	public Document convert(SprintResult data){
		List<Document> remainedCards = new ArrayList<Document>();
		data.getRemainedCards().forEach(
			(card -> remainedCards.add(cardConverter.convertTrelloCardMembers(card))));

		List<Document> finishedCards = new ArrayList<Document>();
		data.getFinishedCards().forEach(
			(card -> remainedCards.add(cardConverter.convertTrelloCardMembers(card))));

		return new Document("sprintId", data.getSprintId())
			.append("createdAt", data.getCreatedAt())
			.append("remainedCards", remainedCards)
			.append("finishedCards", finishedCards);
	}
}
