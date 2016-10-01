package jp.ac.oit.igakilab.tasks.trello;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class TrelloDateFormat extends SimpleDateFormat{
	public static String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

	public TrelloDateFormat(){
		super(PATTERN);
		super.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public void setTimeZone(TimeZone zone)
	throws RuntimeException{
		throw new RuntimeException("このインスタンスではタイムゾーンの変更を禁止しています");
	}
}
