package jp.ac.oit.igakilab.tasks.dwr.forms.model;

import java.util.Set;

import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoardData;

public class TrelloBoardDataForm {
	public static TrelloBoardDataForm getInstance(TrelloBoardData data){
		if( data == null ) return null;
		TrelloBoardDataForm form = new TrelloBoardDataForm();
		form.setId(data.getId());
		form.setDesc(data.getDesc());
		form.setName(data.getName());
		form.setShortLink(data.getShortLink());
		form.setMemberIds(data.getMemberIds());
		form.setClosed(data.isClosed());
		return form;
	}

	private String id;
	private String name;
	private String desc;
	private String shortLink;
	private Set<String> memberIds;
	private boolean isClosed;

	public TrelloBoardDataForm(){}

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getShortLink() {
		return shortLink;
	}

	public void setShortLink(String shortLink) {
		this.shortLink = shortLink;
	}

	public Set<String> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(Set<String> memberIds) {
		this.memberIds = memberIds;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
