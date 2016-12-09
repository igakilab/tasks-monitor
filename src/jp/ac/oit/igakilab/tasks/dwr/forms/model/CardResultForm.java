package jp.ac.oit.igakilab.tasks.dwr.forms.model;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.CardResult;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultCard;

public class CardResultForm {
	public static CardResultForm getInstance(CardResult data){
		CardResultForm form = new CardResultForm();
		form.setCardId(data.getCardId());
		form.memberIds.addAll(data.getMemberIds());
		form.setFinished(data.isFinished());

		return form;
	}

	public static CardResultForm getInstance(SprintResultCard card) {
		CardResultForm form = new CardResultForm();
		form.setCardId(card.getCardId());
		form.memberIds.addAll(card.getMemberIds());
		form.setFinished(card.isFinished());

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
