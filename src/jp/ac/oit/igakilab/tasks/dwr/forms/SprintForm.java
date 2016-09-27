package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;

import jp.ac.oit.igakilab.tasks.sprints.Sprint;

public class SprintForm {
	public static SprintForm getInstance(Sprint sprint){
		if( sprint == null ) return null;

		SprintForm form = new SprintForm();
		form.setId(sprint.getId());
		form.setBoardId(sprint.getBoardId());
		form.setBeginDate(sprint.getBeginDate());
		form.setFinishDate(sprint.getFinishDate());
		form.setClosed(sprint.isClosed());

		return form;
	}


	private String id;
	private String boardId;
	private Date beginDate;
	private Date finishDate;
	private boolean isClosed;

	public SprintForm(){
		id = null;
		boardId = null;
		beginDate = null;
		finishDate = null;
		isClosed = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
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

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
