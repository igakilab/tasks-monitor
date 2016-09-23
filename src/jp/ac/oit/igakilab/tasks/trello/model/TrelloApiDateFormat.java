package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.SimpleDateFormat;

public class TrelloApiDateFormat extends SimpleDateFormat{
	static String DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	public TrelloApiDateFormat(){
		super(DEFAULT_FORMAT);
	}
}
