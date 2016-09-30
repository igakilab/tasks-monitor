package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Sprint {
	public static Calendar roundDate(Date d0){
		if( d0 == null ) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d0);
		Calendar ncal = Calendar.getInstance();
		ncal.clear();
		ncal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		ncal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		ncal.set(Calendar.DATE, cal.get(Calendar.DATE));
		return ncal;
	}

	private String id;
	private String boardId;
	private Date beginDate;
	private Date finishDate;
	private boolean isClosed;
	private Date closedDate;
	private List<String> trelloCardIds;

	public Sprint(){
		id = null;
		boardId = null;
		beginDate = null;
		finishDate = null;
		isClosed = false;
		closedDate = null;
		trelloCardIds = new ArrayList<String>();
	}

	public Sprint(String id){
		this();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = (beginDate == null ?
			null : roundDate(beginDate).getTime());
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = (finishDate == null ?
				null : roundDate(finishDate).getTime());
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public Date getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Date closedDate) {
		this.closedDate = (closedDate == null ?
				null : roundDate(closedDate).getTime());
	}

	public List<String> getTrelloCardIds(){
		return trelloCardIds;
	}

	public void clearTrelloCardId(){
		trelloCardIds.clear();
	}

	public void addTrelloCardId(String cardId){
		if( !trelloCardIds.contains(cardId) ){
			trelloCardIds.add(cardId);
		}
	}
}
