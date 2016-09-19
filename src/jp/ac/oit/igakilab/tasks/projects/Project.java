package jp.ac.oit.igakilab.tasks.projects;

import java.util.ArrayList;
import java.util.List;

public class Project {
	private String id;
	private String name;
	private List<String> members;

	public Project(String id){
		this.id = id;
		this.name = "noname";
		this.members = new ArrayList<String>();
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

	public List<String> getMembers(){
		return members;
	}

	public void addMember(String member){
		if( members.contains(member) ){
			members.add(member);
		}
	}

	public String toString(){
		return String.format("Project: %s %s %dmember(s)",
			id, name, members.size());
	}
}
