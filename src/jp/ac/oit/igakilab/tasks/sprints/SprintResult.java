package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
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


	public List<CardResult> getRemainedCards(){
		List<CardResult> cards = sprintCards.stream()
			.filter((sc -> !sc.isFinished()))
			.collect(Collectors.toList());
		return cards;
	}

	public List<CardResult> getFinishedCards(){
		List<CardResult> cards = sprintCards.stream()
				.filter((sc -> sc.isFinished()))
				.collect(Collectors.toList());
			return cards;
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
