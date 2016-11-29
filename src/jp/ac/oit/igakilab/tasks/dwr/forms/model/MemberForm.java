package jp.ac.oit.igakilab.tasks.dwr.forms.model;

import jp.ac.oit.igakilab.tasks.members.Member;

public class MemberForm {
	public static MemberForm getInstance(Member member){
		MemberForm form = new MemberForm();
		form.setId(member.getId());
		if( member.getName() != null )
			form.setName(member.getName());
		if( member.getTrelloId() != null )
			form.setTrelloId(member.getTrelloId());
		if( member.getSlackId() != null )
			form.setSlackId(member.getSlackId());
		form.setAdmin(member.isAdmin());
		return form;
	}

	public static Member convertToMember(MemberForm form){
		Member member = new Member(form.getId());
		if( form.getName() != null )
			member.setName(form.getName());
		if( form.getTrelloId() != null )
			member.setTrelloId(form.getTrelloId());
		if( form.getSlackId() != null )
			member.setSlackId(form.getSlackId());
		member.setAdmin(form.isAdmin());
		return member;
	}

	private String id;
	private String name;
	private String trelloId;
	private String slackId;
	private boolean isAdmin;

	public MemberForm(){
		id = null;
		name = null;
		trelloId = null;
		slackId = null;
		isAdmin = false;
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
