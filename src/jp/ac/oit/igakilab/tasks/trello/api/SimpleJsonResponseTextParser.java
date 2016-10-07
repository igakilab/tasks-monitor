package jp.ac.oit.igakilab.tasks.trello.api;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi.ResponseTextParser;

public class SimpleJsonResponseTextParser
implements ResponseTextParser<Object>{
	@Override
	public Object parse(String responseText){
		Object parsed = null;
		JSONParser parser = new JSONParser();
		try{
			parsed = parser.parse(responseText);
		}catch(ParseException e0){
			return null;
		}
		return parsed;
	}
}
