package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.dwr.forms.model.CardResultForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloBoardDataForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintDataContainer;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoardData;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintHistoryForms {
	public static class SprintResultData{
		protected static TrelloCardForm getTrelloCardForm
		(TrelloBoard board, String cardId, List<String> memberIds){
			TrelloCard card = board.getCardById(cardId);

			TrelloCardForm cf = null;
			if( card != null ){
				cf = TrelloCardForm.getInstance(card, memberIds);
			}else{
				cf = new TrelloCardForm();
				cf.setId(cardId);
			}

			return cf;
		}

		public static SprintResultData getInstance(Sprint spr, SprintResult res, TrelloBoard board){
			if( spr == null || res == null || board == null ) return null;

			SprintResultData form = new SprintResultData();
			form.setId(spr.getId());
			form.setBeginDate(spr.getBeginDate());
			form.setFinishDate(spr.getFinishDate());
			form.setClosedDate(spr.getClosedDate());

			List<TrelloCardForm> rtmp = new ArrayList<TrelloCardForm>();
			res.getRemainedCards().forEach(c ->
				rtmp.add(getTrelloCardForm(board, c.getCardId(), c.getMemberIds())));
			form.setRemainedCards(rtmp);
			List<TrelloCardForm> ftmp = new ArrayList<TrelloCardForm>();
			res.getFinishedCards().forEach(c ->
				ftmp.add(getTrelloCardForm(board, c.getCardId(), c.getMemberIds())));
			form.setRemainedCards(ftmp);

			form.setBoardData(TrelloBoardDataForm.getInstance(board));

			return form;
		}


		private String id;

		private Date beginDate;
		private Date finishDate;

		private Date closedDate;
		private List<TrelloCardForm> remainedCards;
		private List<TrelloCardForm> finishedCards;

		private TrelloBoardDataForm boardData;

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Date getBeginDate() {
			return beginDate;
		}
		public void setBeginDate(Date beginDate) {
			this.beginDate = beginDate;
		}
		public Date getFinishDate() {
			return finishDate;
		}
		public void setFinishDate(Date finishDate) {
			this.finishDate = finishDate;
		}
		public Date getClosedDate() {
			return closedDate;
		}
		public void setClosedDate(Date closedDate) {
			this.closedDate = closedDate;
		}
		public List<TrelloCardForm> getRemainedCards() {
			return remainedCards;
		}
		public void setRemainedCards(List<TrelloCardForm> remainedCards) {
			this.remainedCards = remainedCards;
		}
		public List<TrelloCardForm> getFinishedCards() {
			return finishedCards;
		}
		public void setFinishedCards(List<TrelloCardForm> finishedCards) {
			this.finishedCards = finishedCards;
		}
		public TrelloBoardDataForm getBoardData() {
			return boardData;
		}
		public void setBoardData(TrelloBoardDataForm boardData) {
			this.boardData = boardData;
		}
	}

	public static class SprintList{
		public static SprintList getInstance(TrelloBoardData bdata, List<SprintData> sprints){
			SprintList form = new SprintList();
			form.setBoardData(TrelloBoardDataForm.getInstance(bdata));
			form.setSprintList(sprints);
			return form;
		}

		public static SprintList getInstance
		(TrelloBoardData bdata, List<Sprint> slist, List<SprintResult> rlist){
			return getInstance(bdata, SprintData.getInstances(slist, rlist));
		}

		public static SprintList getInstanceByDataContainer
		(TrelloBoardData bdata, List<SprintDataContainer> source){
			List<SprintData> data = new ArrayList<SprintData>();

			for(SprintDataContainer c : source){
				data.add(SprintData.getInstance(c.getSprint(), c.getSprintResult()));
			}

			return getInstance(bdata, data);
		}


		private TrelloBoardDataForm boardData;
		private List<SprintData> sprintList;


		public TrelloBoardDataForm getBoardData() {
			return boardData;
		}

		public void setBoardData(TrelloBoardDataForm boardData) {
			this.boardData = boardData;
		}

		public List<SprintData> getSprintList() {
			return sprintList;
		}
		public void setSprintList(List<SprintData> sprintList) {
			this.sprintList = sprintList;
		}
	}

	public static class SprintData{
		public static SprintData getInstance(Sprint spr, SprintResult res){
			if( spr == null ) return null;
			//インスタンス初期化
			SprintData form = new SprintData();

			//スプリントの情報を格納
			form.setId(spr.getId());
			form.setBoardId(spr.getBoardId());
			form.setBeginDate(spr.getBeginDate());
			form.setFinishDate(spr.getFinishDate());

			if( res == null ) return form;
			//スプリントの結果情報を格納
			form.setClosedDate(spr.getClosedDate());
			List<CardResultForm> rtmp = new ArrayList<CardResultForm>();
			res.getRemainedCards().forEach(c -> rtmp.add(CardResultForm.getInstance(c)));
			form.setRemainedCards(rtmp);
			List<CardResultForm> ftmp = new ArrayList<CardResultForm>();
			res.getFinishedCards().forEach(c -> ftmp.add(CardResultForm.getInstance(c)));
			form.setFinishedCards(ftmp);

			//返却
			return form;
		}

		public static List<SprintData> getInstances(List<Sprint> slist, List<SprintResult> rlist){
			List<SprintData> forms = new ArrayList<SprintData>();

			for(Sprint spr : slist){
				SprintResult res = null;
				for(SprintResult r : rlist){
					if( spr.getId().equals(r.getSprintId()) ){
						res = r;
						break;
					}
				}
				forms.add(getInstance(spr, res));
			}

			return forms;
		}

		private String id;

		private String boardId;
		private Date beginDate;
		private Date finishDate;

		private Date closedDate;
		private List<CardResultForm> remainedCards;
		private List<CardResultForm> finishedCards;

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
			this.beginDate = beginDate;
		}
		public Date getFinishDate() {
			return finishDate;
		}
		public void setFinishDate(Date finishDate) {
			this.finishDate = finishDate;
		}
		public Date getClosedDate() {
			return closedDate;
		}
		public void setClosedDate(Date closedDate) {
			this.closedDate = closedDate;
		}
		public List<CardResultForm> getRemainedCards() {
			return remainedCards;
		}
		public void setRemainedCards(List<CardResultForm> remainedCards) {
			this.remainedCards = remainedCards;
		}
		public List<CardResultForm> getFinishedCards() {
			return finishedCards;
		}
		public void setFinishedCards(List<CardResultForm> finishedCards) {
			this.finishedCards = finishedCards;
		}
	}
}
