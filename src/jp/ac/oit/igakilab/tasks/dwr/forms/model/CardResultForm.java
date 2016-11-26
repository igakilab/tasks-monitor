package jp.ac.oit.igakilab.tasks.dwr.forms.model;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.CardResult;

public class CardResultForm {
	public static CardResultForm getInstance(CardResult data){
		CardResultForm form = new CardResultForm();
		form.setCardId(data.getCardId());
		data.getMemberIds().forEach(mid -> form.memberIds.add(mid));
		form.setFinished(data.isFinished());

		return form;
	}

	private String cardId;
	private List<String> memberIds;
	private boolean finished;

	public CardResultForm(){
		cardId = null;
		memberIds = new ArrayList<String>();
		finished = false;
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

	public void setMemberIds(List<String> memberIds) {
		this.memberIds = memberIds;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
