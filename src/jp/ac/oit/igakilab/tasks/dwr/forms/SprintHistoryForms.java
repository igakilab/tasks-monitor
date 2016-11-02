package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.trello.TrelloBoardData;

public class SprintHistoryForms {
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
			List<TrelloCardMembersForm> rtmp = new ArrayList<TrelloCardMembersForm>();
			res.getRemainedCards().forEach(c -> rtmp.add(TrelloCardMembersForm.getInstance(c)));
			List<TrelloCardMembersForm> ftmp = new ArrayList<TrelloCardMembersForm>();
			res.getFinishedCards().forEach(c -> ftmp.add(TrelloCardMembersForm.getInstance(c)));

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
		private List<TrelloCardMembersForm> remainedCards;
		private List<TrelloCardMembersForm> finishedCards;

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
		public List<TrelloCardMembersForm> getRemainedCards() {
			return remainedCards;
		}
		public void setRemainedCards(List<TrelloCardMembersForm> remainedCards) {
			this.remainedCards = remainedCards;
		}
		public List<TrelloCardMembersForm> getFinishedCards() {
			return finishedCards;
		}
		public void setFinishedCards(List<TrelloCardMembersForm> finishedCards) {
			this.finishedCards = finishedCards;
		}
	}
}
