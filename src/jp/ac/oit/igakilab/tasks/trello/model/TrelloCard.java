package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TrelloCard {
	protected String id;
	protected String listId;
	protected String name;
	protected String desc;
	protected Date due;
	protected Set<String> memberIds;
	protected boolean isClosed;

	public TrelloCard(){
		id = null;
		listId = null;
		name = null;
		desc = null;
		due = null;
		memberIds = new HashSet<String>();
		isClosed = false;
	}

	public TrelloCard(String id){
		this();
		setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
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

	public Date getDue() {
		return due;
	}

	public void setDue(Date due) {
		this.due = due;
	}

	public Set<String> getMemberIds() {
		return memberIds;
	}

	public void clearMemberId(){
		memberIds.clear();
	}

	public void addMemberId(String mid){
		memberIds.add(mid);
	}

	public void removeMemberId(String mid){
		memberIds.remove(mid);
	}

	public boolean containsMemberId(String mid){
		return memberIds.contains(mid);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public String toString(){
		return String.format("CARD %s %s | %s %s %s %s",
			name, (id != null ? id.substring(id.length()-4) : "null"),
			(listId != null ? listId.substring(listId.length()-4) : "null"),
			(isClosed ? "C" : "-"),
			(due != null ? "d" : "-"),
			memberIds.size());
	}
}
