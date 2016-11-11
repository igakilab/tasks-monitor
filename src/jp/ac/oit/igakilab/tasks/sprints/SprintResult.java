package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SprintResult {
	private String sprintId;
	private Date createdAt;
	private List<SprintResultTrelloCard> sprintCards;

	public SprintResult(String sprintId){
		this.sprintId = sprintId;
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

	public void addSprintCard(SprintResultTrelloCard scard){
		sprintCards.add(scard);
	}

	public void addSprintCard(String cardId, List<String> memberIds, boolean finished){
		SprintResultTrelloCard scard = new SprintResultTrelloCard(cardId);
		memberIds.forEach((mid -> scard.addMemberId(mid)));
		scard.setFinished(finished);
		sprintCards.add(scard);
	}

	@Deprecated
	public void addRemainedCard(TrelloCardMembers card){
		addSprintCard(card.getCardId(), card.getMemberIds(), false);
	}

	@Deprecated
	public void addRemainedCards(Collection<TrelloCardMembers> col){
		col.forEach(c -> addRemainedCard(c));
	}

	@Deprecated
	public List<TrelloCardMembers> getRemainedCards(){
		List<TrelloCardMembers> cards = new ArrayList<TrelloCardMembers>();
		sprintCards.forEach((sc) -> {
			if( !sc.isFinished() ){
				TrelloCardMembers tcm = new TrelloCardMembers(sc.getTrelloCardId());
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

	@Deprecated
	public List<TrelloCardMembers> getFinishedCards(){
		List<TrelloCardMembers> cards = new ArrayList<TrelloCardMembers>();
		sprintCards.forEach((sc) -> {
			if( sc.isFinished() ){
				TrelloCardMembers tcm = new TrelloCardMembers(sc.getTrelloCardId());
				sc.getMemberIds().forEach((mid -> tcm.addMemberId(mid)));
				cards.add(tcm);
			}
		});
		return cards;
	}

	public List<SprintResultTrelloCard> getAllCards(){
		return sprintCards;
	}
}
