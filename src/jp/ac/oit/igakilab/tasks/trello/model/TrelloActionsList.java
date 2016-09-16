package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloActionsList extends TrelloList {
	public List<TrelloAction> actions;

	public TrelloActionsList(){
		super();
		actions = new ArrayList<TrelloAction>();
	}

	public TrelloActionsList(String lid){
		super(lid);
		actions = new ArrayList<TrelloAction>();
	}


	public boolean applyAction(TrelloAction action){
		//check action
		if( action.getTargetType() != TrelloAction.TARGET_LIST ) return false;
		if( (action.getDate() == null) ||
			(actions.size() > 0 &&
			actions.get(actions.size()-1).getDate().compareTo(action.getDate()) > 0)
		) return false;

		Map<String,String> dataList = action.getData().getChildMap("list");
		int actionType = action.getActionType();

		if( actionType == TrelloAction.ACTION_CREATE ){
			setId(dataList.get("id"));
		}

		if(
			(actionType == TrelloAction.ACTION_CREATE ||
			actionType == TrelloAction.ACTION_UPDATE) &&
			getId().equals(dataList.get("id"))
		){
			for(Entry<String,String> entry : dataList.entrySet()){
				if( entry.getKey().equals("name") ){
					setName(entry.getValue());
				}else if( entry.getKey().equals("isClosed") ){
					setClosed(entry.getValue().equals("true"));
				}
			}
		}else{
			return false;
		}

		actions.add(action);
		return true;
	}
}
