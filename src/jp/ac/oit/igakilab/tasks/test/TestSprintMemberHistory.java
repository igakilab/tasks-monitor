package jp.ac.oit.igakilab.tasks.test;

import jp.ac.oit.igakilab.tasks.dwr.ExcuteFailedException;
import jp.ac.oit.igakilab.tasks.dwr.SprintMemberHistory;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;

public class TestSprintMemberHistory {
	public static void main(String[] args)
	throws ExcuteFailedException{
		TasksTrelloClientBuilder.setTestApiKey();
		SprintMemberHistory serv = new SprintMemberHistory();

		serv.getTaskCardsByMemberId("koike").forEach((c) ->
			System.out.format("%s: (%s) %s\n", c.getId(), c.getSprintId(), c.getAssignedMemberIds()));
	}
}
