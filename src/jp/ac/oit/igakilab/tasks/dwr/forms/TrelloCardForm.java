package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class TrelloCardForm {
	protected static void setValues(TrelloCardForm form, TrelloCard card){
		form.setId(card.getId());
		form.setName(card.getName());
		form.setDesc(card.getDesc());
		form.setListId(card.getListId());
		form.setDue(card.getDue());
		form.setMemberIds(card.getMemberIds().toArray(new String[0]));
		form.setClosed(card.isClosed());
	}

	public static TrelloCardForm getInstance(TrelloCard card){
		TrelloCardForm form = new TrelloCardForm();
		setValues(form, card);
		return form;
	}

	private String id;
	private String name;
	private String desc;
	private String listId;
	private Date due;
	private String[] memberIds;
	private boolean isClosed;

	public TrelloCardForm(){
		id = null;
		name = null;
		desc = null;
		due = null;
		memberIds = new String[0];
		isClosed = false;
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

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public Date getDue() {
		return due;
	}

	public void setDue(Date due) {
		this.due = due;
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
}
