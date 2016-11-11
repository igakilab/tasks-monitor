package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

public class SprintResultTrelloCard {
	private String trelloCardId;
	private List<String> memberIds;
	private boolean finished;

	public SprintResultTrelloCard(String trelloCardId){
		this.trelloCardId = trelloCardId;
		this.memberIds = new ArrayList<String>();
		finished = false;
	}

	public String getTrelloCardId() {
		return trelloCardId;
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
