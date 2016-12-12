package jp.ac.oit.igakilab.tasks.trello.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TrelloBoard extends TrelloBoardData{
	protected List<TrelloCard> cards;
	protected List<TrelloList> lists;

	public TrelloBoard(){
		init();
	}

	public void init(){
		super.init();
		cards = new ArrayList<TrelloCard>();
		lists = new ArrayList<TrelloList>();
	}

	public void clear(){
		super.clear();
		cards.clear();
		lists.clear();
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

	public TrelloCard getCardById(String cid){
		for(TrelloCard card : cards){
			if( card.getId().equals(cid) ){
				return card;
			}
		}
		return null;
	}

	public TrelloCard getCardByName(String name){
		for(TrelloCard card : cards){
			if( card.getName().equals(name) ){
				return card;
			}
		}
		return null;
	}

	public boolean containsCardById(String cid){
		return getCardById(cid) != null;
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

	public TrelloList getListById(String lid){
		for(TrelloList list : lists){
			if( list.getId().equals(lid) ){
				return list;
			}
		}
		return null;
	}

	public List<TrelloList> getListsByNameMatches(String regex){
		List<TrelloList> result = new ArrayList<TrelloList>();
		for(TrelloList list : lists){
			if( list.getName().matches(regex) ){
				result.add(list);
			}
		}
		return result;
	}

	public List<TrelloCard> getCardsByListId(String lid){
		List<TrelloCard> pick = new ArrayList<TrelloCard>();
		cards.stream()
			.filter((card) -> card.getListId().equals(lid))
			.forEach((card) -> pick.add(card));
		return pick;
	}

	public List<TrelloCard> getCardsByListName(String lname){
		TrelloList list = lists.stream()
			.filter((li) -> li.getName().equals(lname))
			.findFirst()
			.orElse(null);

		if( list != null ){
			return getCardsByListId(list.getId());
		}
		return new ArrayList<TrelloCard>();
	}

	public List<TrelloCard> getCardsByListNameMatches(String regex){
		List<TrelloCard> cards = new ArrayList<TrelloCard>();

		lists.stream()
			.filter((li) -> li.getName().matches(regex))
			.forEach((li) -> {
				getCardsByListId(li.getId()).forEach(
					(card -> cards.add(card)));
			});

		return cards;
	}

	public boolean containsListById(String lid){
		return getListById(lid) != null;
	}

	public void printListsAndCards(PrintStream stream){
		stream.println("BOARD LISTS --");
		for(TrelloList list : lists){
			stream.println("\t" + list.toString());
		}
		stream.println("BOARD CARDS --");
		for(TrelloCard card : cards){
			stream.println("\t" + card.toString());
		}
	}

	public void printContentId() {
		System.out.println("==== BOARD ====");
		System.out.format("%s: %s\n", getId(), getName());

		System.out.println("\n==== LISTS ====");
		for(TrelloList list : getLists()){
			System.out.format("%s: %s\n", list.getId(), list.getName());
		}

		System.out.println("\n==== CARDS ====");
		for(TrelloCard card : getCards()){
			System.out.format("%s: %s\n", card.getId(), card.getName());
		}
	}
}
