package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class CardResult{
	/**
	 * TrelloCardからCardResultを生成します
	 * @param card TrelloCardオブジェクト
	 * @param ttb trelloIdからmemberIdへの返還テーブル
	 * @param finished 完了フラグ
	 * @return
	 */
	public static CardResult
	getInstance(TrelloCard card, MemberTrelloIdTable ttb, boolean finished){
		CardResult cres = new CardResult();
		cres.setCardId(card.getId());
		cres.getMemberIds().forEach((tmid) -> {
			if( ttb != null ){
				cres.addMemberId(tmid);
			}
		});
		cres.setFinished(finished);
		return cres;
	}

	private String cardId;
	private List<String> memberIds;
	private boolean finished;

	public CardResult(){
		cardId = null;
		memberIds = new ArrayList<String>();
		finished = false;
	}

	public CardResult(String cardId){
		this();
		this.cardId = cardId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public boolean containsMemberId(String mid){
		return memberIds.contains(mid);
	}

	public void addMemberId(String mid){
		memberIds.add(mid);
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
