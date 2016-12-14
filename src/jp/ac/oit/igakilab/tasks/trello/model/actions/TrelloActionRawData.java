package jp.ac.oit.igakilab.tasks.trello.model.actions;

import org.bson.Document;
import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;

public interface TrelloActionRawData {
	public String toJsonString();
	public TrelloAction toTrelloAction();
	public Object getRawInstance();

	public class JSONObjectModel implements TrelloActionRawData{
		private JSONObject jsonObject;

		public JSONObjectModel(JSONObject obj){
			this.jsonObject = obj;
		}

		@Override
		public String toJsonString(){
			return jsonObject.toJSONString();
		}

		@Override
		public TrelloAction toTrelloAction(){
			return null;
		}

		@Override
		public Object getRawInstance(){
			return jsonObject;
		}
	}


	public class DocumentModel implements TrelloActionRawData{
		private Document doc;

		public DocumentModel(Document doc){
			this.doc = doc;
		}

		@Override
		public String toJsonString(){
			return doc.toJson();
		}

		@Override
		public TrelloAction toTrelloAction(){
			TrelloActionDocumentParser parser = new TrelloActionDocumentParser();
			return parser.parse(doc);
		}

		@Override
		public Object getRawInstance(){
			return doc;
		}
	}
}