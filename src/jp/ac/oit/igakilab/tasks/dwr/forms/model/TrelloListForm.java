package jp.ac.oit.igakilab.tasks.dwr.forms.model;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class TrelloListForm {
	public static TrelloListForm getInstance(TrelloList list){
		TrelloListForm form = new TrelloListForm();
		form.setId(list.getId());
		form.setName(list.getName());
		form.setClosed(list.isClosed());
		return form;
	}

	private String id;
	private String name;
	private boolean isClosed;

	public TrelloListForm(){
		id = null;
		name = null;
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

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
