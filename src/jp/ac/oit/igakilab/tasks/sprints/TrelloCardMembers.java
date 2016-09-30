package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

public class TrelloCardMembers {
	private String trelloCardId;
	private List<String> memberIds;

	public TrelloCardMembers(String cardId){
		this.trelloCardId = cardId;
		this.memberIds = new ArrayList<String>();
	}

	public String getCardId() {
		return trelloCardId;
	}

	public void setCardId(String cardId) {
		this.trelloCardId = cardId;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public void addMemberId(String memberId){
		if( !memberIds.contains(memberId) ){
			memberIds.add(memberId);
		}
	}
}
