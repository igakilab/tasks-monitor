package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SprintResult {
	private String sprintId;
	private Date createdAt;
	private List<CardResult> sprintCards;

	public SprintResult(String sprintId){
		this.sprintId = sprintId;
		createdAt = null;
		sprintCards = new ArrayList<CardResult>();
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

	public void addSprintCard(CardResult scard){
		sprintCards.add(scard);
	}

	public void addSprintCard(String cardId, List<String> memberIds, boolean finished){
		CardResult cr = new CardResult(cardId);
		memberIds.forEach((mid -> cr.addMemberId(mid)));
		cr.setFinished(finished);
		sprintCards.add(cr);
	}

	@Deprecated
	public void addRemainedCard(TrelloCardMembers card){
		addSprintCard(card.getCardId(), card.getMemberIds(), false);
	}

	@Deprecated
	public void addRemainedCards(Collection<TrelloCardMembers> col){
		col.forEach(c -> addRemainedCard(c));
	}

	public List<TrelloCardMembers> getRemainedCards(){
		List<TrelloCardMembers> cards = new ArrayList<TrelloCardMembers>();
		sprintCards.forEach((sc) -> {
			if( !sc.isFinished() ){
				TrelloCardMembers tcm = new TrelloCardMembers(sc.getCardId());
				sc.getMemberIds().forEach((mid -> tcm.addMemberId(mid)));
				cards.add(tcm);
			}
		});
		return cards;
	}

	@Deprecated
	public void addFinishedCard(TrelloCardMembers card){
		addSprintCard(card.getCardId(), card.getMemberIds(), false);
	}

	@Deprecated
	public void addFinishedCards(Collection<TrelloCardMembers> col){
		col.forEach(c -> addFinishedCard(c));
	}

	public List<TrelloCardMembers> getFinishedCards(){
		List<TrelloCardMembers> cards = new ArrayList<TrelloCardMembers>();
		sprintCards.forEach((sc) -> {
			if( sc.isFinished() ){
				TrelloCardMembers tcm = new TrelloCardMembers(sc.getCardId());
				sc.getMemberIds().forEach((mid -> tcm.addMemberId(mid)));
				cards.add(tcm);
			}
		});
		return cards;
	}

	public List<CardResult> getAllCards(){
		return sprintCards;
	}
}
