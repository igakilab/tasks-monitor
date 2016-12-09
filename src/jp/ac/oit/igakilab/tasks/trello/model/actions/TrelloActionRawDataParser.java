package jp.ac.oit.igakilab.tasks.trello.model.actions;

import java.util.function.Function;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;

public class TrelloActionRawDataParser
implements Function<TrelloActionRawData,TrelloAction>{
	private TrelloActionDocumentParser parser = new TrelloActionDocumentParser();

	@Override
	public TrelloAction apply(TrelloActionRawData t) {
		if( t.getRawInstance() instanceof Document ){
			return parser.parse((Document)t.getRawInstance());
		}else{
			return parser.parse(
				Document.parse(t.toJsonString()));
		}
	}

}
