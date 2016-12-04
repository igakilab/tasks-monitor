package jp.ac.oit.igakilab.tasks.scripts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.json.simple.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import jp.ac.oit.igakilab.tasks.db.converters.SprintResultCardDocumentConverter;
import jp.ac.oit.igakilab.tasks.sprints.SprintResultCard;
import jp.ac.oit.igakilab.tasks.trello.TasksTrelloClientBuilder;
import jp.ac.oit.igakilab.tasks.trello.TrelloCardFetcher;
import jp.ac.oit.igakilab.tasks.trello.api.SimpleJsonResponseTextParser;
import jp.ac.oit.igakilab.tasks.trello.api.TrelloApi;
import jp.ac.oit.igakilab.tasks.trello.model.actions.TrelloActionRawData;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class SprintResultsDBOptimizer {
	public static String TARGET_DB_HOST = "localhost";
	public static int TARGET_DB_PORT = 27017;

	public static String TARGET_DBNAME = "tasks-monitor";
	public static String TARGET_COLNAME = "sprint_results";

	public static String TRELLO_API_KEY = TasksTrelloClientBuilder.TEST_API_KEY;
	public static String TRELLO_API_TOKEN = TasksTrelloClientBuilder.TEST_API_TOKEN;

	public static void main(String[] args){
		MongoClient client = new MongoClient(TARGET_DB_HOST, TARGET_DB_PORT);
		TrelloApi<Object> api =
			new TrelloApi<>(TRELLO_API_KEY, TRELLO_API_TOKEN, new SimpleJsonResponseTextParser());
		SprintResultsDBOptimizer optimizer = new SprintResultsDBOptimizer(client, api);

		int count = optimizer.optimize(TARGET_DBNAME, TARGET_COLNAME);

		System.out.println("replaced " + count + "record(s)");
		client.close();
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
		this.printVerbose = true;
	}


	private void verbose(String msg){
		if( printVerbose ) System.out.println(msg);
	}


	public String renameBackupCollection(MongoCollection<Document> col){
		DateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
		MongoNamespace oldNamespace = col.getNamespace();

		if( col.count() <= 0 ){
			System.err.println("collection [" + oldNamespace.getFullName() + "] is empty");
			return null;
		}

		String newName = oldNamespace.getCollectionName() +
			"_backup_" + df.format(Calendar.getInstance().getTime());
		verbose("renamed to [" + newName + "] from [" + oldNamespace.getCollectionName() + "]");

		col.renameCollection(new MongoNamespace(oldNamespace.getDatabaseName(), newName));
		return newName;
	}


	/**
	 * 最適化作業を行うメソッドです。
	 * @param dbName
	 * @param colName
	 * @return
	 */
	public int optimize(String dbName, String colName){
		verbose("TARGET_DB: " + dbName);
		verbose("TARGET_COLLECTION: " + colName);
		MongoDatabase db = client.getDatabase(dbName);

		/*
		 * 旧コレクションの取得とリネーム
		 */
		verbose("--- BACKUP SOURCE COLLECTION");
		String sourceName = renameBackupCollection(db.getCollection(colName));
		if( sourceName == null ){
			System.err.println("<ERROR> COLLECTION BACKUP ERROR");
			return -1;
		}

		MongoCollection<Document> source = db.getCollection(sourceName);
		verbose("attached source collection [" + source.getNamespace().getFullName() + "]");
		MongoCollection<Document> dest = db.getCollection(colName);
		verbose("attached dest collection [" + dest.getNamespace().getFullName() + "]");


		/*
		 * データ変換クラスを生成
		 */
		Consumer<String> logger = (msg -> verbose(msg));
		DataOptimizer[] optimizers = {
			new SprintResultMetaDataOptimizer(),
			new SprintResultCardDataOptimizer(),
			new OldSprintResultDataOptimizer(new TrelloCardFetcher(trelloApi), logger)
		};
		CollectionWrapper wrapper = new CollectionWrapper(){
			boolean testMode = true;
			@Override
			public void insert(Document doc){
				verbose("insert: " + doc.toJson());
				if( !testMode )dest.insertOne(doc);
			}
		};

		/*
		 * 変換処理
		 */
		FindIterable<Document> sourceCursor = source.find();
		int cnt = 0;

		for(Document doc : sourceCursor){
			for(DataOptimizer optimizer : optimizers){
				verbose("source: " + doc.toJson());
				if( optimizer.isTargetData(doc) ){
					verbose("replaced by " + optimizer.getClass().getSimpleName());
					boolean res = optimizer.optimize(wrapper, doc);
					if( !res ){
						System.err.println("<ERROR> data optimize error");
						System.err.println("optimizer: " + optimizer.toString());
						System.err.println("data: " + doc.toJson());
					}else{
						cnt++;
					}
				}
			}
		}

		return cnt;
	}


	/**
	 * データ最適化クラス抽象型
	 * @author taka
	 *
	 */
	public static interface DataOptimizer{
		public boolean isTargetData(Document doc);
		public boolean optimize(CollectionWrapper dest, Document doc);
	}

	/**
	 * データ最適化クラスにコレクションを渡すパラメータクラス
	 */
	public static interface CollectionWrapper{
		public void insert(Document doc);
	}

	/**
	 * (最新)スプリントメタデータの最適化クラス
	 * @author taka
	 *
	 */
	public static class SprintResultMetaDataOptimizer implements DataOptimizer{
		@Override
		public boolean isTargetData(Document doc){
			return doc.containsKey("sprintId") && doc.containsKey("createdAt") &&
				!doc.containsKey("remainedCards") && !doc.containsKey("fninishedCards") &&
				!doc.containsKey("sprintCards");
		}
		@Override
		public boolean optimize(CollectionWrapper dest, Document doc){
			dest.insert(doc);
			return true;
		}
	}


	/**
	 * (最新)スプリントカードデータの最適化クラス
	 * @author taka
	 *
	 */
	public static class SprintResultCardDataOptimizer implements DataOptimizer{
		@Override
		public boolean isTargetData(Document doc){
			return doc.containsKey("sprintId") && doc.containsKey("cardId");
		}
		@Override
		public boolean optimize(CollectionWrapper dest, Document doc){
			dest.insert(doc);
			return true;
		}
	}


	/**
	 * (要変換)古いスプリントデータの最適化クラス
	 */
	public static class OldSprintResultDataOptimizer implements DataOptimizer{
		private Consumer<String> logger;
		private TrelloCardFetcher fetcher;

		public OldSprintResultDataOptimizer(TrelloCardFetcher fetcher){
			this.fetcher = fetcher;
			this.logger = null;
		}

		public OldSprintResultDataOptimizer(TrelloCardFetcher fetcher, Consumer<String> logger){
			this(fetcher);
			this.logger = logger;
		}

		public void log(String msg){
			if( logger != null ) logger.accept(msg);
		}

		@Override
		public boolean isTargetData(Document doc){
			return doc.containsKey("sprintId") && doc.containsKey("createdAt") && (
					doc.containsKey("remainedCards") || doc.containsKey("finishedCards") ||
					doc.containsKey("sprintCards")
				);
		}

		@Override
		public boolean optimize(CollectionWrapper dest, Document doc){
			//メタデータ生成
			DocumentValuePicker picker = new DocumentValuePicker(doc);
			String sprintId = picker.getString("sprintId", null);
			Document meta = new Document("sprintId", sprintId);
			Date createdAt = picker.getDate("createdAt", null);
			meta.append("createdAt", createdAt);
			if( sprintId == null || createdAt == null ) return false;

			//カードデータ生成
			List<SprintResultCard> cards = new ArrayList<SprintResultCard>();
			picker.getDocumentArray("remainedCards").forEach((doc0) -> {
				SprintResultCard rc = convertSprintResultCard(sprintId, doc0, createdAt, false);
				if( rc != null ) cards.add(rc);
			});
			picker.getDocumentArray("finishedCards").forEach((doc0) -> {
				SprintResultCard rc = convertSprintResultCard(sprintId, doc0, createdAt, true);
				if( rc != null ) cards.add(rc);
			});
			picker.getDocumentArray("sprintCards").forEach((doc0) -> {
				SprintResultCard rc = convertSprintResultCard(sprintId, doc0, createdAt, false);
				if( rc != null ) cards.add(rc);
			});

			//メタデータの登録
			dest.insert(meta);

			//カードデータの登録
			SprintResultCardDocumentConverter converter =
				new SprintResultCardDocumentConverter();
			for(SprintResultCard rc : cards){
				Document cdoc = converter.convert(rc);
				if( cdoc != null ) dest.insert(cdoc);
			}

			return true;
		}

		public SprintResultCard convertSprintResultCard
		(String sprintId, Document doc, Date before, boolean defaultFinished){
			DocumentValuePicker picker = new DocumentValuePicker(doc);

			String cardId = picker.getString("cardId", null);
			List<String> memberIds = picker.getStringArray("memberIds");
			boolean finished = doc.containsKey("finished") ?
				picker.getBoolean("finished", false) : defaultFinished;

			if( cardId != null ){
				SprintResultCard converted = new SprintResultCard();
				converted.setSprintId(sprintId);
				converted.setCardId(cardId);
				converted.setMemberIds(memberIds);
				converted.setFinished(finished);

				log("fetching card actions " + cardId + "...");
				List<JSONObject> actions = fetcher.getCardActions(cardId, null, before);
				if( actions != null && actions.size() > 0 ){
					actions.forEach((act) ->
						converted.addTrelloAction(new TrelloActionRawData.JSONObjectModel(act)));
				}else{
					log("<ERROR> fetching error " + cardId);
					return null;
				}

				return converted;
			}else{
				return null;
			}
		}
	}


}
