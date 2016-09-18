package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;

public class TrelloBoardInfoForm {
	private String boardId;
	private Date lastUpdate;

	public TrelloBoardInfoForm(){
		boardId = null;
		lastUpdate = null;
	}

	public String getBoardId() {
		return boardId;
	}
	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
