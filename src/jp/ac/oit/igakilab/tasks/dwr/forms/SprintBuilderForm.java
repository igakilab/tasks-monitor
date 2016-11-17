package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.Sprint;

public class SprintBuilderForm {
	public static SprintBuilderForm getInstance
	(Sprint currentSprint, List<TrelloCardForm> trelloCards, List<MemberForm> members){
		SprintBuilderForm form = new SprintBuilderForm();

		//進行中スプリントがある場合はフォームに指定
		if( currentSprint != null ){
			form.setCurrentSprint(SprintForm.getInstance(currentSprint));
		}

		//対象カードを指定する
		form.getCards().addAll(trelloCards);

		//ボードに所属するメンバー一覧を設定する
		form.getMembers().addAll(members);

		return form;
	}

	private SprintForm currentSprint;
	private List<TrelloCardForm> cards;
	private List<MemberForm> members;

	public SprintBuilderForm(){
		currentSprint = null;
		cards = new ArrayList<TrelloCardForm>();
		members = new ArrayList<MemberForm>();
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

	public List<MemberForm> getMembers() {
		return members;
	}

	public void setMembers(List<MemberForm> memberIds) {
		this.members = memberIds;
	}
}
