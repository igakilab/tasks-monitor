package jp.ac.oit.igakilab.tasks.dwr.forms;

import static jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard.ListMovement;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class AnalyzedTrelloCardForm  extends TrelloCardForm{
	public static class ListUpdate{
		public static ListUpdate getInstance(Date moveAt, String listId, TrelloBoard board){
			ListUpdate form = new ListUpdate();

			form.setMovedAt(moveAt);
			form.setListId(listId);

			if( board != null ){
				TrelloList list = board.getListById(listId);

				if( list != null ){
					form.setListName(list.getName());

					if( list.getName().matches(REGEX_TODO) ){
						form.setType("todo");
					}else if( list.getName().matches(REGEX_DOING) ){
						form.setType("doing");
					}else if( list.getName().matches(REGEX_DONE) ){
						form.setType("done");
					}else{
						form.setType("unknown");
					}
				}
			}else{
				form.setType("unknown");
			}

			return form;
		}


		private Date movedAt;
		private String listId;
		private String listName;
		private String type;

		public Date getMovedAt() {
			return movedAt;
		}
		public void setMovedAt(Date movedAt) {
			this.movedAt = movedAt;
		}
		public String getListId() {
			return listId;
		}
		public void setListId(String listId) {
			this.listId = listId;
		}
		public String getListName() {
			return listName;
		}
		public void setListName(String listName) {
			this.listName = listName;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}

	public static AnalyzedTrelloCardForm getInstance
	(TrelloActionsCard card, TrelloBoard board, Date begin, Date end, MemberTrelloIdTable ttb){
		//インスタンス初期化
		AnalyzedTrelloCardForm form = new AnalyzedTrelloCardForm();
		setValues(form, card, ttb);

		//作成時間(createdAt)の設定
		form.setCreatedAt(card.getCreatedAt());

		//リスト移動履歴の取得
		List<ListMovement> movements = card.getListMovement();
		//リスト初期位置を格納
		List<ListUpdate> listpos = new ArrayList<ListUpdate>();
		if( begin != null ){
			if( movements.size() > 0 ){
				listpos.add(ListUpdate.getInstance(begin, movements.get(0).getListIdBefore(), board));
			}else{
				listpos.add(ListUpdate.getInstance(begin, card.getListId(), board));
			}
		}
		//リスト移動履歴を格納
		for(int i=0; i<movements.size(); i++){
			ListMovement move = movements.get(i);
			listpos.add(ListUpdate.getInstance(move.getDate(), move.getListIdAfter(), board));
		}
		form.listUpdates = listpos;

		//完了フラグを設定
		form.finished = listpos.get(listpos.size() - 1).getType().equals("done");

		//作業時間を計算
		long worktime = 0;
		end = (end == null ? Calendar.getInstance().getTime() : end);
		for(int i=0; i<listpos.size(); i++){
			ListUpdate pos = listpos.get(i);

			if( pos.getType().equals("doing") ){
				Date head = pos.getMovedAt();
				Date rear;

				if( i < listpos.size() - 1 ){
					rear = listpos.get(i + 1).getMovedAt();
				}else{
					rear = end;
				}

				if( head != null && rear != null ){
					worktime += (rear.getTime() - head.getTime());
				}
			}
		}
		form.workingMinutes = (int)(worktime / 1000 / 60);

		return form;
	}

	private Date createdAt;
	private List<ListUpdate> listUpdates;
	private boolean finished;
	private int workingMinutes;

	private List<String> tags;

	public AnalyzedTrelloCardForm(){
		createdAt = null;
		listUpdates = null;
		finished = false;
		workingMinutes = 0;
		tags = null;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<ListUpdate> getListUpdates() {
		return listUpdates;
	}

	public void setListUpdates(List<ListUpdate> listUpdates) {
		this.listUpdates = listUpdates;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public int getWorkingMinutes() {
		return workingMinutes;
	}

	public void setWorkingMinutes(int workingMinutes) {
		this.workingMinutes = workingMinutes;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public void addTag(String tag){
		if( this.tags == null ) this.tags = new ArrayList<String>();
		this.tags.add(tag);
	}
}
