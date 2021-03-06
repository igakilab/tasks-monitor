package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;

public class SprintFinisherForms {
	public static class MemberCards{
		private String memberId;
		private List<String> remainedCards;
		private List<String> finishedCards;

		public MemberCards(String memberId){
			this.memberId = memberId;
			this.remainedCards = new ArrayList<String>();
			this.finishedCards = new ArrayList<String>();
		}

		public String getMemberId() {
			return memberId;
		}

		public void setMemberId(String memberId) {
			this.memberId = memberId;
		}

		public List<String> getRemainedCards() {
			return remainedCards;
		}

		public void addReaminedCard(String cardId){
			this.remainedCards.add(cardId);
		}

		public void setRemainedCards(List<String> remainedCards) {
			this.remainedCards = remainedCards;
		}

		public List<String> getFinishedCards() {
			return finishedCards;
		}

		public void addFinishedCards(String cardId){
			this.finishedCards.add(cardId);
		}

		public void setFinishedCards(List<String> finishedCards) {
			this.finishedCards = finishedCards;
		}
	}

	public static class ClosedSprintResult{
		private String sprintId;
		private Date createdAt;
		private List<MemberCards> memberTasks;
		private List<String> remainedCards;
		private List<String> finishedCards;
		private List<TrelloCardForm> sprintCards;

		public ClosedSprintResult(){
			sprintId = null;
			createdAt = null;
			memberTasks = null;
			remainedCards = null;
			finishedCards = null;
			sprintCards = null;
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

		public List<MemberCards> getMemberTasks() {
			return memberTasks;
		}

		public void setMemberTasks(List<MemberCards> memberTasks) {
			this.memberTasks = memberTasks;
		}

		public List<String> getRemainedCards() {
			return remainedCards;
		}

		public void setRemainedCards(List<String> remainedCards) {
			this.remainedCards = remainedCards;
		}

		public List<String> getFinishedCards() {
			return finishedCards;
		}

		public void setFinishedCards(List<String> finishedCards) {
			this.finishedCards = finishedCards;
		}

		public List<TrelloCardForm> getSprintCards() {
			return sprintCards;
		}

		public void setSprintCards(List<TrelloCardForm> sprintCards) {
			this.sprintCards = sprintCards;
		}
	}

	public static class SprintResultCardTagsForm{
		private String cardId;
		private List<String> tags;

		public String getCardId() {
			return cardId;
		}
		public void setCardId(String cardId) {
			this.cardId = cardId;
		}
		public List<String> getTags() {
			return tags;
		}
		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}
}
