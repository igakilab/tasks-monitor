package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class TrelloBoardTreeForm {
	public static class TrelloListTreeForm extends TrelloListForm{
		public static TrelloListTreeForm getInstance(TrelloBoard board, String listId){
			TrelloListForm listForm = getInstance(board.getListById(listId));
			if( listForm == null ) return null;

			TrelloListTreeForm form = new TrelloListTreeForm();
			form.setId(listForm.getId());
			form.setName(listForm.getName());
			form.setClosed(listForm.isClosed());

			List<TrelloCard> cards = board.getCardsByListId(listId);
			form.setCards(new TrelloCardForm[cards.size()]);
			for(int i=0; i<cards.size(); i++){
				form.getCards()[i] = TrelloCardForm.getInstance(cards.get(i));
			}

			return form;
		}


		private TrelloCardForm[] cards;

		public TrelloListTreeForm(){
			super();
			cards = new TrelloCardForm[0];
		}

		public TrelloCardForm[] getCards() {
			return cards;
		}

		public void setCards(TrelloCardForm[] cards) {
			this.cards = cards;
		}
	}

	private String id;
	private String name;
	private String desc;
	private String[] memberIds;
	private boolean isClosed;
	private TrelloListTreeForm[] lists;


	public static TrelloBoardTreeForm getInstance(TrelloBoard board){
		TrelloBoardTreeForm form = new TrelloBoardTreeForm();
		form.setId(board.getId());
		form.setName(board.getName());
		form.setDesc(board.getDesc());
		form.setMemberIds(board.getMemberIds().toArray(new String[0]));

		List<TrelloListTreeForm> lists = new ArrayList<TrelloListTreeForm>();
		for(TrelloList list : board.getLists()){
			lists.add(TrelloListTreeForm.getInstance(board, list.getId()));
		}
		form.setLists(lists.toArray(new TrelloListTreeForm[0]));

		return form;
	}

	public TrelloBoardTreeForm(){
		id = null;
		name = null;
		desc = null;
		memberIds = new String[0];
		isClosed = false;
		lists = new TrelloListTreeForm[0];
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

	public TrelloListTreeForm[] getLists() {
		return lists;
	}

	public void setLists(TrelloListTreeForm[] lists) {
		this.lists = lists;
	}
}
