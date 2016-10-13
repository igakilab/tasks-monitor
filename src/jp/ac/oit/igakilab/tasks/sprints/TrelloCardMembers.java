package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class TrelloCardMembers {
	public static TrelloCardMembers getInstance(TrelloBoard board, MemberTrelloIdTable mtable, String cardId){
		TrelloCard card = board.getCardById(cardId);
		if( card != null ){
			TrelloCardMembers mcard = new TrelloCardMembers(card.getId());
			card.getMemberIds().forEach((tmid) -> {
				String mid = mtable.getMemberId(tmid);
				if( mid != null ){
					mcard.addMemberId(mid);
				}
			});
			return mcard;
		}else{
			return null;
		}
	}

	private String trelloCardId;
	private List<String> memberIds;

	public TrelloCardMembers(String cardId){
		this.trelloCardId = cardId;
		this.memberIds = new ArrayList<String>();
	}

	public String getCardId() {
		return trelloCardId;
	}

	public void setCardId(String cardId) {
		this.trelloCardId = cardId;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public void addMemberId(String memberId){
		if( !memberIds.contains(memberId) ){
			memberIds.add(memberId);
		}
	}
}
