package jp.ac.oit.igakilab.tasks.sprints;

public class SprintResultTrelloCard {
	private String trelloCardId;
	private List<String> memberIds;
	private boolean finished;

	public SprintResultTrelloCard(){
	}

	public String getTrelloCardId() {
		return trelloCardId;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public boolean isFinished() {
		return finished;
	}
}
