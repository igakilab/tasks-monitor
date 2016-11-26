package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;

public class KanbanForm {
	public static KanbanForm getInstance(TrelloBoard board){
		KanbanForm form = new KanbanForm();
		form.setBoardId(board.getId());
		form.setBoardName(board.getName());

		List<TrelloList> lists = board.getLists();
		for(TrelloList list : lists){
			if( list.getName().matches("(?i)to\\s*do") ){
				board.getCardsByListId(list.getId()).forEach((card ->
					form.getTodo().add(TrelloCardForm.getInstance(card))));
			}else if( list.getName().matches("(?i)doing") ){
				board.getCardsByListId(list.getId()).forEach((card ->
					form.getDoing().add(TrelloCardForm.getInstance(card))));
			}else if( list.getName().matches("(?i)done") ){
				board.getCardsByListId(list.getId()).forEach((card ->
					form.getDone().add(TrelloCardForm.getInstance(card))));
			}
		}

		return form;
	}

	private String boardId;
	private String boardName;
	private List<TrelloCardForm> todo;
	private List<TrelloCardForm> doing;
	private List<TrelloCardForm> done;

	public KanbanForm(){
		boardId = null;
		boardName = null;
		todo = new ArrayList<TrelloCardForm>();
		doing = new ArrayList<TrelloCardForm>();
		done = new ArrayList<TrelloCardForm>();
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
