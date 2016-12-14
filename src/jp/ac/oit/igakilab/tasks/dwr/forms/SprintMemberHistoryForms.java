package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloBoardDataForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintMemberHistoryForms {
	public static class MemberTasksWrapper{
		private SprintForm sprint;
		private TrelloBoardDataForm board;
		private List<AssignedCard> cards;

		public MemberTasksWrapper(){
			sprint = null;
			board = null;
			cards = null;
		}

		public SprintForm getSprint() {
			return sprint;
		}
		public void setSprint(SprintForm sprint) {
			this.sprint = sprint;
		}
		public TrelloBoardDataForm getBoard() {
			return board;
		}
		public void setBoard(TrelloBoardDataForm board) {
			this.board = board;
		}
		public List<AssignedCard> getCards() {
			return cards;
		}
		public void setCards(List<AssignedCard> cards) {
			this.cards = cards;
		}
		public void addCard(AssignedCard card){
			if( cards == null ) cards = new ArrayList<AssignedCard>();
			cards.add(card);
		}
	}



	public static class AssignedCard extends TrelloCardForm{
		public static AssignedCard getInstance
		(TrelloCard card, String sprintId, List<String> assignedMemberIds, boolean finished){
			AssignedCard form = new AssignedCard();
			setValues(form, card);
			form.setSprintId(sprintId);
			assignedMemberIds.forEach((mid -> form.assignedMemberIds.add(mid)));
			form.setFinished(finished);
			return form;
		}

		private String sprintId;
		private List<String> assignedMemberIds;
		private boolean finished;

		public AssignedCard(){
			super();
			sprintId = null;
			assignedMemberIds = new ArrayList<String>();
			finished = false;
		}

		public String getSprintId() {
			return sprintId;
		}
		public void setSprintId(String sprintId) {
			this.sprintId = sprintId;
		}
		public List<String> getAssignedMemberIds() {
			return assignedMemberIds;
		}
		public void setAssignedMemberIds(List<String> assignedMemberIds) {
			this.assignedMemberIds = assignedMemberIds;
		}
		public boolean isFinished() {
			return finished;
		}
		public void setFinished(boolean finished) {
			this.finished = finished;
		}
	}
}
