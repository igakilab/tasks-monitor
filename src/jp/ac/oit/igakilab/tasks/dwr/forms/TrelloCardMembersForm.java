package jp.ac.oit.igakilab.tasks.dwr.forms;

import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;

public class TrelloCardMembersForm {
	public static TrelloCardMembers
	convertToTrelloCardMembers(TrelloCardMembersForm form){
		TrelloCardMembers tcm = new TrelloCardMembers(form.getTrelloCardId());
		if( form.getMemberIds() != null ){
			for(String mid : form.getMemberIds()){
				tcm.addMemberId(mid);
			}
		}

		return tcm;
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
