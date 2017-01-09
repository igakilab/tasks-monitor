package jp.ac.oit.igakilab.tasks.db.converters;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.sprints.SprintResultCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionRawData;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class SprintResultCardDocumentConverter
implements DocumentParser<SprintResultCard>, DocumentConverter<SprintResultCard>{

	@Override
	public SprintResultCard parse(Document doc) {
		SprintResultCard card = new SprintResultCard();

		DocumentValuePicker picker = new DocumentValuePicker(doc);

		card.setSprintId(picker.getString("sprintId", null));
		card.setCardId(picker.getString("cardId", null));
		card.setFinished(picker.getBoolean("finished", false));

		picker.getStringArray("memberIds").forEach((mid) ->
			card.addMemberId(mid));
		picker.getStringArray("tags").forEach((tag) ->
			card.addTag(tag));
		//System.out.println(card.getSprintId() + " : " + card.getTags());
		picker.getArray("trelloActions").forEach((obj) -> {
			if( obj instanceof Document ){
				card.addTrelloAction(new TrelloActionRawData.DocumentModel((Document)obj));
			}
		});

		return card;
	}

	@Override
	public Document convert(SprintResultCard data) {
		if( data != null ){
			Document doc = new Document();

			doc.append("sprintId", data.getSprintId());
			doc.append("cardId", data.getCardId());
			doc.append("finished", data.isFinished());
			doc.append("memberIds", data.getMemberIds());
			if( data.getTags().size() > 0 ){
				doc.append("tags", data.getTags());
			}

			List<Document> actions = new ArrayList<Document>();
			data.getTrelloActions().forEach((action) -> {
				if( action.getRawInstance() instanceof Document ){
					actions.add((Document)action.getRawInstance());
				}else{
					Document adoc = Document.parse(action.toJsonString());
					actions.add(adoc);
				}
			});
			doc.append("trelloActions", actions);

			return doc;
		}else{
			return null;
		}
	}
}
