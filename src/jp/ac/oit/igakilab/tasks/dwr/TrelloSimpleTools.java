package jp.ac.oit.igakilab.tasks.dwr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardDataFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoardData;

public class TrelloSimpleTools {
	public String getBoardIdByShortLink(String shortLink){
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		TrelloBoardDataFetcher tbf = new TrelloBoardDataFetcher(api);
		TrelloBoardData data = tbf.getTrelloBoardData(shortLink);
		if( data != null ){
			return data.getId();
		}else{
			return null;
		}
	}

	public String getBoardIdByShortUrl(String shortUrl){
		String regex = "https?://trello.com/b/([0-9a-zA-Z]*)";
		Matcher m = Pattern.compile(regex).matcher(shortUrl);
		if( m.find() && m.groupCount() >= 1 ){
			return getBoardIdByShortLink(m.group(1));
		}else{
			return null;
		}
	}

	public String getMemberIdByUsername(String username){
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		Object res = api.rget("/1/members/" + username).getData();
		if( res != null ){
			try{
				JSONObject json = (JSONObject)res;
				String id = (String)json.get("id");
				return id;
			}catch(ClassCastException e0){
				return null;
			}
		}else{
			return null;
		}
	}
}
