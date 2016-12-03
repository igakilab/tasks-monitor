package jp.ac.oit.igakilab.tasks.scripts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;

import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;

public class SprintResultsDBOptimizer {
	public static void main(String[] args){
		MongoClient client = new MongoClient();

		client.close();
	}


	public static void renameBackupCollection(MongoCollection<Document> col){
		DateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
		MongoNamespace oldNamespace = col.getNamespace();
		String newName = oldNamespace.getCollectionName() +
			"_backup_" + df.format(Calendar.getInstance().getTime());
		System.out.println("renamed to: " + newName);
		col.renameCollection(new MongoNamespace(oldNamespace.getDatabaseName(), newName));
	}


	private MongoClient client;
	private TrelloApi<Object> trelloApi;
	public boolean printVerbose;

	/**
	 * コンストラクタです
	 * @param client
	 * @param trelloApi
	 */
	public SprintResultsDBOptimizer(MongoClient client, TrelloApi<Object> trelloApi){
		this.client = client;
		this.trelloApi = trelloApi;
	}


	/**
	 * 最適化作業を行うメソッドです。
	 * @param dbName
	 * @param colName
	 * @return
	 */
	public int optimize(String dbName, String colName){
		return 0;
		/*
		 * 旧コレクションの取得とリネーム
		 */

	}
}
