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
	private Date closedDate;
	private List<TrelloCardMembers> trelloCards;

	public Sprint(){
		id = null;
		boardId = null;
		beginDate = null;
		finishDate = null;
		closedDate = null;
		trelloCards = new ArrayList<TrelloCardMembers>();
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
		return closedDate != null;
	}

	public Date getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Date closedDate) {
		this.closedDate = (closedDate == null ?
				null : roundDate(closedDate).getTime());
	}

	public List<TrelloCardMembers> getTrelloCards(){
		return trelloCards;
	}

	public void clearTrelloCards(){
		trelloCards.clear();
	}

	public void addTrelloCard(TrelloCardMembers cm){
		for(int i=0; i<trelloCards.size(); i++){
			if( trelloCards.get(i).getCardId().equals(cm.getCardId()) ){
				trelloCards.set(i, cm);
				return;
			}
		}
		trelloCards.add(cm);
	}
}
