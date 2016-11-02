package jp.ac.oit.igakilab.tasks.hubot;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class NotifyTrelloCard {
	public static NotifyTrelloCard getInstance
	(TrelloCard card, TrelloBoard board, MemberTrelloIdTable mtable){
		NotifyTrelloCard ncard = new NotifyTrelloCard();
		ncard.board = board;
		ncard.list = board.getListById(card.getListId());
		ncard.card = card;

		if( mtable != null ){
			for(String tmid : card.getMemberIds()){
				String tmp = mtable.getMemberId(tmid);
				if( tmp != null ){
					ncard.memberIds.add(tmp);
				}
			}
		}

		return ncard;
	}

	private List<String> memberIds;
	private TrelloBoard board;
	private TrelloList list;
	private TrelloCard card;

	public NotifyTrelloCard(){
		memberIds = new ArrayList<String>();
		board = null;
		list = null;
		card = null;
	}

	public void addMemberId(String mid){
		memberIds.add(mid);
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public TrelloBoard getBoard() {
		return board;
	}

	public TrelloList getList() {
		return list;
	}

	public TrelloCard getCard() {
		return card;
	}
}
