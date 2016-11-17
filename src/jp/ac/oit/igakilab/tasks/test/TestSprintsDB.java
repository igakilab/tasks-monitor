package jp.ac.oit.igakilab.tasks.test;

import java.util.List;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.db.converters.DocumentParser;
import jp.ac.oit.igakilab.tasks.db.converters.SprintResultDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;

public class TestSprintsDB {
	public static void main(String[] args){
		MongoClient client = /*TasksMongoClientBuilder.createClient();*/ new MongoClient("150.89.234.253");
		SprintResultsDB srdb = new SprintResultsDB(client);

		DocumentParser<SprintResult> parser = new SprintResultDocumentConverter();

		List<SprintResult> result = srdb.getSprintResultsByCardMemberId("koike", parser);

		result.forEach((res) -> {
			System.out.format("ID:%s\n", res.getSprintId());
			res.getCardsByMemberIdContains("koike").forEach((sc) ->
				System.out.format("\t%s: %s\n", sc.getCardId(), sc.getMemberIds().toString()));
		});

		client.close();
	}
}
