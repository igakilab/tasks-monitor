package jp.ac.oit.igakilab.tasks.test;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardActionsDB;
import jp.ac.oit.igakilab.tasks.db.converters.TrelloActionDocumentParser;
import jp.ac.oit.igakilab.tasks.hubot.ChannelNotification;
import jp.ac.oit.igakilab.tasks.hubot.HubotTaskNotify;
import jp.ac.oit.igakilab.tasks.hubot.NotifyTrelloCard;
import jp.ac.oit.igakilab.tasks.members.MemberSlackIdTable;
import jp.ac.oit.igakilab.tasks.members.MemberTrelloIdTable;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloActionsBoard;

public class TestChannelNotification {
	public static void main(String[] args){
		String boardId = "57ab33677fd33ec535cc4f28";

		MongoClient client = TasksMongoClientBuilder.createClient();
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);
		TrelloActionsBoard board = new TrelloActionsBoard();
		board.addActions(adb.getTrelloActions(boardId, new TrelloActionDocumentParser()));
		board.build();

		HubotTaskNotify msg = new HubotTaskNotify("http://igakilabot.herokuapp.com");
		MemberSlackIdTable stb = new MemberSlackIdTable(client);
		MemberTrelloIdTable mtb = new MemberTrelloIdTable(client);
		ChannelNotification notifer = new ChannelNotification(msg);
		notifer.setTestMode(false);
		notifer.setSlackIdTable(stb);
		List<NotifyTrelloCard> cards = new ArrayList<NotifyTrelloCard>();
		board.getCardsByListNameMatches(TasksTrelloClientBuilder.REGEX_TODO).forEach((c) ->
			cards.add(NotifyTrelloCard.getInstance(c, board, mtb)));
		board.getCardsByListNameMatches(TasksTrelloClientBuilder.REGEX_DOING).forEach((c) ->
			cards.add(NotifyTrelloCard.getInstance(c, board, mtb)));

		System.out.println(cards.size() + " card(s)");
		notifer.taskNotification("koike", "test", cards);

		client.close();
	}
}
