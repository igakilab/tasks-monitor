package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;

public class TrelloBoardInfoForm {
	private String id;
	private Date lastUpdate;

	public TrelloBoardInfoForm(){
		id = null;
		lastUpdate = null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
