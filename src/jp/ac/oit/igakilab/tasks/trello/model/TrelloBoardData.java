package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.HashSet;
import java.util.Set;

public class TrelloBoardData {
	protected String id;
	protected String name;
	protected String desc;
	protected String shortLink;
	protected Set<String> memberIds;
	protected boolean isClosed;

	public TrelloBoardData(){
		init();
	}

	public void init(){
		id = null;
		name = null;
		desc = null;
		shortLink = null;
		memberIds = new HashSet<String>();
		isClosed = false;
	}

	public void clear(){
		id = null;
		name = null;
		desc = null;
		shortLink = null;
		memberIds.clear();
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

	public String getShortLink() {
		return shortLink;
	}

	public void setShortLink(String shortLink) {
		this.shortLink = shortLink;
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

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public String toString(){
		//BOARD: [boardId] [boardName] [closed]
		return String.format("BOARD: %s %s %s",
			getId(), getName(), (isClosed ?  "CLOSED" : "OPEN"));
	}
}
