package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.CardMembers;

public class CardMembersForm {
	public static CardMembersForm getInstance(CardMembers data){
		CardMembersForm form = new CardMembersForm();
		form.setCardId(data.getCardId());
		data.getMemberIds().forEach((mid) -> form.getMemberIds().add(mid));
		return form;
	}

	public static CardMembers convert(CardMembersForm form){
		CardMembers card = new CardMembers();
		card.setCardId(form.getCardId());
		card.getMemberIds().addAll(form.getMemberIds());
		return card;
	}

	private String cardId;
	private List<String> memberIds;

	public CardMembersForm(){
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

	public void setMemberIds(List<String> memberIds) {
		this.memberIds = memberIds;
	}
}
