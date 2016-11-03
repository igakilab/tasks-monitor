package jp.ac.oit.igakilab.tasks.trello.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.ac.oit.igakilab.tasks.trello.TrelloDateFormat;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class TrelloActionsCard extends TrelloCard {
	public List<TrelloAction> actions;

	public TrelloActionsCard(){
		super();
		actions = new ArrayList<TrelloAction>();
	}

	public TrelloActionsCard(String cid){
		super(cid);
		actions = new ArrayList<TrelloAction>();
	}


	public boolean applyAction(TrelloAction action){
		//check action
		if( action.getTargetType() != TrelloAction.TARGET_CARD ) return false;
		if( (action.getDate() == null) ||
			(actions.size() > 0 &&
			actions.get(actions.size()-1).getDate().compareTo(action.getDate()) > 0)
		) return false;

		Map<String,String> dataCard = action.getData().getChildMap("card");
		int actionType = action.getActionType();

		if( actionType == TrelloAction.ACTION_CREATE ){
			setId(dataCard.get("id"));
			if( !dataCard.containsKey("idList") ){
				if( action.getData().containsKey("list.id") ){
					dataCard.put("idList", action.getData().get("list.id"));
				}else{
					return false;
				}
			}else{
				return false;
			}
		}

		if( getId() == null || !getId().equals(dataCard.get("id")) ) return false;

		if( actionType == TrelloAction.ACTION_CREATE ||
			actionType == TrelloAction.ACTION_UPDATE ){
			for(Entry<String,String> entry : dataCard.entrySet()){
				if( entry.getKey().equals("idList") ){
					setListId(entry.getValue());

				}else if( entry.getKey().equals("name") ){
					setName(entry.getValue());

				}else if( entry.getKey().equals("desc") ){
					setDesc(entry.getValue());

				}else if( entry.getKey().equals("due") ){
					DateFormat df = new TrelloDateFormat();
					Date tmp = null;
					try{ tmp = df.parse(entry.getValue());
					}catch(ParseException e0){};
					if( tmp != null ) setDue(tmp);

				}else if(entry.getKey().equals("closed") ){
					setClosed(entry.getValue().equals("true"));

				}
			}
		}else
		if( actionType == TrelloAction.ACTION_ADDMEMBER ){
			String mid = action.getData().get("idMember");
			if( mid != null && !containsMemberId(mid) ){
				addMemberId(mid);
			}

		}else
		if( actionType == TrelloAction.ACTION_REMOVEMEMBER ){
			String mid = action.getData().get("idMember");
			if( mid != null && containsMemberId(mid) ){
				removeMemberId(mid);
			}

		}else{
			return false;
		}

		actions.add(action);
		return true;
	}

	public List<TrelloAction> getActions(){
		return actions;
	}


}
