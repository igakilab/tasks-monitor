package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;
import org.json.simple.JSONObject;

public class JsonDocumentConverter
implements DocumentConverter<Object>{

	@Override
	public Document convert(Object data) {

		if( data instanceof Document ){
			return (Document)data;

		}else if( data instanceof JSONObject ){
			JSONObject json = (JSONObject)data;
			return Document.parse(json.toJSONString());

		}else if( data instanceof String ){
			String jsonString = (String)data;
			return Document.parse(jsonString);
		}

		return null;
	}

}
