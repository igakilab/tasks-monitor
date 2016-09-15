package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

public class TrelloActionsBoard extends TrelloBoard{
	protected List<Document> actions;
	
	public TrelloActionsBoard(){
		actions = new ArrayList<Document>();
	}
	
	
}
