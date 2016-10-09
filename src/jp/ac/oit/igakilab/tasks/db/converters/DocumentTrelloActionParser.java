package jp.ac.oit.igakilab.tasks.db.converters;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.trello.TrelloDateFormat;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionData;
import jp.ac.oit.igakilab.tasks.util.DocumentToMapConverter;

public class DocumentTrelloActionParser
implements DocumentParser<TrelloAction>{
	private DateFormat df;

	public DocumentTrelloActionParser(){
		df = new TrelloDateFormat();
	}

	public TrelloAction parse(Document doc){
		TrelloAction action = new TrelloAction();

		//parse id
		action.setId(doc.getString("id"));

		//parse type
		if( !doc.containsKey("type") ) return null;
		action.setType(doc.getString("type"));
		action.setTargetType(TrelloAction.parseTargetType(action.getType()));
		action.setActionType(TrelloAction.parseActionType(action.getType()));

		//parse date
		if( !doc.containsKey("date") ) return null;
		try{ action.setDate(df.parse(doc.getString("date"))); }
		catch(ParseException e0){ return null; }

		//parse data
		Map<String,String> data =
			DocumentToMapConverter.convertToStringMap((Document)doc.get("data"));
		TrelloActionData tadata = new TrelloActionData(data);
		action.setData(tadata);

		//parse memberCreator
		action.setMemberCreatorId(((Document)doc.get("memberCreator")).getString("id"));

		//set raw text
		action.setRawText(doc.toJson());

		return action;
	}
}
