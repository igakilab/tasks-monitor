package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;

public class BoardMenuForms {
	public static class BoardInfo{
		private TrelloBoardDataForm board;
		private Date lastUpdate;
		public TrelloBoardDataForm getBoard() {
			return board;
		}
		public void setBoard(TrelloBoardDataForm board) {
			this.board = board;
		}
		public Date getLastUpdate() {
			return lastUpdate;
		}
		public void setLastUpdate(Date lastUpdate) {
			this.lastUpdate = lastUpdate;
		}
	}
}
