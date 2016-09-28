package jp.ac.oit.igakilab.tasks.members;

public class Member {
	private String id;
	private String name;
	private String trelloId;
	private String slackId;
	private boolean isAdmin;

	public Member(String id){
		this.id = id;
		this.name = "";
		this.trelloId = null;
		this.slackId = null;
		this.isAdmin = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTrelloId() {
		return trelloId;
	}

	public void setTrelloId(String trelloId) {
		this.trelloId = trelloId;
	}

	public String getSlackId() {
		return slackId;
	}

	public void setSlackId(String slackId) {
		this.slackId = slackId;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
}
