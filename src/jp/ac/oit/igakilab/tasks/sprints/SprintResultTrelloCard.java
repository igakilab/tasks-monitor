package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintResultTrelloCard {
	public static SprintResultTrelloCard
	getInstance(TrelloCard card, MemberTrelloIdTable mtable, boolean finished){
		if( card != null ){
			SprintResultTrelloCard scard = new SprintResultTrelloCard(card.getId());
			card.getMemberIds().forEach((tmid) -> {
				if( mtable != null ){
					scard.addMemberId(mtable.getMemberId(tmid));
				}
			});
			scard.setFinished(finished);
			return scard;
		}else{
			return null;
		}
	}

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
