package jp.ac.oit.igakilab.tasks.cron;

import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.hubot.HubotTaskNotify;
import jp.ac.oit.igakilab.tasks.scripts.SlackChannelMeetingNotify;

public class HubotMeetingNotification extends CronTask{
	public static Scheduler createScheduler(String s, String hubotUrl, String homeUrl){
		HubotMeetingNotification n = new HubotMeetingNotification(hubotUrl, homeUrl);
		return createScheduler(s, n);
	}

	private String hubotUrl;
	private String homeUrl;

	public HubotMeetingNotification(String hubotUrl, String homeUrl){
		this.hubotUrl = hubotUrl;
		this.homeUrl = homeUrl;
		this.taskName = "meetingNotification";
	}

	public void execute(){
		HubotTaskNotify msg = new HubotTaskNotify(hubotUrl);
		MongoClient client = TasksMongoClientBuilder.createClient();

		SlackChannelMeetingNotify notifer = new SlackChannelMeetingNotify(client, msg);
		notifer.setHomeUrl(homeUrl);

		notifer.execute();

		client.close();
	}
}
