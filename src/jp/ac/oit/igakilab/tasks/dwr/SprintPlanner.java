package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.db.SprintsManageDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.db.converters.SprintDocumentConverter;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.dwr.forms.CardMembersForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.jsmodule.SprintBuilderForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.jsmodule.SprintBuilderForm.SBTrelloCardForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.MemberForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.SprintForm;
import jp.ac.oit.igakilab.tasks.dwr.forms.model.TrelloCardForm;
import jp.ac.oit.igakilab.tasks.hubot.HubotSendMessage;
import jp.ac.oit.igakilab.tasks.members.Member;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.scripts.SprintEditException;
import jp.ac.oit.igakilab.tasks.scripts.SprintEditor;
import jp.ac.oit.igakilab.tasks.scripts.TrelloBoardBuilder;
import jp.ac.oit.igakilab.tasks.sprints.CardMembers;
import jp.ac.oit.igakilab.tasks.sprints.Sprint;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoardData;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloAction;

public class SprintPlanner {
	//ボードに設定された現在のスプリントの情報が返却される
	//ボードやスプリントがない場合はnullが返される
	public SprintForm getCurrentSprint(String boardId){
		//クライアントとdb操作クラスを生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//現在日時から期間内のスプリントを取得
		Sprint sprint = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		//取得できなかった場合はnullを返却
		if( sprint == null ){
			client.close();
			return null;
		}

		//DBをクローズ、formに変換してreturn
		client.close();
		return SprintForm.getInstance(sprint);
	}

	//SprintBuilderのためのデータを取得する
	public SprintBuilderForm getSprintBuilderForm(String boardId)
	throws ExecuteFailedException{
		//dbのクライアント等生成
		MongoClient client = TasksMongoClientBuilder.createClient();

		//ボードの存在確認
		TrelloBoardsDB bdb = new TrelloBoardsDB(client);
		if( !bdb.boardIdExists(boardId) ){
			client.close();
			throw new ExecuteFailedException("ボードが見つかりません");
		}

		//現在進行中のスプリント取得
		SprintsManageDB smdb = new SprintsManageDB(client);
		Sprint sprint = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		//ボードオブジェクトを生成
		TrelloBoardBuilder builder = new TrelloBoardBuilder(client);
		TrelloBoard board = builder.buildTrelloBoardFromTrelloActions(boardId);
		if( board == null ){
			client.close();
			throw new ExecuteFailedException("ボードの生成に失敗しました");
		}


		//対象カードリストを生成
		List<SBTrelloCardForm> fcards = new ArrayList<SBTrelloCardForm>();
		MemberTrelloIdTable ttb = new MemberTrelloIdTable(client);
		Consumer<TrelloCard> collector = (card) -> {
			if( sprint != null && sprint.getTrelloCardIds().contains(card.getId()) ){
				fcards.add(SBTrelloCardForm.getInstance(card, client, ttb));
			}else{
				fcards.add(SBTrelloCardForm.getInstance(card, client, null));
			}
		};
		board.getCardsByListNameMatches(
			TasksTrelloClientBuilder.REGEX_TODO).forEach(collector);
		board.getCardsByListNameMatches(
			TasksTrelloClientBuilder.REGEX_DOING).forEach(collector);

		//メンバーリストを生成
		List<MemberForm> members = new ArrayList<MemberForm>();
		board.getMemberIds().forEach((tmid) -> {
			Member m = ttb.getMember(tmid);
			if( m != null ){
				members.add(MemberForm.getInstance(m));
			}
		});

		//formを生成
		SprintBuilderForm form = SprintBuilderForm.getInstance(sprint, fcards, members);

		client.close();
		return form;
	}

	//スプリントを新しく生成
	//現在進行中のスプリントがあった場合、エラーを投げる
	public String createSprint(String boardId, Date finishDate, List<CardMembersForm> cardsForm)
	throws ExecuteFailedException{
		//DBのクライアントと操作クラスの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintsManageDB smdb = new SprintsManageDB(client);
		HubotSendMessage msg = new HubotSendMessage(AppProperties.global.get("tasks.hubot.url"));
		SprintEditor se = new SprintEditor(client, api, msg);

		//進行中スプリントの取得
		Sprint current = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());
		//進行中スプリントがあった場合、クローズする
		if( current != null ){
			client.close();
			throw new ExecuteFailedException("現在進行中のスプリントがあります");
		}

		//日付の取得
		Date today = Sprint.roundDate(Calendar.getInstance().getTime()).getTime();
		//formをオブジェクトに変換
		List<CardMembers> cards = new ArrayList<CardMembers>();
		if( cardsForm != null ){
			cardsForm.forEach(
				(card -> cards.add(CardMembersForm.convert(card))));
		}
		//DB登録
		String newId = null;
		try{
			newId = se.createSprint(boardId, today, finishDate, cards);
		}catch(SprintEditException e0){
			client.close();
			throw new ExecuteFailedException("スプリント登録に失敗しました: " + e0.getMessage());
		}

		//DBをクローズ、登録されたidを返却
		client.close();
		return newId;
	}

	//スプリントを更新
	// 指定されたスプリントidがない場合はエラーを投げる
	public String updateSprint(String sprintId, Date finishDate, List<CardMembersForm> cardForm)
	throws ExecuteFailedException{
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloApi<Object> api = TasksTrelloClientBuilder.createApiClient();
		SprintsManageDB smdb = new SprintsManageDB(client);
		SprintEditor se = new SprintEditor(client, api, null);

		cardForm.forEach((c -> System.out.format("i:%s, m:%s\n", c.getCardId(), c.getMemberIds())));

		//スプリントデータを取得
		SprintDocumentConverter converter = new SprintDocumentConverter();
		Sprint sprint = smdb.getSprintById(sprintId, converter);

		//スプリントデータを検証
		if( sprint == null ){
			client.close();
			throw new ExecuteFailedException("指定されたスプリントがありません");
		}
		if( sprint.isClosed() ){
			client.close();
			throw new ExecuteFailedException("指定されたスプリントはすでに閉じられています");
		}

		//スプリントデータ更新作業
		List<CardMembers> members = new ArrayList<CardMembers>();
		cardForm.forEach((cmf) -> members.add(CardMembersForm.convert(cmf)));
		try{
			se.updateSprint(sprintId, finishDate, members);
		}catch(SprintEditException e0){
			client.close();
			throw new ExecuteFailedException("登録に失敗しました: " + e0.getMessage());
		}

		client.close();
		return sprint.getId();
	}

	//boardIdより現在進行中のスプリントをクローズする
	//スプリントがない場合や、更新に失敗した場合はfalseを返却する
	public boolean closeCurrentSprint(String boardId){
		//DBのクライアントと操作クラスの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		SprintsManageDB smdb = new SprintsManageDB(client);

		//進行中のスプリントを取得
		Sprint current = smdb.getCurrentSprint(boardId, new SprintDocumentConverter());

		//進行中スプリントがある場合はスプリントをクローズ
		boolean res = false;
		if( current != null ){
			res = smdb.closeSprint(current.getId());
		}

		//DBをクローズ、結果を返却
		client.close();
		return res;
	}

	//ボードにあるtodoのカードリストを返却する
	//ボードやリストがない場合は空のリストが返却される
	public List<TrelloCardForm> getTodoTrelloCards(String boardId){
		//dbのクライアントを生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(client);

		//アクションを取得
		List<TrelloAction> actions =
			adb.getTrelloActions(boardId, new TrelloActionDocumentParser());

		//カードリストの初期化
		List<TrelloCardForm> forms = new ArrayList<TrelloCardForm>();

		//ボードの解析
		if( actions.size() > 0 ){
			//ボードを生成・ビルド
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			board.build();

			//正規表現でマッチするリストのカードを取得
			board.getCardsByListNameMatches(TasksTrelloClientBuilder.REGEX_TODO).forEach(
				(card -> forms.add(TrelloCardForm.getInstance(card, mtable))));
		}

		//結果を返却
		client.close();
		return forms;
	}

	//ボードに参加しているメンバーのID一覧を取得します
	public List<MemberForm> getBoardMembers(String boardId){
		//dbクライアントの生成
		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		MemberTrelloIdTable mtable = new MemberTrelloIdTable(client);

		//アクションを取得
		List<TrelloAction> actions =
			adb.getTrelloActions(boardId, new TrelloActionDocumentParser());

		//メンバーリスト初期化
		List<MemberForm> members = new ArrayList<MemberForm>();

		//ボード解析
		if( actions.size() > 0 ){
			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			TrelloBoardData bdata = board.buildBoardData();

			bdata.getMemberIds().forEach((tmid) -> {
				Member m = mtable.getMember(tmid);
				if( m != null ) members.add(MemberForm.getInstance(m));
			});
		}

		System.out.println(members.toString());

		client.close();
		return members;
	}
}
