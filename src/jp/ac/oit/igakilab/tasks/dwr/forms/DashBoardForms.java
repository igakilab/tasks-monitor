package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class DashBoardForms {
	/**
	 * スプリントのカードごとを表すクラス
	 * @author Ryokun
	 *
	 */
	public static class SprintCard extends TrelloCardForm{
		public static SprintCard getInstance(TrelloActionsCard card){
			//インスタンス生成
			SprintCard form = new SprintCard();
			//スーパークラスで情報の設定
			setValues(form, card);

			for(TrelloAction act : card.getActions()){
				System.out.println(act.dataString());
			}

			return null;
		}

		//カードが作られた日時
		private Date createdAt;
		//カードが最後にdoingに移動した日時
		private Date movedDoingAt;
		//カードが最後にdoneに移動した日時
		private Date movedDoneAt;

		public SprintCard(){
			createdAt = null;
			movedDoingAt = null;
			movedDoneAt = null;
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
	}


	/**
	 * かんばん(todo,doing,doneのタスクボード)を表すクラス
	 * @author Ryokun
	 *
	 */
	public static class Kanban{
		public static Kanban getInstance(TrelloBoard board){
			Kanban form = new Kanban();

			List<TrelloList> lists = board.getLists();
			for(TrelloList list : lists){
				if( list.getName().matches(TasksTrelloClientBuilder.REGEX_TODO) ){
					board.getCardsByListId(list.getId()).forEach((card ->
						form.getTodo().add(TrelloCardForm.getInstance(card))));
				}else if( list.getName().matches(TasksTrelloClientBuilder.REGEX_DOING) ){
					board.getCardsByListId(list.getId()).forEach((card ->
						form.getDoing().add(TrelloCardForm.getInstance(card))));
				}else if( list.getName().matches(TasksTrelloClientBuilder.REGEX_DONE) ){
					board.getCardsByListId(list.getId()).forEach((card ->
						form.getDone().add(TrelloCardForm.getInstance(card))));
				}
			}

			return form;
		}

		private List<TrelloCardForm> todo;
		private List<TrelloCardForm> doing;
		private List<TrelloCardForm> done;

		public Kanban(){
			todo = new ArrayList<TrelloCardForm>();
			doing = new ArrayList<TrelloCardForm>();
			done = new ArrayList<TrelloCardForm>();
		}

		public List<TrelloCardForm> getTodo() {
			return todo;
		}

		public void setTodo(List<TrelloCardForm> todo) {
			this.todo = todo;
		}

		public List<TrelloCardForm> getDoing() {
			return doing;
		}

		public void setDoing(List<TrelloCardForm> doing) {
			this.doing = doing;
		}

		public List<TrelloCardForm> getDone() {
			return done;
		}

		public void setDone(List<TrelloCardForm> done) {
			this.done = done;
		}
	}


	/**
	 * ダッシュボードに渡すデータの集合体
	 * ボードの情報と、スプリントに関する情報がある
	 * @author Ryokun
	 *
	 */
	public static class DashBoardData{
		public static DashBoardData getInstance(TrelloBoard board, Sprint sprint){
			//インスタンス作成
			DashBoardData form = new DashBoardData();
			//ボード情報の設定
			if( board != null ){
				form.setBoardId(board.getId());
				form.setBoardName(board.getName());
				form.setBoardUrl(board.getShortLink());
				form.setKanban(Kanban.getInstance(board));
			}
			//スプリント情報の設定
			if( board != null && sprint != null ){
				form.setSprintId(sprint.getId());
				form.setFinishDate(sprint.getFinishDate());
				for(String cid : sprint.getTrelloCardIds()){
					TrelloCard ctmp = board.getCardById(cid);
					if( ctmp instanceof TrelloActionsCard ){
						TrelloActionsCard card = (TrelloActionsCard)ctmp;
						form.getSprintCards().add(SprintCard.getInstance(card));
					}
				}
			}

			return form;
		}

		//ボードの情報
		private String boardId;
		private String boardName;
		private String boardUrl;
		private Kanban kanban;
		//スプリントの情報
		private String sprintId;
		private Date finishDate;
		private List<SprintCard> sprintCards;

		public DashBoardData(){
			boardId = null;
			boardName = null;
			boardUrl = null;
			kanban = null;
			sprintId = null;
			finishDate = null;
			sprintCards = new ArrayList<SprintCard>();
		}

		public String getBoardId() {
			return boardId;
		}

		public void setBoardId(String boardId) {
			this.boardId = boardId;
		}

		public String getBoardName() {
			return boardName;
		}

		public void setBoardName(String boardName) {
			this.boardName = boardName;
		}

		public String getBoardUrl() {
			return boardUrl;
		}

		public void setBoardUrl(String boardUrl) {
			this.boardUrl = boardUrl;
		}

		public Kanban getKanban() {
			return kanban;
		}

		public void setKanban(Kanban kanban) {
			this.kanban = kanban;
		}

		public String getSprintId() {
			return sprintId;
		}

		public void setSprintId(String sprintId) {
			this.sprintId = sprintId;
		}

		public Date getFinishDate() {
			return finishDate;
		}

		public void setFinishDate(Date finishDate) {
			this.finishDate = finishDate;
		}

		public List<SprintCard> getSprintCards() {
			return sprintCards;
		}

		public void setSprintCards(List<SprintCard> sprintCards) {
			this.sprintCards = sprintCards;
		}
	}
}
