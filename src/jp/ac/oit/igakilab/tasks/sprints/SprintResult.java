package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SprintResult {
	private String sprintId;
	private Date createdAt;
	private List<TrelloCardMembers> remainedCards;
	private List<TrelloCardMembers> finishedCards;

	public SprintResult(String sprintId){
		this.sprintId = sprintId;
		remainedCards = new ArrayList<TrelloCardMembers>();
		finishedCards = new ArrayList<TrelloCardMembers>();
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

	public void addRemainedCard(TrelloCardMembers card){
		remainedCards.add(card);
	}

	public void addRemainedCards(Collection<TrelloCardMembers> col){
		remainedCards.addAll(col);
	}

	public List<TrelloCardMembers> getRemainedCards(){
		return remainedCards;
	}

	public void addFinishedCard(TrelloCardMembers card){
		finishedCards.add(card);
	}

	public void addFinishedCards(Collection<TrelloCardMembers> col){
		finishedCards.addAll(col);
	}

	public List<TrelloCardMembers> getFinishedCards(){
		return finishedCards;
	}
}
