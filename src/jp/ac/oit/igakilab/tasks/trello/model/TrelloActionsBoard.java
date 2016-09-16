package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionData;

public class TrelloActionsBoard extends TrelloBoard{
	protected List<TrelloAction> actions;
	protected List<TrelloAction> ignoredActions;

	public TrelloActionsBoard(){
		actions = new ArrayList<TrelloAction>();
	}

	private boolean sortActions(){
		boolean isSorted = true;
		for(int i=0; i<actions.size()-1; i++){
			TrelloAction a1 = actions.get(i);
			TrelloAction a2 = actions.get(i+1);
			if( a1.getDate() == null || a2.getDate() == null ) return false;
			if( a1.getDate().compareTo(a2.getDate()) < 0 ){
				isSorted = false;
				break;
			}
		}

		if( !isSorted ){
			actions.sort((TrelloAction a1, TrelloAction a2) ->
				a1.getDate().compareTo(a2.getDate())
			);
		}

		return true;
	}

	private boolean applyBoardActionData(int actionType, TrelloActionData data){
		Map<String,String> dataBoard = data.getChildMap("board");

		if( actionType == TrelloAction.ACTION_CREATE ||
			actionType == TrelloAction.ACTION_UPDATE ){
			if( actionType == TrelloAction.ACTION_CREATE ){
				setId(dataBoard.get("id"));
			}
			if( getId().equals(dataBoard.get("id")) ){
				for(Entry<String,String> entry : dataBoard.entrySet()){
					String key = entry.getKey();
					String value = entry.getValue();
					if( key.equals("name") ){
						setName(value);
					}else if( key.equals("desc") ){
						setDesc(value);
					}else if( key.equals("isClosed") ){
						setClosed(value.equals("true"));
					}
				}
			}

		}else
		if( actionType == TrelloAction.ACTION_ADDMEMBER ){
			if( data.containsKey("idMemberAdded") ){
				String mid = data.get("idMemberAdded");
				if( !memberIds.contains(mid) && getId().equals(dataBoard.get("id")) ){
					memberIds.add(mid);
				}
			}

		}else{
			return false;
		}

		return true;
	}

	private boolean applyListAction(TrelloAction action){
		boolean result = false;

		if( action.getTargetType() == TrelloAction.TARGET_LIST ){
			int actionType = action.getActionType();
			String lid = action.getData().get("list.id");

			if( actionType == TrelloAction.ACTION_CREATE ){
				lists.add(new TrelloActionList(lid));
			}

			if( actionType == TrelloAction.ACTION_CREATE ||
				actionType == TrelloAction.ACTION_UPDATE ){
				TrelloActionList list = getActionListById(lid);
				if( list == null ) return false;
				result = list.applyAction(action);
			}else{
				return false;
			}

		}else{
			return false;
		}

		return result;
	}

	private boolean applyCardAction(TrelloAction action){
		//check target
		if( action.getTargetType() != TrelloAction.TARGET_CARD ) return false;

		//check card id data
		String cid = action.getData().get("card.id");
		if( cid == null ) return false;

		//create card
		if( action.getActionType() == TrelloAction.ACTION_CREATE ){
			cards.add(new TrelloActionCard(cid));
		}

		//delete card
		if( action.getActionType() == TrelloAction.ACTION_DELETE ){
			removeCard(cid);
			return true;
		}

		//check vaild card id
		TrelloActionCard card = getActionCardById(cid);
		if( card == null ) return false;

		//check valid action type
		if( !( action.getActionType() == TrelloAction.ACTION_CREATE ||
			action.getActionType() == TrelloAction.ACTION_UPDATE ||
			action.getActionType() == TrelloAction.ACTION_ADDMEMBER ||
			action.getActionType() == TrelloAction.ACTION_REMOVEMEMBER ) ){
			return false;
		}

		return card.applyAction(action);
	}

	public void build(){
		//sort actions
		if( !sortActions() ) return;

		//initialization
		clear();
		ignoredActions.clear();

		//iteration
		for(int i=0; i<actions.size(); i++){
			TrelloAction action = actions.get(i);
			boolean result = false;

			switch( action.getTargetType() ){
			case TrelloAction.TARGET_BOARD:
				result = applyBoardActionData(
					action.getActionType(), action.getData());
				break;

			case TrelloAction.TARGET_LIST:
				result = applyListAction(action);
				break;

			case TrelloAction.TARGET_CARD:
				result = applyCardAction(action);
				break;
			}

			if( !result ){
				ignoredActions.add(action);
			}
		}
	}

	public TrelloActionList getActionListById(String lid){
		TrelloList list = getListById(lid);
		if( list != null && (list instanceof TrelloActionList) ){
			return (TrelloActionList)list;
		}
		return null;
	}

	public TrelloActionCard getActionCardById(String cid){
		TrelloCard card = getCardById(cid);
		if( card != null && (card instanceof TrelloActionCard) ){
			return (TrelloActionCard)card;
		}
		return null;
	}

	public void clearActions(){
		actions.clear();
	}

	public void addAction(TrelloAction action){
		actions.add(action);
	}

	public void addActions(List<TrelloAction> actions){
		this.actions.addAll(actions);
	}

	public List<TrelloAction> getActions(){
		return actions;
	}

	public List<TrelloAction> getIgnoredActions(){
		return ignoredActions;
	}
}
