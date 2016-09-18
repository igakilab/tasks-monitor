package jp.ac.oit.igakilab.tasks.cron.samples;

import java.util.Date;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import it.sauronsoftware.cron4j.Scheduler;

public class GarbageMakingMachine implements Runnable{
	public static String DB_HOST = "150.89.234.253";
	public static int DB_PORT = 27017;
	public static String DB_NAME = "tasks";
	public static String DB_COLLECTION = "garbage";

	public static String[] MEMBERS = {"kiichi", "hayashi", "gida", "shinya", "zoe",
		"mitsui", "kitaba", "ryokun", "dekopon", "ueyama", "shimizu"};

	public static Scheduler createScheduler(String schedule){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new GarbageMakingMachine());
		return scheduler;
	}


	public void run(){
		MongoClient client = new MongoClient(DB_HOST, DB_PORT);
		MongoCollection<Document> collection =
			client.getDatabase(DB_NAME).getCollection(DB_COLLECTION);

		Document doc = new Document();
		doc.append("time", new Date())
			.append("choice", MEMBERS[(int)(Math.random() * MEMBERS.length)]);
		collection.insertOne(doc);

		client.close();
	}
}
