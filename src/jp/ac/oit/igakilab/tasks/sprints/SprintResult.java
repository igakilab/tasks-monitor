package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SprintResult {
	private String sprintId;
	private Date createdAt;
	private List<SprintResultCard> sprintCards;

	public SprintResult(String sprintId){
		this.sprintId = sprintId;
		createdAt = null;
		sprintCards = new ArrayList<SprintResultCard>();
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

	public List<SprintResultCard> getRemainedCards(){
		List<SprintResultCard> cards = sprintCards.stream()
			.filter((sc -> !sc.isFinished()))
			.collect(Collectors.toList());
		return cards;
	}

	public List<SprintResultCard> getFinishedCards(){
		List<SprintResultCard> cards = sprintCards.stream()
				.filter((sc -> sc.isFinished()))
				.collect(Collectors.toList());
		return cards;
	}


	public List<SprintResultCard> getCardsByMemberIdContains(String mid){
		return sprintCards.stream()
			.filter((c -> c.containsMemberId(mid)))
			.collect(Collectors.toList());
	}

	public List<SprintResultCard> getAllCards(){
		return sprintCards;
	}

	public List<CardResult> getAllCardResults(){
		List<CardResult> results = new ArrayList<CardResult>();
		sprintCards.forEach((sc -> results.add(CardResult.getInstance(sc))));

		return results;
	}

	public void addSprintCard(SprintResultCard card){
		sprintCards.add(card);
	}
}
