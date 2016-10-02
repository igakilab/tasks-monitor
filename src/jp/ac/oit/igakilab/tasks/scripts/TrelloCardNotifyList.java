package jp.ac.oit.igakilab.tasks.scripts;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class TrelloCardNotifyList {
	public static void main(String[] args){
		TrelloCardNotifyList list = new TrelloCardNotifyList();
		list.add("coice", new TrelloCard("2c2"), null, null);
		list.add("koikce", new TrelloCard("2c2"), null, null);
		System.out.println(list.getNotifyMemberList());
	}

	public static class NotifyCard{
		private String memberId;
		private TrelloBoard board;
		private TrelloList list;
		private TrelloCard card;

		public NotifyCard(String m0, TrelloCard c0, TrelloList l0, TrelloBoard b0){
			memberId = m0;
			board = b0;
			list = l0;
			card = c0;
		}

		public String getMemberId() {
			return memberId;
		}

		public TrelloBoard getBoard() {
			return board;
		}

		public TrelloList getList() {
			return list;
		}

		public TrelloCard getCard() {
			return card;
		}

		@Override
		public boolean equals(Object obj){
			try{
				NotifyCard nc = (NotifyCard)obj;
				return ( memberId.equals(nc.getMemberId())
					&& card.getId().equals(nc.getCard().getId()) );
			}catch(ClassCastException e0){
				return false;
			}
		}

		@Override
		public int hashCode(){
			return 2;
		}
	}


	private List<NotifyCard> notifyCards;

	public TrelloCardNotifyList(){
		this.notifyCards = new ArrayList<NotifyCard>();
	}

	public boolean add(String memberId, TrelloCard card, TrelloList list, TrelloBoard board){
		NotifyCard nc = new NotifyCard(memberId, card, list, board);

		if( (memberId != null && card != null) && !notifyCards.contains(nc) ){
			notifyCards.add(nc);
			return true;
		}else{
			return false;
		}
	}

	public List<String> getNotifyMemberList(){
		List<String> members = new ArrayList<String>();

		notifyCards.forEach((nc) -> {
			if( !members.contains(nc.getMemberId()) ){
				members.add(nc.getMemberId());
			}
		});

		return members;
	}

	public List<NotifyCard> getNotifyCardsByMemberId(String memberId){
		List<NotifyCard> result = new ArrayList<NotifyCard>();

		notifyCards.forEach((nc) -> {
			if( nc.getMemberId().equals(memberId) ){
				result.add(nc);
			}
		});

		return result;
	}

	public int size(){
		return notifyCards.size();
	}
}
