package jp.ac.oit.igakilab.tasks.test;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.dwr.ExcuteFailedException;
import jp.ac.oit.igakilab.tasks.dwr.forms.SprintResultAnalyzerForm;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;

public class TestSprintAnalyzerForm {
	public static void main(String[] args)
	throws ExcuteFailedException{
		String sprintId = "1cd1807fffed00";
		MongoClient client = TasksMongoClientBuilder.createClient();

		TasksTrelloClientBuilder.setTestApiKey();

		SprintResultAnalyzerForm form =
			SprintResultAnalyzerForm.buildInstance(client, sprintId);

		form.getMemberHistories().forEach((mh) -> {
			System.out.println(mh.getMemberId());
			mh.getResults().forEach((msr -> System.out.println("\t" + msr.getSprintId())));;
		});

		client.close();
	}
}
