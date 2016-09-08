package jp.ac.oit.igakilab.tasks.cron;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.tasks.http.HttpRequest;

public class HubotDailyTalk implements Runnable{
	static String[] MESSAGES = {
		"今日も一日頑張ろう！",
		"課題やタスクは早めに終わらせよう",
		"今日の運勢が気になったときは\"@igakilabot 運勢は？\"で聞いてみてね",
		"@ueda いつやるの？"
	};

	public static Scheduler createSchedule(String schedule){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new HubotDailyTalk());
		return scheduler;
	}

	public void run(){
		HttpRequest request = new HttpRequest("POST", "http://igakilabot.herokuapp.com/sendmsg/");
		request.setParameter("message", MESSAGES[(int)(Math.random() * MESSAGES.length)]);
		request.setParameter("room", "botbot");
		request.sendRequest();
	}
}
