package jp.ac.oit.igakilab.tasks.dwr.forms.jsmodule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.MemberForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TagCountForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.sprints.CardTagsAggregator.TagCount;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;

public class SprintBuilderForm {
	public static class SBTrelloCardForm extends TrelloCardForm{
		public static SBTrelloCardForm getInstance
		(TrelloCard card, MongoClient dbclient, MemberTrelloIdTable ttb){
			//インスタンス初期化
			SBTrelloCardForm form = new SBTrelloCardForm();
			setValues(form, card, ttb);

			//作成時間の設定
			if( card instanceof TrelloActionsCard ){
				form.setCreatedAt(((TrelloActionsCard)card).getCreatedAt());
			}

			//未達成回数の表示
			if( dbclient != null ){
				SprintResultsDB srdb = new SprintResultsDB(dbclient);
				form.setRemainedTimes(srdb.countCardRemainedTimes(card.getId()));
			}

			return form;
		}

		private Date createdAt;
		private int remainedTimes;

		public SBTrelloCardForm(){
			super();
			createdAt = null;
			remainedTimes = 0;
		}

		public Date getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
		}

		public int getRemainedTimes() {
			return remainedTimes;
		}

		public void setRemainedTimes(int remainedTimes) {
			this.remainedTimes = remainedTimes;
		}
	}


	public static class TagsMemberForm extends MemberForm{
		public static TagsMemberForm getInstance(Member member, List<TagCount> tags){
			TagsMemberForm form = new TagsMemberForm();
			form.setId(member.getId());
			if( member.getName() != null )
				form.setName(member.getName());
			if( member.getTrelloId() != null )
				form.setTrelloId(member.getTrelloId());
			if( member.getSlackId() != null )
				form.setSlackId(member.getSlackId());
			form.setAdmin(member.isAdmin());

			form.setTagCount(tags);

			return form;
		}

		private List<TagCountForm> tags;

		public List<TagCountForm> getTags() {
			return tags;
		}

		public void setTags(List<TagCountForm> tags) {
			this.tags = tags;
		}

		public void addTag(TagCountForm tcf){
			if( tags == null ) tags = new ArrayList<>();
			tags.add(tcf);
		}

		public void setTagCount(List<TagCount> tags){
			if( tags != null ){
				this.tags  = new ArrayList<>();
				tags.forEach(tc -> addTag(TagCountForm.getInstance(tc)));
			}
		}
	}


	public static SprintBuilderForm getInstance
	(Sprint currentSprint, List<SBTrelloCardForm> trelloCards, List<TagsMemberForm> members){
		SprintBuilderForm form = new SprintBuilderForm();

		//進行中スプリントがある場合はフォームに指定
		if( currentSprint != null ){
			form.setCurrentSprint(SprintForm.getInstance(currentSprint));
		}

		//対象カードを指定する
		form.getCards().addAll(trelloCards);

		//ボードに所属するメンバー一覧を設定する
		form.getMembers().addAll(members);

		return form;
	}


	private SprintForm currentSprint;
	private List<SBTrelloCardForm> cards;
	private List<TagsMemberForm> members;

	public SprintBuilderForm(){
		currentSprint = null;
		cards = new ArrayList<SBTrelloCardForm>();
		setMembers(new ArrayList<TagsMemberForm>());
	}

	public SprintForm getCurrentSprint() {
		return currentSprint;
	}

	public void setCurrentSprint(SprintForm currentSprint) {
		this.currentSprint = currentSprint;
	}

	public List<SBTrelloCardForm> getCards() {
		return cards;
	}

	public void setCards(List<SBTrelloCardForm> cards) {
		this.cards = cards;
	}

	public List<TagsMemberForm> getMembers() {
		return members;
	}

	public void setMembers(List<TagsMemberForm> members) {
		this.members = members;
	}
}
