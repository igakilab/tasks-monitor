package jp.ac.oit.igakilab.tasks.test;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;

public class TestSprintRenewal {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();

		SprintResultsDB srdb = new SprintResultsDB(client);
		SprintResultDocumentConverter srdc = new SprintResultDocumentConverter(srdb);

		SprintResult res = srdb.getSprintResultBySprintId("04a040435eb034", srdc);
		System.out.println(res);
		client.close();
	}
}
