package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

public class CardMembers {
	public static CardMembers getInstance(TrelloCardMembers tcm){
		CardMembers cm = new CardMembers();
		cm.setCardId(tcm.getCardId());
		tcm.getMemberIds().forEach((mid -> cm.addMemberId(mid)));
		return cm;
	}

	private String cardId;
	private List<String> memberIds;

	public CardMembers(){
		cardId = null;
		memberIds = new ArrayList<String>();
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public void addMemberId(String mid){
		memberIds.add(mid);
	}
}
