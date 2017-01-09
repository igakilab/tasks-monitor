package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloBoardDataForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.sprints.CardTagsAggregator.TagCount;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintMemberHistoryForms {
	public static class MemberTasksResult{
		private List<MemberTasksWrapper> sprints;
		private List<TagCountForm> tags;

		public MemberTasksResult(){
			sprints = null;
			tags = null;
		}

		public List<MemberTasksWrapper> getSprints() {
			return sprints;
		}

		public void setSprints(List<MemberTasksWrapper> sprints) {
			this.sprints = sprints;
		}

		public List<TagCountForm> getTags() {
			return tags;
		}

		public void setTags(List<TagCountForm> tags) {
			this.tags = tags;
		}

		public void setTagCounts(List<TagCount> source){
			tags = new ArrayList<>();
			source.forEach((e -> tags.add(TagCountForm.getInstance(e))));
		}

		public void addTasksWrapper(MemberTasksWrapper wrpr){
			if( sprints == null ) sprints = new ArrayList<>();
			sprints.add(wrpr);
		}
	}

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


	public static class TagCountForm{
		public static TagCountForm getInstance(TagCount tc){
			return getInstance(tc.getTagString(), tc.getCount());
		}

		public static TagCountForm getInstance(String tag, int cnt){
			TagCountForm form = new TagCountForm();
			form.setTagName(tag);
			form.setCount(cnt);
			return form;
		}

		private String tagName;
		private int count;

		public String getTagName() {
			return tagName;
		}
		public void setTagName(String tagName) {
			this.tagName = tagName;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
	}
}
