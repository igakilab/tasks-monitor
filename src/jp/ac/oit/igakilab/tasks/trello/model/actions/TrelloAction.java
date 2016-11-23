package jp.ac.oit.igakilab.tasks.trello.model.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

public class TrelloAction {
	public static final int TARGET_BOARD = 101;
	public static final int TARGET_LIST = 102;
	public static final int TARGET_CARD = 103;
	public static final int TARGET_UNKNOWN = 191;
	public static final int ACTION_CREATE = 201;
	public static final int ACTION_UPDATE = 202;
	public static final int ACTION_DELETE = 203;
	public static final int ACTION_ADDMEMBER = 211;
	public static final int ACTION_REMOVEMEMBER = 212;
	public static final int ACTION_UNKNOWN = 291;

	static class Type{
		private int code;
		private List<String> labels;

		Type(int c0, List<String> l0){
			code = c0;
			labels = l0;
		}

		int getCode(){ return code; }
		List<String> getLabels(){ return labels; }

		boolean labelContains(String l0){
			return labels.contains(l0);
		}
	}

	public static Type[] targetTypes = {
		new Type(TARGET_CARD, Arrays.asList(
			"createCard", "updateCard", "addMemberToCard", "removeMemberToCard", "deleteCard")),
		new Type(TARGET_LIST, Arrays.asList(
			"createList", "updateList")),
		new Type(TARGET_BOARD, Arrays.asList(
			"createBoard", "updateBoard", "addMemberToBoard", "removeMemberFromBoard"))
	};

	public static List<String> getTargetTypeLabels(int code){
		for(Type tobj : targetTypes){
			if( tobj.getCode() == code ){
				return tobj.getLabels();
			}
		}
		return new ArrayList<String>();
	}

	public static int parseTargetType(String type){
		if( type == null ) return TARGET_UNKNOWN;

		for(Type tobj : targetTypes){
			if( tobj.labelContains(type) ){
				return tobj.getCode();
			}
		}

		return TARGET_UNKNOWN;
	}

	public static Type[] actionTypes = {
		new Type(ACTION_CREATE, Arrays.asList(
			"createCard", "createList", "createBoard")),
		new Type(ACTION_UPDATE, Arrays.asList(
			"updateCard", "updateList", "updateBoard")),
		new Type(ACTION_DELETE, Arrays.asList(
			"deleteCard")),
		new Type(ACTION_ADDMEMBER, Arrays.asList(
			"addMemberToCard", "addMemberToBoard")),
		new Type(ACTION_REMOVEMEMBER, Arrays.asList(
			"removeMemberFromCard", "removeMemberFromBoard"))
	};

	public static List<String> getActionTypeLabels(int code){
		for(Type tobj : actionTypes){
			if( tobj.getCode() == code ){
				return tobj.getLabels();
			}
		}
		return new ArrayList<String>();
	}


	public static int parseActionType(String type){
		if( type == null ) return ACTION_UNKNOWN;

		for(Type tobj : actionTypes){
			if( tobj.labelContains(type) ){
				return tobj.getCode();
			}
		}

		return ACTION_UNKNOWN;
	}


	protected String id;
	protected String type;
	protected int targetType;
	protected int actionType;
	protected Date date;
	protected TrelloActionData data;
	protected String memberCreatorId;
	protected String rawText;

	public TrelloAction(){
		init();
	}

	public void init(){
		id = null;
		type = null;
		targetType = TARGET_UNKNOWN;
		actionType = ACTION_UNKNOWN;
		date = null;
		data = null;
		memberCreatorId = null;
		rawText = null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTargetType() {
		return (targetType == TARGET_UNKNOWN ) ?
			parseTargetType(getType()) : targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getActionType() {
		return (actionType == ACTION_UNKNOWN ) ?
			parseActionType(getType()) : actionType;
	}

	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public TrelloActionData getData() {
		return data;
	}

	public void setData(TrelloActionData data) {
		this.data = data;
	}

	public String getMemberCreatorId() {
		return memberCreatorId;
	}

	public void setMemberCreatorId(String memberCreatorId) {
		this.memberCreatorId = memberCreatorId;
	}

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public String dataString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(id).append('\n');
		buffer.append("date: ").append(date).append('\n');
		buffer.append("memberCreatorId: ").append(memberCreatorId).append('\n');

		String target = (
			targetType == TARGET_BOARD ? "BOARD" :
			targetType == TARGET_LIST ? "LIST" :
			targetType == TARGET_CARD ? "CARD" : "UNKNOWN" );
		String action = (
			actionType == ACTION_CREATE ? "CREATE" :
			actionType == ACTION_UPDATE ? "UPDATE" :
			actionType == ACTION_DELETE ? "DELETE" :
			actionType == ACTION_ADDMEMBER ? "ADDMEMBER" :
			actionType == ACTION_REMOVEMEMBER ? "REMOVEMEMBER" : "UNKNOWN" );
		buffer.append(target).append(' ').append(action).append("(" + type + ")").append('\n');

		for(Entry<String,String> entry : data.entrySet()){
			buffer.append(String.format("\t%s: %s\n", entry.getKey(), entry.getValue()));
		}

		return buffer.toString();
	}
}
