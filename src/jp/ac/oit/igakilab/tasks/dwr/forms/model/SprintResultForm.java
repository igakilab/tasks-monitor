package jp.ac.oit.igakilab.tasks.dwr.forms.model;

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
			form.getRemainedCards().add(CardResultForm.getInstance(card))));
		data.getFinishedCards().forEach((card ->
			form.getFinishedCards().add(CardResultForm.getInstance(card))));

		return form;
	}

	private String sprintId;
	private Date createdAt;
	private List<CardResultForm> remainedCards;
	private List<CardResultForm> finishedCards;

	public SprintResultForm(){
		this.sprintId = null;
		this.createdAt = null;
		this.remainedCards = new ArrayList<CardResultForm>();
		this.finishedCards = new ArrayList<CardResultForm>();
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

	public List<CardResultForm> getRemainedCards() {
		return remainedCards;
	}

	public void setRemainedCards(List<CardResultForm> remainedCards) {
		this.remainedCards = remainedCards;
	}

	public List<CardResultForm> getFinishedCards() {
		return finishedCards;
	}

	public void setFinishedCards(List<CardResultForm> finishedCards) {
		this.finishedCards = finishedCards;
	}
}
