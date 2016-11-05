package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintResultAnalyzerForm {
	public static SprintResultAnalyzerForm getInstance
	(TrelloBoard board, Sprint sprint, SprintResult result, List<Member> members){
		SprintResultAnalyzerForm form = new SprintResultAnalyzerForm();

		//ボードデータの設定
		form.setBoardData(TrelloBoardDataForm.getInstance(board));

		//スプリントデータの設定
		form.setSprint(SprintForm.getInstance(sprint));

		//スプリントリザルトデータの設定
		form.setResult(SprintResultForm.getInstance(result));

		//スプリント対象カードの設定
		List<TrelloCardForm> tmp = new ArrayList<TrelloCardForm>();
		Consumer<TrelloCardMembers> collector = (mc) -> {
			TrelloCard card = board.getCardById(mc.getCardId());
			if( card != null ){
				tmp.add(TrelloCardForm.getInstance(card));
			}
		};
		result.getFinishedCards().forEach(collector);
		result.getRemainedCards().forEach(collector);
		form.setSprintCards(tmp);

		//メンバーリスト設定
		form.setMembers(new ArrayList<MemberForm>());
		members.forEach((member ->
			form.getMembers().add(MemberForm.getInstance(member))));

		return form;
	}


	private TrelloBoardDataForm boardData;
	private SprintForm sprint;
	private SprintResultForm result;
	private List<TrelloCardForm> sprintCards;
	private List<MemberForm> members;

	public TrelloBoardDataForm getBoardData() {
		return boardData;
	}
	public void setBoardData(TrelloBoardDataForm boardData) {
		this.boardData = boardData;
	}
	public SprintForm getSprint() {
		return sprint;
	}
	public void setSprint(SprintForm sprint) {
		this.sprint = sprint;
	}
	public SprintResultForm getResult() {
		return result;
	}
	public void setResult(SprintResultForm result) {
		this.result = result;
	}
	public List<TrelloCardForm> getSprintCards() {
		return sprintCards;
	}
	public void setSprintCards(List<TrelloCardForm> sprintCards) {
		this.sprintCards = sprintCards;
	}
	public List<MemberForm> getMembers() {
		return members;
	}
	public void setMembers(List<MemberForm> members) {
		this.members = members;
	}
}
