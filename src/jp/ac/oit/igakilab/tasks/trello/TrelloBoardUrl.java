package jp.ac.oit.igakilab.tasks.trello;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class TrelloBoardUrl {
	public static String URL_HEAD = "http://trello.com/b/";
	public static String URL_REGEX = "https?://trello.com/b/([0-9a-zA-Z]*)/?";
	public static String SHORTLINK_REGEX = "[0-9a-zA-Z]+";

	public static void main(String[] args){
		String[] strings = {"https://trello.com/b/WirAa5NS/", "WirAa5NS"};
		TasksTrelloClientBuilder.setTestApiKey();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();

		for(String str : strings){
			/*System.out.println("STR: " + str);
			System.out.format("\tURL: %b SHORTLINK: %b\n",
				isUrl(str), isShortLink(str));
			System.out.println("\tto url: " + toUrl(str));
			System.out.println("\ttoshortlink: " + toShortLink(str));*/
			TrelloBoardUrl url = new TrelloBoardUrl(str);
			System.out.println(url.toString());
			System.out.println(url.fetchTrelloBoardData(api));
		}
	}

	public static boolean isShortLink(String str){
		return str.matches(SHORTLINK_REGEX);
	}

	public static boolean isUrl(String str){
		return str.matches(URL_REGEX);
	}

	public static String toShortLink(String url){
		Matcher m = Pattern.compile(URL_REGEX).matcher(url);
		if( m.find() ){
			return m.group(1);
		}else{
			return null;
		}
	}

	public static String toUrl(String shortlink){
		if( isShortLink(shortlink) ){
			return URL_HEAD + shortlink;
		}else{
			return null;
		}
	}


	private String text;

	public TrelloBoardUrl(String text){
		this.text = null;
		set(text);
	}

	public boolean set(String text){
		if( isShortLink(text) || isUrl(text) ){
			this.text = text;
			return true;
		}else{
			return false;
		}
	}

	public boolean isValid(){
		return text != null ?
			(isShortLink(text) || isUrl(text)) : false;
	}

	public String getUrl(){
		if( text != null ){
			if( isUrl(text) ){
				return text;
			}else{
				return toUrl(text);
			}
		}else{
			return null;
		}
	}

	public String getShortLink(){
		if( text != null ){
			if( isShortLink(text) ){
				return text;
			}else{
				return toShortLink(text);
			}
		}else{
			return null;
		}
	}

	public TrelloBoardData fetchTrelloBoardData(TrelloApi<Object> api){
		TrelloBoardDataFetcher fetcher = new TrelloBoardDataFetcher(api);
		TrelloBoardData data = fetcher.getTrelloBoardData(getShortLink());
		return data;
	}

	public String toString(){
		if( !isValid() ){
			return "TRELLO BOARD URL [INVALID TEXT]";
		}else{
			return String.format("TRELLO BOARD URL [%s %s %s]",
				isShortLink(text) ? "SHORTLINK" : "URL",
				getUrl(), getShortLink());
		}
	}






}
