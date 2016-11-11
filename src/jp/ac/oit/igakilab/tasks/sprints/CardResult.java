package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

public class CardResult {
	private String cardId;
	private List<String> memberIds;
	private boolean finished;

	public CardResult(){
		cardId = null;
		memberIds = new ArrayList<String>();
		finished = false;
	}

	public CardResult(String cardId){
		this();
		this.cardId = cardId;
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

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
