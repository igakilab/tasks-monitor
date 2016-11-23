package jp.ac.oit.igakilab.tasks.cron;

import java.io.IOException;
import java.util.Calendar;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.hubot.HubotSendMessage;

public class HubotDailyTalk extends CronTask{
	public static void main(String[] args){
		HubotDailyTalk ht = new HubotDailyTalk("http://localhost:8082", "shell");
		ht.run();
	}

	static String[] MESSAGES = {
		"今日も一日頑張ろう！",
		"課題やタスクは早めに終わらせよう",
		"今日の運勢が気になったときは\"@igakilabot 運勢は？\"で聞いてみてね"
	};
	static String[] WEEKS = {"null", "日", "月", "火", "水", "木", "金", "土"};

	static boolean SWITCH = true; //falseでテストモード リクエストを送信しない

	public static Scheduler createSchedule(String schedule, String u0, String r0){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new HubotDailyTalk(u0, r0));
		return scheduler;
	}

	private HubotSendMessage msg;
	private String room;
	private String[] messages;

	public HubotDailyTalk(String u0, String r0, String[] m0){
		msg = new HubotSendMessage(u0);
		room = r0;
		messages = m0;
		taskName = "HubotDailyTalk";
	}

	public HubotDailyTalk(String u0, String r0){
		this(u0, r0, MESSAGES);
	}

	public void execute(){
		Calendar cal = Calendar.getInstance();
		String date = String.format("今日は%d年%d月%d日(%s）です",
			cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DATE),
			WEEKS[cal.get(Calendar.DAY_OF_WEEK)]);
		String talk = messages[(int)(Math.random() * messages.length)];

		if( SWITCH ){
			log(DebugLog.LS_INFO, "send message: [" + room + "] " + date + talk);
			try{
				msg.send(room,  date + "\n" + talk);
			}catch(IOException e0){
				log(DebugLog.LS_WARN, "message send failed: " + e0.getMessage());
				e0.printStackTrace();
			}
		}else{
			System.out.println("HubotDailyTalk: room:" + room + ", msg:" + date + "\n" + talk);
		}
	}
}
