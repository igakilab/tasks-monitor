package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

	@Deprecated
	public void addFinishedCard(TrelloCardMembers card){
		addSprintCard(card.getCardId(), card.getMemberIds(), false);
	}

	@Deprecated
	public void addFinishedCards(Collection<TrelloCardMembers> col){
		col.forEach(c -> addFinishedCard(c));
	}

	public List<CardResult> getRemainedCards(){
		return sprintCards.stream()
			.filter(c -> !c.isFinished())
			.collect(Collectors.toList());
	}

	public List<CardResult> getFinishedCards(){
		return sprintCards.stream()
			.filter(c -> c.isFinished())
			.collect(Collectors.toList());
	}

	public List<CardResult> getCardsByMemberIdContains(String mid){
		return sprintCards.stream()
			.filter((c -> c.containsMemberId(mid)))
			.collect(Collectors.toList());
	}

	public List<CardResult> getAllCards(){
		return sprintCards;
	}
}
