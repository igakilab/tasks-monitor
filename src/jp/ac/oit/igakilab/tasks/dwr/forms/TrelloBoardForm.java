package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;

public class TrelloBoardForm {
	public static TrelloBoardForm getInstance(TrelloBoard board){
		TrelloBoardForm form = new TrelloBoardForm();
		form.setId(board.getId());
		form.setName(board.getName());
		form.setDesc(board.getDesc());
		form.setMemberIds(board.getMemberIds().toArray(new String[0]));

		List<TrelloCardForm> cards = new ArrayList<TrelloCardForm>();
		board.getCards().forEach((card) ->
			cards.add(TrelloCardForm.getInstance(card)));
		form.setCards(cards.toArray(new TrelloCardForm[cards.size()]));

		List<TrelloListForm> lists = new ArrayList<TrelloListForm>();
		board.getLists().forEach((list) ->
			lists.add(TrelloListForm.getInstance(list)));
		form.setLists(lists.toArray(new TrelloListForm[lists.size()]));

		return form;
	}

	private String id;
	private String name;
	private String desc;
	private String[] memberIds;
	private boolean isClosed;
	private TrelloCardForm[] cards;
	private TrelloListForm[] lists;

	public TrelloBoardForm(){
		id = null;
		name = null;
		desc = null;
		memberIds = new String[0];
		isClosed = false;
		cards = new TrelloCardForm[0];
		lists = new TrelloListForm[0];
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String[] getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(String[] memberIds) {
		this.memberIds = memberIds;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public TrelloCardForm[] getCards() {
		return cards;
	}

	public void setCards(TrelloCardForm[] cards) {
		this.cards = cards;
	}

	public TrelloListForm[] getLists() {
		return lists;
	}

	public void setLists(TrelloListForm[] lists) {
		this.lists = lists;
	}
}
