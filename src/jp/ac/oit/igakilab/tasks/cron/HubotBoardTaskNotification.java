package jp.ac.oit.igakilab.tasks.cron;

import java.util.Calendar;

import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.hubot.HubotSendMessage;
import jp.ac.oit.igakilab.tasks.scripts.SlackChannelTaskNotify;

public class HubotBoardTaskNotification extends CronTask{
	public static void main(String[] args){
		Scheduler s1 = CronTask.createScheduler("* * * * *");
		Scheduler s0 = createScheduler("* * * * *", "http://igakilabot.herokuapp.com");
		s0.start();
		s1.start();
	}

	public static Scheduler createScheduler(String s, String hubotUrl){
		return createScheduler(s, new HubotBoardTaskNotification(hubotUrl));
	}

	private String hubotUrl;

	public HubotBoardTaskNotification(String hubotUrl){
		this.hubotUrl = hubotUrl;
		this.taskName = "boardTaskNotification";
	}

	@Override
	public void execute(){
		HubotSendMessage msg = new HubotSendMessage(hubotUrl);
		MongoClient client = TasksMongoClientBuilder.createClient();

		SlackChannelTaskNotify notifer = new SlackChannelTaskNotify(client, msg);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 3);
		notifer.setNotifyLine(cal.getTime());

		notifer.execute();

		client.close();
	}
}
