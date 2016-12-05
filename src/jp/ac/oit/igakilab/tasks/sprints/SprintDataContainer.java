package jp.ac.oit.igakilab.tasks.sprints;

public class SprintDataContainer {
	private Sprint sprint;
	private SprintResult result;

	public SprintDataContainer(){
		sprint = null;
		result = null;
	}

	public SprintDataContainer(Sprint sprint, SprintResult result){
		this();
		setSprint(sprint);
		setSprintResult(result);
	}

	public void setSprint(Sprint sprint){
		if( result == null || result.getSprintId().equals(sprint.getId()) ){
			this.sprint = sprint;
		}
	}

	public void setSprintResult(SprintResult result){
		if( sprint == null || sprint.getId().equals(result.getSprintId()) ){
			this.result = result;
		}
	}

	public Sprint getSprint(){
		return sprint;
	}

	public SprintResult getSprintResult(){
		return result;
	}

	public String getSprintId(){
		return sprint != null ? sprint.getId() : (result != null ? result.getSprintId() : null);
	}

	public boolean isClosed(){
		return sprint.isClosed() && result != null;
	}
}
