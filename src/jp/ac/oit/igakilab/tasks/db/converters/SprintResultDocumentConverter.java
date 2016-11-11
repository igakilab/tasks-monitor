package jp.ac.oit.igakilab.tasks.db.converters;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.sprints.CardResult;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class SprintResultDocumentConverter
implements DocumentParser<SprintResult>, DocumentConverter<SprintResult>{
	public CardResult parseCardResult(Document doc){
		DocumentValuePicker picker = new DocumentValuePicker(doc);
		return parseCardResult(picker, picker.getBoolean("finished", false));
	}

	public CardResult parseCardResult(Document doc, boolean finished){
		return parseCardResult(new DocumentValuePicker(doc), finished);
	}

	public CardResult parseCardResult(DocumentValuePicker picker, boolean finished){
		CardResult cres = new CardResult();
		cres.setCardId(picker.getString("cardId", null));
		picker.getStringArray("memberIds").forEach(
			(mid -> cres.addMemberId(mid)));
		cres.setFinished(finished);
		return cres;
	}

	public Document convertCardResult(CardResult cres){
		Document doc = new Document("cardId", cres.getCardId());
		List<String> memberIds = new ArrayList<String>();
		cres.getMemberIds().forEach((memberId ->
			memberIds.add(memberId)));
		doc.append("memberIds", memberIds);
		return doc;
	}

	@Override
	public SprintResult parse(Document doc){
		DocumentValuePicker picker = new DocumentValuePicker(doc);
		SprintResult data = new SprintResult(picker.getString("sprintId", null));
		data.setCreatedAt(picker.getDate("createdAt", null));

		if( picker.getArray("sprintCards") != null ){
			picker.getArray("sprintCards").forEach((card) ->
				data.addSprintCard(parseCardResult((Document)card)));
		}

		//昔のデータはこのフォーマットで格納されている
		if( picker.getArray("remainedCards") != null ){
			picker.getArray("remainedCards").forEach((card) ->
				data.addSprintCard(parseCardResult((Document)card, false)));
		}
		if( picker.getArray("finishedCards") != null ){
			picker.getArray("finishedCards").forEach((card) ->
				data.addSprintCard(parseCardResult((Document)card, true)));
		}

		return data;
	}

	@Override
	public Document convert(SprintResult data){
		List<Document> sprintCards = new ArrayList<Document>();
		data.getAllCards().forEach((sc) ->
			sprintCards.add(convertCardResult(sc)));

		return new Document("sprintId", data.getSprintId())
			.append("createdAt", data.getCreatedAt())
			.append("sprintCards", sprintCards);
	}
}
