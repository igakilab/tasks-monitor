package jp.ac.oit.igakilab.tasks.dwr.forms;

import jp.ac.oit.igakilab.tasks.sprints.CardResult;

public class TrelloCardMembersForm {
	public static TrelloCardMembersForm getInstance(CardResult result){
		TrelloCardMembersForm form = new TrelloCardMembersForm();
		form.setTrelloCardId(result.getCardId());
		form.setMemberIds(result.getMemberIds().toArray(new String[0]));

		return form;
	}

	private String trelloCardId;
	private String[] memberIds;

	public TrelloCardMembersForm(){
		trelloCardId = null;
		memberIds = new String[0];
	}

	public String getTrelloCardId() {
		return trelloCardId;
	}

	public void setTrelloCardId(String trelloCardId) {
		this.trelloCardId = trelloCardId;
	}

	public String[] getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(String[] memberIds) {
		this.memberIds = memberIds;
	}
}
