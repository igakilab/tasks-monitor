package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionRawData;

public class SprintResultCard {
	private String sprintId;
	private String cardId;
	private boolean finished;
	private List<String> memberIds;
	private List<TrelloActionRawData> trelloActions;

	public SprintResultCard(){
		sprintId = null;
		cardId = null;
		finished = false;
		memberIds = new ArrayList<String>();
		trelloActions = new ArrayList<TrelloActionRawData>();
	}

	public String getSprintId() {
		return sprintId;
	}

	public void setSprintId(String sprintId) {
		this.sprintId = sprintId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(List<String> memberIds) {
		this.memberIds = memberIds;
	}

	public void addMemberId(String memberId){
		this.memberIds.add(memberId);
	}

	public boolean containsMemberId(String memberId){
		return memberIds.contains(memberId);
	}

	public List<TrelloActionRawData> getTrelloActions() {
		return trelloActions;
	}

	public List<TrelloAction> getTrelloActions(Function<TrelloActionRawData, TrelloAction> parser){
		List<TrelloAction> actions = new ArrayList<TrelloAction>();

		for(TrelloActionRawData raw : trelloActions){
			TrelloAction parsed = parser.apply(raw);
			if( parsed != null ){
				actions.add(parsed);
			}
		}

		actions.sort((v1, v2) -> v1.getDate().compareTo(v2.getDate()));
		System.out.println("TRELLO ACTIONS ---");
		actions.forEach((act -> System.out.println("\t" + act.toString())));

		return actions;
	}

	public TrelloActionsCard getTrelloActionsCard(Function<TrelloActionRawData,TrelloAction> parser){
		TrelloActionsCard card = new TrelloActionsCard();
		getTrelloActions(parser).forEach((act -> card.applyAction(act)));
		System.out.println("\tbuilt->" + card.toString());
		return card;
	}

	public void setTrelloActions(List<TrelloActionRawData> trelloActions) {
		this.trelloActions = trelloActions;
	}

	public void addTrelloAction(TrelloActionRawData rawData){
		this.trelloActions.add(rawData);
	}
}
