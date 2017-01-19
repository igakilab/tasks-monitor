package jp.ac.oit.igakilab.tasks.dwr.forms;

import static jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloBoardDataForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class HubotApiForms {
	/**
	 * 現在進行中のスプリントに対する情報を格納します
	 * @author taka
	 *
	 */
	public static class CurrentSprint{
		//GET INSTANCE
		public static CurrentSprint getInstance
		(Sprint csprint, TrelloBoard board, MemberTrelloIdTable ttb){
			//formを生成
			CurrentSprint form = new CurrentSprint();

			//スプリントを変換
			if( csprint != null ){
				form.setSprint(NotpSprintForm.getInstance(csprint));
			}

			//ボードデータを変換
			if( board != null ){
				form.setBoard(TrelloBoardDataForm.getInstance(board));
			}

			//タスクカードを変換
			if( csprint != null && board != null ){
				//スプリント対象カードをイテレーション
				for(String cid : csprint.getTrelloCardIds()){
					//カードとリストを取得
					TrelloCard card = board.getCardById(cid);
					TrelloList list = card != null ? board.getListById(card.getListId()) : null;

					//カードをタスクリストに追加
					if( card != null && list != null ){
						//カードフォームに変換
						NotpTrelloCardForm cform = NotpTrelloCardForm.getInstance(card);

						//trelloIdをslackIdに変換
						if( ttb != null ){
							List<String> slackIds = new ArrayList<>();
							for(String tmid : card.getMemberIds()){
								Member m = ttb.getMember(tmid);
								if( m != null ) slackIds.add(m.getSlackId());
							}
							cform.setMemberIds(slackIds);
						}

						//タスクリストに追加
						if( list.getName().matches(REGEX_TODO) ){
							form.addTodoTask(cform);
						}else if( list.getName().matches(REGEX_DOING) ){
							form.addDoingTask(cform);
						}else if( list.getName().matches(REGEX_DONE) ){
							form.addDoneTask(cform);
						}
					}
				}
			}

			return form;
		}

		private NotpSprintForm sprint;
		private TrelloBoardDataForm board;
		private List<NotpTrelloCardForm> tasksTodo;
		private List<NotpTrelloCardForm> tasksDoing;
		private List<NotpTrelloCardForm> tasksDone;

		public CurrentSprint(){
			sprint = null;
			board = null;
			tasksTodo = new ArrayList<>();
			tasksDoing = new ArrayList<>();
			tasksDone = new ArrayList<>();
		}

		public void addTodoTask(NotpTrelloCardForm card){
			tasksTodo.add(card);
		}

		public void addDoingTask(NotpTrelloCardForm card){
			tasksDoing.add(card);
		}

		public void addDoneTask(NotpTrelloCardForm card){
			tasksDone.add(card);
		}

		public NotpSprintForm getSprint() {
			return sprint;
		}
		public void setSprint(NotpSprintForm sprint) {
			this.sprint = sprint;
		}
		public TrelloBoardDataForm getBoard() {
			return board;
		}
		public void setBoard(TrelloBoardDataForm board) {
			this.board = board;
		}
		public List<NotpTrelloCardForm> getTasksTodo() {
			return tasksTodo;
		}
		public void setTasksTodo(List<NotpTrelloCardForm> tasksTodo) {
			this.tasksTodo = tasksTodo;
		}
		public List<NotpTrelloCardForm> getTasksDoing() {
			return tasksDoing;
		}
		public void setTasksDoing(List<NotpTrelloCardForm> tasksDoing) {
			this.tasksDoing = tasksDoing;
		}
		public List<NotpTrelloCardForm> getTasksDone() {
			return tasksDone;
		}
		public void setTasksDone(List<NotpTrelloCardForm> tasksDone) {
			this.tasksDone = tasksDone;
		}
	}


	/**
	 * Json形式にするためにDate型の要素を無効化したTrelloCardFormクラスです
	 * @author taka
	 *
	 */
	public static class NotpTrelloCardForm extends TrelloCardForm{
		public static NotpTrelloCardForm getInstance(TrelloCard card){
			NotpTrelloCardForm form = new NotpTrelloCardForm();
			setValues(form, card);

			Date due = form.getDue();
			if( due != null ){
				form.setDueTime(due.getTime());
				form.setDue(null);
			}

			return form;
		}

		private Long dueTime;

		public NotpTrelloCardForm(){
			dueTime = null;
		}

		public Long getDueTime() {
			return dueTime;
		}
		public void setDueTime(Long dueTime) {
			this.dueTime = dueTime;
		}
	}


	/**
	 * Json形式にするためにDate型の要素を無効化したSprintFormクラスです
	 * @author taka
	 *
	 */
	public static class NotpSprintForm extends SprintForm{
		public static NotpSprintForm getInstance(Sprint sprint){
			NotpSprintForm form = new NotpSprintForm();
			setValues(form, sprint);

			if( form.getBeginDate() != null ){
				form.setBeginDateTime(form.getBeginDate().getTime());
				form.setBeginDate(null);
			}
			if( form.getFinishDate() != null ){
				form.setFinishDateTime(form.getFinishDate().getTime());
				form.setFinishDate(null);
			}
			if( form.getClosedDate() != null ){
				form.setClosedDateTime(form.getClosedDate().getTime());
				form.setClosedDate(null);
			}

			return form;
		}

		private Long beginDateTime;
		private Long finishDateTime;
		private Long closedDateTime;

		public NotpSprintForm(){
			beginDateTime = null;
			finishDateTime = null;
			closedDateTime = null;
		}

		public Long getBeginDateTime() {
			return beginDateTime;
		}
		public void setBeginDateTime(Long beginDateTime) {
			this.beginDateTime = beginDateTime;
		}
		public Long getFinishDateTime() {
			return finishDateTime;
		}
		public void setFinishDateTime(Long finishDateTime) {
			this.finishDateTime = finishDateTime;
		}
		public Long getClosedDateTime() {
			return closedDateTime;
		}
		public void setClosedDateTime(Long closedDateTime) {
			this.closedDateTime = closedDateTime;
		}
	}
}
