package jp.ac.oit.igakilab.tasks.trello.model;

public class TrelloList {
	protected String id;
	protected String name;
	protected boolean isClosed;

	public TrelloList(){
		id = null;
		name = null;
		isClosed = false;
	}

	public TrelloList(String id){
		this();
		setId(id);
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

	public String toString(){
		return String.format("LIST %s %s - %s, ",
			name, (id != null ? id.substring(id.length()-4) : "null"),
			(isClosed ? "C" : "-"));
	}
}
