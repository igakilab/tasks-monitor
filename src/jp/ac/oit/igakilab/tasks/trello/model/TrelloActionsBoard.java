package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloActionsBoard extends TrelloBoard{
	protected List<TrelloAction> actions;

	public TrelloActionsBoard(){
		actions = new ArrayList<TrelloAction>();
	}


}
