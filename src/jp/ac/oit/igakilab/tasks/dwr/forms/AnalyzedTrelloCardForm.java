package jp.ac.oit.igakilab.tasks.dwr.forms;

import static jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder.*;

import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard.ListMovement;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class AnalyzedTrelloCardForm  extends TrelloCardForm{
	public static AnalyzedTrelloCardForm getInstance
	(TrelloActionsCard card, TrelloBoard board, Date begin, Date end, MemberTrelloIdTable ttb){
		//インスタンス初期化
		AnalyzedTrelloCardForm form = new AnalyzedTrelloCardForm();
		setValues(form, card, ttb);

		//作成時間(createdAt)の設定
		form.setCreatedAt(card.getCreatedAt());

		//リスト移動日時の設定
		List<ListMovement> movements = card.getListMovement(begin, end);
		for(int i=0; i<movements.size(); i++){
			ListMovement move = movements.get(i);
			TrelloList after = board.getListById(move.getListIdAfter());

			if( after != null ){
				if( after.getName().matches(REGEX_DOING) && form.movedDoingAt == null ){
					form.movedDoingAt = move.getDate();
					form.movedDoneAt = null;
				}else if( after.getName().matches(REGEX_DONE) && form.movedDoingAt != null ){
					form.movedDoneAt = move.getDate();
				}
			}
		}

		//作業時間の計算
		if( form.movedDoingAt != null && form.movedDoneAt != null ){
			form.workingMinutes =
				(int)((form.movedDoneAt.getTime() - form.movedDoingAt.getTime()) / 1000 / 60);
		}

		return form;
	}

	private Date createdAt;
	private Date movedDoingAt;
	private Date movedDoneAt;
	private int workingMinutes;

	public AnalyzedTrelloCardForm(){
		createdAt = null;
		movedDoingAt = null;
		movedDoneAt = null;
		workingMinutes = 0;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getMovedDoingAt() {
		return movedDoingAt;
	}

	public void setMovedDoingAt(Date movedDoingAt) {
		this.movedDoingAt = movedDoingAt;
	}

	public Date getMovedDoneAt() {
		return movedDoneAt;
	}

	public void setMovedDoneAt(Date movedDoneAt) {
		this.movedDoneAt = movedDoneAt;
	}

	public int getWorkingMinutes() {
		return workingMinutes;
	}

	public void setWorkingMinutes(int workingMinutes) {
		this.workingMinutes = workingMinutes;
	}
}
