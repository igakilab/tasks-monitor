package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class TrelloCardForm {
	protected static void setValues(TrelloCardForm form, TrelloCard card, List<String> memberIds){
		form.setId(card.getId());
		form.setName(card.getName());
		form.setDesc(card.getDesc());
		form.setListId(card.getListId());
		form.setDue(card.getDue());

		if( memberIds != null ){
			form.getMemberIds().clear();
			form.getMemberIds().addAll(memberIds);
		}

		form.getTrelloMemberIds().clear();
		form.getTrelloMemberIds().addAll(card.getMemberIds());

		form.setClosed(card.isClosed());
	}

	protected static void setValues(TrelloCardForm form, TrelloCard card, MemberTrelloIdTable ttb){
		if( ttb != null ){
			List<String> memberIds = new ArrayList<String>();
			card.getMemberIds().forEach((tmid) -> {
				String mid = ttb.getMemberId(tmid);
				if( mid != null ){
					memberIds.add(mid);
				}
			});
			setValues(form, card, memberIds);
		}else{
			setValues(form, card, (List<String>)null);
		}
	}

	protected static void setValues(TrelloCardForm form, TrelloCard card){
		setValues(form, card, (List<String>)null);
	}

	public static TrelloCardForm getInstance(TrelloCard card, List<String> memberIds){
		TrelloCardForm form = new TrelloCardForm();
		setValues(form, card, memberIds);
		return form;
	}

	public static TrelloCardForm getInstance(TrelloCard card, MemberTrelloIdTable ttb){
		TrelloCardForm form = new TrelloCardForm();
		setValues(form, card, ttb);
		return form;
	}

	public static TrelloCardForm getInstance(TrelloCard card){
		TrelloCardForm form = new TrelloCardForm();
		setValues(form, card);
		return form;
	}

	public static TrelloCard convert(TrelloCardForm form){
		TrelloCard card = new TrelloCard();

		card.setId(form.getId());
		card.setName(form.getName());
		card.setDesc(form.getDesc());
		card.setListId(form.getListId());
		card.setDue(form.getDue());
		if( form.getTrelloMemberIds() != null ){
			card.getMemberIds().addAll(form.getTrelloMemberIds());
		}
		card.setClosed(form.isClosed());

		return card;
	}

	public static TrelloCard convert(TrelloCardForm form, MemberTrelloIdTable ttb){
		if( form.getMemberIds() != null && ttb != null ){
			form.getMemberIds().forEach((mid) -> {
				String tmid = ttb.getTrelloId(mid);
				if( tmid != null ){
					if( !form.getTrelloMemberIds().contains(tmid) ){
						form.getTrelloMemberIds().add(tmid);
					}
				}
			});
		}
		return convert(form);
	}

	private String id;
	private String name;
	private String desc;
	private String listId;
	private Date due;
	private List<String> memberIds;
	private List<String> trelloMemberIds;
	private boolean isClosed;

	public TrelloCardForm(){
		id = null;
		name = null;
		desc = null;
		listId = null;
		due = null;
		memberIds = new ArrayList<String>();
		trelloMemberIds = new ArrayList<String>();
		isClosed = false;
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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public Date getDue() {
		return due;
	}

	public void setDue(Date due) {
		this.due = due;
	}

	public List<String> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(List<String> memberIds) {
		this.memberIds = memberIds;
	}

	public List<String> getTrelloMemberIds() {
		return trelloMemberIds;
	}

	public void setTrelloMemberIds(List<String> trelloMemberIds) {
		this.trelloMemberIds = trelloMemberIds;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
