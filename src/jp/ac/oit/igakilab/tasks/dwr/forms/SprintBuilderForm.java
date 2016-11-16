package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintBuilderForm {
	public SprintBuilderForm getInstance
	(Sprint currentSprint, List<TrelloCard> trelloCards, MemberTrelloIdTable ttb){
		SprintBuilderForm form = new SprintBuilderForm();

		//進行中スプリントがある場合はフォームに指定
		if( currentSprint != null ){
			form.setCurrentSprint(SprintForm.getInstance(currentSprint));
		}

		//対象カードを指定する
		trelloCards.forEach((tc) ->
			form.getCards().add(TrelloCardForm.getInstance(tc, ttb)));

		return form;
	}

	private SprintForm currentSprint;
	private List<TrelloCardForm> cards;

	public SprintBuilderForm(){
		currentSprint = null;
		cards = new ArrayList<TrelloCardForm>();
	}

	public SprintForm getCurrentSprint() {
		return currentSprint;
	}

	public void setCurrentSprint(SprintForm currentSprint) {
		this.currentSprint = currentSprint;
	}

	public List<TrelloCardForm> getCards() {
		return cards;
	}

	public void setCards(List<TrelloCardForm> cards) {
		this.cards = cards;
	}
}
