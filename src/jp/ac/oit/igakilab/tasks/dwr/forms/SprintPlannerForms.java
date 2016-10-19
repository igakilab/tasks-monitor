package jp.ac.oit.igakilab.tasks.dwr.forms;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintPlannerForms {
	public static class TrelloCardMemberIds extends TrelloCardForm{
		public static TrelloCardMemberIds getInstance(TrelloCard card, MemberTrelloIdTable mtable){
			TrelloCardForm cform = TrelloCardForm.getInstance(card);
			TrelloCardMembersForm mform = TrelloCardMembersForm.getInstance(
				TrelloCardMembers.getInstance(card, mtable));
			return getInstance(cform, mform);
		}

		public static TrelloCardMemberIds getInstance(TrelloCardForm card, TrelloCardMembersForm mems){
			TrelloCardMemberIds form = new TrelloCardMemberIds();
			form.setId(card.getId());
			form.setName(card.getName());
			form.setDesc(card.getDesc());
			form.setListId(card.getListId());
			form.setDue(card.getDue());
			form.setTrelloMemberIds(card.getMemberIds());
			form.setClosed(card.isClosed());
			form.setMemberIds(mems.getMemberIds());
			return form;
		}

		private String[] tasksMemberIds;

		public String[] getTrelloMemberIds(){
			return super.getMemberIds();
		}

		public void setTrelloMemberIds(String[] memberIds){
			super.setMemberIds(memberIds);
		}

		public String[] getMemberIds() {
			return tasksMemberIds;
		}

		public void setMemberIds(String[] tasksMemberIds) {
			this.tasksMemberIds = tasksMemberIds;
		}
	}
}
