package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.MemberForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardUrl;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class DashBoardForms {
	/**
	 * スプリントのカードごとを表すクラス
	 * @author Ryokun
	 *
	 */
//	public static class SprintCard extends TrelloCardForm{
//		public static SprintCard getInstance(TrelloActionsCard card, TrelloBoard board){
//			//インスタンス生成
//			SprintCard form = new SprintCard();
//			//スーパークラスで情報の設定
//			setValues(form, card);
//			//完了フラグを無効にしておく
//			form.setFinished(false);
//
//			//リスト移動履歴を取得
//			List<ListMovement> movements = card.getListMovement();
//
//			for(int i=0; i<movements.size(); i++){
//				ListMovement m = movements.get(i);
//				TrelloList after = board.getListById(m.getListIdAfter());
//
//				if( after != null ){
//					if( after.getName().matches(REGEX_DOING) && form.getMovedDoingAt() == null ){
//						form.setMovedDoingAt(m.getDate());
//						form.setMovedDoneAt(null);
//					}else if( after.getName().matches(REGEX_DONE) && form.getMovedDoingAt() != null ){
//						form.setMovedDoneAt(m.getDate());
//					}else{
//						form.setMovedDoneAt(null);
//					}
//				}
//			}
//
//			if( form.getMovedDoneAt() != null ){
//				form.setFinished(true);
//			}
//
//			return form;
//		}
//
//		//カードがdoneに移動しているかどうか
//		private boolean finished;
//		//カードが作られた日時
//		private Date createdAt;
//		//カードが最後にdoingに移動した日時
//		private Date movedDoingAt;
//		//カードが最後にdoneに移動した日時
//		private Date movedDoneAt;
//
//		public SprintCard(){
//			finished = false;
//			createdAt = null;
//			movedDoingAt = null;
//			movedDoneAt = null;
//		}
//
//		public boolean isFinished() {
//			return finished;
//		}
//
//		public void setFinished(boolean finished) {
//			this.finished = finished;
//		}
//
//		public Date getCreatedAt() {
//			return createdAt;
//		}
//		public void setCreatedAt(Date createdAt) {
//			this.createdAt = createdAt;
//		}
//		public Date getMovedDoingAt() {
//			return movedDoingAt;
//		}
//		public void setMovedDoingAt(Date movedDoingAt) {
//			this.movedDoingAt = movedDoingAt;
//		}
//		public Date getMovedDoneAt() {
//			return movedDoneAt;
//		}
//		public void setMovedDoneAt(Date movedDoneAt) {
//			this.movedDoneAt = movedDoneAt;
//		}
//	}


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
					board.getCardsByListId(list.getId()).forEach((card) -> {
						if( !card.isClosed() ){
							form.getTodo().add(TrelloCardForm.getInstance(card));
						}
					});
				}else if( list.getName().matches(TasksTrelloClientBuilder.REGEX_DOING) ){
					board.getCardsByListId(list.getId()).forEach((card) -> {
						if( !card.isClosed() ){
							form.getDoing().add(TrelloCardForm.getInstance(card));
						}
					});
				}else if( list.getName().matches(TasksTrelloClientBuilder.REGEX_DONE) ){
					board.getCardsByListId(list.getId()).forEach((card) -> {
						if( !card.isClosed() ){
							form.getDone().add(TrelloCardForm.getInstance(card));
						}
					});
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
		public static DashBoardData getInstance(TrelloBoard board, Sprint sprint, MemberTrelloIdTable ttb){
			//インスタンス作成
			DashBoardData form = new DashBoardData();
			//ボード情報の設定
			if( board != null ){
				form.setBoardId(board.getId());
				form.setBoardName(board.getName());
				TrelloBoardUrl url = new TrelloBoardUrl(board.getShortLink());
				form.setBoardUrl(url.getUrl());
				form.setKanban(Kanban.getInstance(board));
				if( ttb != null ){
					board.getMemberIds().forEach((tmid) -> {
						Member member = ttb.getMember(tmid);
						if( member != null ){
							form.addMember(MemberForm.getInstance(member));
						}
					});
				}
			}
			//スプリント情報の設定
			if( board != null && sprint != null ){
				form.setSprintId(sprint.getId());
				form.setBeginDate(sprint.getBeginDate());
				form.setFinishDate(sprint.getFinishDate());
				for(String cid : sprint.getTrelloCardIds()){
					TrelloCard ctmp = board.getCardById(cid);
					if( ctmp instanceof TrelloActionsCard ){
						TrelloActionsCard card = (TrelloActionsCard)ctmp;
						form.addSprintCard(AnalyzedTrelloCardForm.getInstance(
							card, board, sprint.getBeginDate(), null, ttb));
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
		private List<MemberForm> members;
		//スプリントの情報
		private String sprintId;
		private Date beginDate;
		private Date finishDate;
		private List<AnalyzedTrelloCardForm> sprintCards;

		public DashBoardData(){
			boardId = null;
			boardName = null;
			boardUrl = null;
			kanban = null;
			members = new ArrayList<>();
			sprintId = null;
			beginDate = null;
			finishDate = null;
			sprintCards = new ArrayList<>();
		}

		public void addSprintCard(AnalyzedTrelloCardForm card){
			if( sprintCards == null ) sprintCards = new ArrayList<>();
			sprintCards.add(card);
		}

		public void addMember(MemberForm member){
			if( members == null ) members = new ArrayList<>();
			members.add(member);
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
		public List<MemberForm> getMembers() {
			return members;
		}
		public void setMembers(List<MemberForm> members) {
			this.members = members;
		}
		public String getSprintId() {
			return sprintId;
		}
		public void setSprintId(String sprintId) {
			this.sprintId = sprintId;
		}
		public Date getBeginDate() {
			return beginDate;
		}
		public void setBeginDate(Date beginDate) {
			this.beginDate = beginDate;
		}
		public Date getFinishDate() {
			return finishDate;
		}
		public void setFinishDate(Date finishDate) {
			this.finishDate = finishDate;
		}
		public List<AnalyzedTrelloCardForm> getSprintCards() {
			return sprintCards;
		}
		public void setSprintCards(List<AnalyzedTrelloCardForm> sprintCards) {
			this.sprintCards = sprintCards;
		}
	}
}
