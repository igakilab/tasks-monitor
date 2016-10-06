package jp.ac.oit.igakilab.tasks.db.converters;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class SprintDocumentConverter
implements DocumentConverter<Sprint>, DocumentParser<Sprint>{

	public TrelloCardMembers parseTrelloCardMembers(Document doc){
		DocumentValuePicker picker = new DocumentValuePicker(doc);
		TrelloCardMembers card = new TrelloCardMembers(picker.getString("cardId", null));
		picker.getStringArray("memberIds").forEach(
			(memberId ->card.addMemberId(memberId)));
		return card;
	}

	public Document convertTrelloCardMembers(TrelloCardMembers card){
		Document doc = new Document("cardId", card.getCardId());
		List<String> memberIds = new ArrayList<String>();
		card.getMemberIds().forEach((memberId ->
			memberIds.add(memberId)));
		doc.append("memberIds", memberIds);
		return doc;
	}

	@Override
	public Sprint parse(Document doc) {
		Sprint sprint = new Sprint(doc.getString("id"));
		DocumentValuePicker picker = new DocumentValuePicker(doc);
		sprint.setBoardId(picker.getString("boardId", null));
		sprint.setBeginDate(picker.getDate("beginDate", null));
		sprint.setFinishDate(picker.getDate("finishDate", null));
		sprint.setClosedDate(picker.getDate("closedDate", null));
		if( doc.containsKey("trelloCardIds") ){
			picker.getStringArray("trelloCardIds").forEach(
				(cardId -> sprint.addTrelloCard(
					new TrelloCardMembers(cardId))));
		}else if( doc.containsKey("trelloCards") ){
			picker.getArray("trelloCards").forEach(
				(card -> sprint.addTrelloCard(
					parseTrelloCardMembers((Document)card))));
		}

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

		ArrayList<Document> cards = new ArrayList<Document>();
		data.getTrelloCards().forEach((card ->
			cards.add(convertTrelloCardMembers(card))));
		doc.append("trelloCards", cards);

		return doc;
	}

}
