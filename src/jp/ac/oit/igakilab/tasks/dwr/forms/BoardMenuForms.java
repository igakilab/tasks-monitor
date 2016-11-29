package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloBoardDataForm;

public class BoardMenuForms {
	public static class BoardInfo{
		private TrelloBoardDataForm data;
		private List<String> members;
		private Date lastUpdate;
		public TrelloBoardDataForm getData() {
			return data;
		}
		public void setData(TrelloBoardDataForm board) {
			this.data = board;
		}
		public List<String> getMembers() {
			return members;
		}
		public void setMembers(List<String> members) {
			this.members = members;
		}
		public Date getLastUpdate() {
			return lastUpdate;
		}
		public void setLastUpdate(Date lastUpdate) {
			this.lastUpdate = lastUpdate;
		}
	}
}
