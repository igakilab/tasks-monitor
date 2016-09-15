package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrelloBoard {
	protected String id;
	protected String name;
	protected String desc;
	protected Set<String> memberIds;
	protected boolean isClosed;

	protected List<TrelloCard> cards;
	protected List<TrelloList> lists;

	public void init(){
		id = null;
		name = null;
		desc = null;
		memberIds = new HashSet<String>();
		isClosed = false;
		cards = new ArrayList<TrelloCard>();
		lists = new ArrayList<TrelloList>();
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

	public List<TrelloCard> getCards() {
		return cards;
	}

	public void clearCards(){
		cards.clear();
	}

	public void addCard(TrelloCard card){
		cards.add(card);
	}

	public void removeCard(String cid){
		for(int i=0 ;i<cards.size(); i++){
			if( cards.get(i).getId().equals(cid) ){
				cards.remove(i);
				return;
			}
		}
	}

	public List<TrelloList> getLists() {
		return lists;
	}

	public void clearLists(){
		lists.clear();
	}

	public void addList(TrelloList list){
		lists.add(list);
	}

	public void removeList(String lid){
		for(int i=0 ;i<lists.size(); i++){
			if( lists.get(i).getId().equals(lid) ){
				lists.remove(i);
				return;
			}
		}
	}


}
