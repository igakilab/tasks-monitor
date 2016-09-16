package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.Date;
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

	public static int parseTargetType(String type){
		if( type == null ) return TARGET_UNKNOWN;
		if( type.equals("createCard") ||
			type.equals("updateCard") ||
			type.equals("addMemberToCard") ||
			type.equals("removeMemberFromCard") ||
			type.equals("deleteCard") ){
			return TARGET_CARD;
		}else
		if( type.equals("createList") ||
			type.equals("updateList") ){
			return TARGET_LIST;
		}else
		if( type.equals("createBoard") ||
			type.equals("updateBoard") ||
			type.equals("addMemberToBoard") ||
			type.equals("removeMemberFromBoard") ){
			return TARGET_BOARD;
		}
		return TARGET_UNKNOWN;
	}

	public static int parseActionType(String type){
		if( type == null ) return ACTION_UNKNOWN;
		if( type.equals("createCard") ||
			type.equals("createList") ||
			type.equals("createBoard") ){
			return ACTION_CREATE;
		}else
		if( type.equals("updateCard") ||
			type.equals("updateList") ||
			type.equals("updateBoard") ){
			return ACTION_UPDATE;
		}else
		if( type.equals("deleteCard") ){
			return ACTION_DELETE;
		}else
		if( type.equals("addMemberToCard") ||
			type.equals("addMemberToBoard") ){
			return ACTION_ADDMEMBER;
		}else
		if( type.equals("removeMemberFromCard") ||
			type.equals("removeMemberFromBoard") ){
			return ACTION_REMOVEMEMBER;
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
