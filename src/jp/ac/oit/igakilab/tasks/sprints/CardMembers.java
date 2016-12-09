package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

public class CardMembers {
	public static List<String> getCardIdList(List<CardMembers> cms){
		List<String> ids = new ArrayList<String>(cms.size());
		cms.forEach((cm -> ids.add(cm.getCardId())));
		return ids;
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
