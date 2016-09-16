package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.SimpleDateFormat;

public class TrelloDateFormat extends SimpleDateFormat{
	static String DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	public TrelloDateFormat(){
		super(DEFAULT_FORMAT);
	}
}
