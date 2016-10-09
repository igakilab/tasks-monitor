package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.SprintResult;

public class SprintResultForm {
	public static SprintResultForm getInstance(SprintResult data){
		SprintResultForm form = new SprintResultForm();
		form.setSprintId(data.getSprintId());
		form.setCreatedAt(data.getCreatedAt());
		data.getRemainedCards().forEach((card ->
			form.getRemainedCards().add(TrelloCardMembersForm.getInstance(card))));
		data.getFinishedCards().forEach((card ->
			form.getFinishedCards().add(TrelloCardMembersForm.getInstance(card))));

		return form;
	}

	private String sprintId;
	private Date createdAt;
	private List<TrelloCardMembersForm> remainedCards;
	private List<TrelloCardMembersForm> finishedCards;

	public SprintResultForm(){
		this.sprintId = null;
		this.createdAt = null;
		this.remainedCards = new ArrayList<TrelloCardMembersForm>();
		this.finishedCards = new ArrayList<TrelloCardMembersForm>();
	}

	public String getSprintId() {
		return sprintId;
	}

	public void setSprintId(String sprintId) {
		this.sprintId = sprintId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<TrelloCardMembersForm> getRemainedCards() {
		return remainedCards;
	}

	public void setRemainedCards(List<TrelloCardMembersForm> remainedCards) {
		this.remainedCards = remainedCards;
	}

	public List<TrelloCardMembersForm> getFinishedCards() {
		return finishedCards;
	}

	public void setFinishedCards(List<TrelloCardMembersForm> finishedCards) {
		this.finishedCards = finishedCards;
	}
}
