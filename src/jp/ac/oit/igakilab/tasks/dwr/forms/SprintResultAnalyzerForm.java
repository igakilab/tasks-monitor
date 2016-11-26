package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.SprintsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.dwr.ExcuteFailedException;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.sprints.CardResult;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;
import jp.ac.oit.igakilab.tasks.sprints.TrelloCardMembers;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintResultAnalyzerForm {
	public static int RESULT_HISTORY_CNT = 5;

	public static class CardIdAndFinished{
		private String id;
		private boolean finished;

		public CardIdAndFinished(){
			id = null;
			finished = false;
		}

		public CardIdAndFinished(String id, boolean f){
			this.id = id;
			this.finished = f;
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public boolean isFinished() {
			return finished;
		}
		public void setFinished(boolean finished) {
			this.finished = finished;
		}
	}

	public static class MemberSprintResult{
		public static MemberSprintResult getInstance
		(String sprintId, List<CardResult> cards){
			MemberSprintResult res = new MemberSprintResult();

			//スプリントid設定
			res.setSprintId(sprintId);

			//カードをカウント
			int rem = 0;
			int fin = 0;
			for(CardResult cr : cards){
				res.cards.add(new CardIdAndFinished(cr.getCardId(), cr.isFinished()));
				if( cr.isFinished() ){
					fin++;
				}else{
					rem++;
				}
			}
			res.setRemainedCount(rem);
			res.setFinishedCount(fin);

			return res;
		}

		private String sprintId;
		private int remainedCount;
		private int finishedCount;
		private List<CardIdAndFinished> cards;

		public MemberSprintResult(){
			sprintId = null;
			remainedCount = 0;
			finishedCount = 0;
			cards = new ArrayList<>();
		}

		public String getSprintId() {
			return sprintId;
		}

		public void setSprintId(String sprintId) {
			this.sprintId = sprintId;
		}

		public int getRemainedCount() {
			return remainedCount;
		}

		public void setRemainedCount(int remainedCount) {
			this.remainedCount = remainedCount;
		}

		public int getFinishedCount() {
			return finishedCount;
		}

		public void setFinishedCount(int finishedCount) {
			this.finishedCount = finishedCount;
		}

		public List<CardIdAndFinished> getCards() {
			return cards;
		}

		public void setCards(List<CardIdAndFinished> cards) {
			this.cards = cards;
		}
	}

	public static class MemberHistory{
		private String memberId;
		private List<MemberSprintResult> results;

		public MemberHistory(){
			memberId = null;
			results = new ArrayList<>();
		}

		public MemberHistory(String mid){
			this();
			memberId = mid;
		}

		public String getMemberId() {
			return memberId;
		}

		public void setMemberId(String memberId) {
			this.memberId = memberId;
		}

		public List<MemberSprintResult> getResults() {
			return results;
		}

		public void setResults(List<MemberSprintResult> results) {
			this.results = results;
		}

		public void applySprintResult(SprintResult res){
			List<CardResult> cards = res.getCardsByMemberIdContains(memberId);
			results.add(MemberSprintResult.getInstance(res.getSprintId(), cards));
		}
	}


	@Deprecated
	public static SprintResultAnalyzerForm getInstance
	(TrelloActionsBoard board, Sprint sprint, SprintResult result, List<Member> members){
		SprintResultAnalyzerForm form = new SprintResultAnalyzerForm();

		//ボードデータの設定
		form.setBoardData(TrelloBoardDataForm.getInstance(board));

		//スプリントデータの設定
		form.setSprint(SprintForm.getInstance(sprint));

		//スプリントリザルトデータの設定
		form.setResult(SprintResultForm.getInstance(result));

		//スプリント対象カードの設定
		Calendar end = Calendar.getInstance();
		end.setTime(sprint.getFinishDate());
		end.add(Calendar.DATE, 1);
		List<AnalyzedTrelloCardForm> tmp = new ArrayList<AnalyzedTrelloCardForm>();
		Consumer<TrelloCardMembers> collector = (mc) -> {
			TrelloCard ctmp = board.getCardById(mc.getCardId());
			TrelloActionsCard card = (ctmp instanceof TrelloActionsCard) ? (TrelloActionsCard)ctmp : null;
			if( card != null ){
				tmp.add(AnalyzedTrelloCardForm
					.getInstance(card, board, sprint.getBeginDate(), end.getTime(), null));
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

	public static SprintResultAnalyzerForm buildInstance
	(MongoClient client, String sprintId)
	throws ExcuteFailedException{
		SprintResultAnalyzerForm form = new SprintResultAnalyzerForm();

		//スプリントデータの取得
		SprintsDB sdb = new SprintsDB(client);
		SprintDocumentConverter sdc = new SprintDocumentConverter();
		Sprint sprint = sdb.getSprintById(sprintId, sdc);
		if( sprint == null ){
			throw new ExcuteFailedException("対象スプリントが見つかりません");
		}
		if( !sprint.isClosed() ){
			throw new ExcuteFailedException("このスプリントは閉じられていません");
		}

		//ボードデータの設定
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		TrelloActionsBoard board =
			builder.buildTrelloActionsBoardFromTrelloActions(sprint.getBoardId());
		form.setBoardData(TrelloBoardDataForm.getInstance(board));

		//スプリントデータの設定
		form.setSprint(SprintForm.getInstance(sprint));

		//スプリントリザルトデータの取得
		SprintResultsDB srdb = new SprintResultsDB(client);
		List<String> sprintIds = new ArrayList<String>();
		sdb.getLatestFinishedSprintByBoardId(
			sprint.getBoardId(), sprint.getId(), RESULT_HISTORY_CNT, sdc).forEach(
				(s -> sprintIds.add(s.getId()))
			);
		System.out.println(sprintIds);
		List<SprintResult> sprintResults =
			srdb.getSprintResultsBySprintIds(sprintIds, new SprintResultDocumentConverter());
		sprintResults.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

		//スプリントリザルトデータの設定
		SprintResult result = sprintResults.get(0);
		form.setResult(SprintResultForm.getInstance(result));

		//スプリント対象カードの取得
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(client);
		Calendar end = Calendar.getInstance();
		end.setTime(sprint.getFinishDate());
		end.add(Calendar.DATE, 1);
		List<AnalyzedTrelloCardForm> cards = new ArrayList<AnalyzedTrelloCardForm>();
		result.getAllCards().forEach((cr) -> {
			TrelloCard tmp0 = board.getCardById(cr.getCardId());
			TrelloActionsCard card = (tmp0 instanceof TrelloActionsCard) ? (TrelloActionsCard)tmp0 : null;
			if( card != null ){
				cards.add(AnalyzedTrelloCardForm
					.getInstance(card, board, sprint.getBeginDate(), end.getTime(), ttb));
			}
		});
		form.setSprintCards(cards);

		//メンバーリスト設定
		List<MemberForm> members = new ArrayList<>();
		board.getMemberIds().forEach((tmid) -> {
			Member m = ttb.getMember(tmid);
			if( m != null ) members.add(MemberForm.getInstance(m));
		});
		form.setMembers(members);

		//メンバーヒストリー設定
		List<MemberHistory> histories = new ArrayList<>();
		for(MemberForm m : form.getMembers()){
			MemberHistory mh = new MemberHistory(m.getId());
			sprintResults.forEach((res -> mh.applySprintResult(res)));
			histories.add(mh);
		}
		form.setMemberHistories(histories);

		return form;
	}


	private TrelloBoardDataForm boardData;
	private SprintForm sprint;
	private SprintResultForm result;
	private List<AnalyzedTrelloCardForm> sprintCards;
	private List<MemberForm> members;
	private List<MemberHistory> memberHistories;

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
	public List<AnalyzedTrelloCardForm> getSprintCards() {
		return sprintCards;
	}
	public void setSprintCards(List<AnalyzedTrelloCardForm> sprintCards) {
		this.sprintCards = sprintCards;
	}
	public List<MemberForm> getMembers() {
		return members;
	}
	public void setMembers(List<MemberForm> members) {
		this.members = members;
	}
	public List<MemberHistory> getMemberHistories() {
		return memberHistories;
	}
	public void setMemberHistories(List<MemberHistory> memberHistories) {
		this.memberHistories = memberHistories;
	}
}
