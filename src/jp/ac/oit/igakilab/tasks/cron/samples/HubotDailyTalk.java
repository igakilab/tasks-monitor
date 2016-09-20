package jp.ac.oit.igakilab.tasks.cron.samples;

import java.util.Calendar;

import org.bson.Document;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.tasks.http.HttpRequest;
import jp.ac.oit.igakilab.tasks.http.HttpRequest.ConnectionErrorHandler;
import jp.ac.oit.igakilab.tasks.http.HttpResponse;

public class HubotDailyTalk implements Runnable{
	public static void main(String[] args){
		HubotDailyTalk ht = new HubotDailyTalk("http://localhost:8080", "shell");
		ht.run();
	}

	static String[] MESSAGES = {
		"今日も一日頑張ろう！",
		"課題やタスクは早めに終わらせよう",
		"今日の運勢が気になったときは\"@igakilabot 運勢は？\"で聞いてみてね",
		"@ueda いつやるの？",
		"今日も一日気合を入れろ!"
	};
	static String[] WEEKS = {"null", "日", "月", "火", "水", "木", "金", "土"};

	static boolean SWITCH = true; //falseでテストモード リクエストを送信しない

	public static Scheduler createSchedule(String schedule, String u0, String r0){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new HubotDailyTalk(u0, r0));
		return scheduler;
	}

	private String hubotUrl;
	private String room;
	private String[] messages;

	public HubotDailyTalk(String u0, String r0, String[] m0){
		hubotUrl = u0;
		room = r0;
		messages = m0;
	}

	public HubotDailyTalk(String u0, String r0){
		this(u0, r0, MESSAGES);
	}

	public String sendMessage(String room, String message){
		HttpRequest request = new HttpRequest("POST", hubotUrl + "/hubot/send_message");
		request.setErrorHandler(new ConnectionErrorHandler(){
			public void onError(Exception e0){
				e0.printStackTrace();
			}
		});
		Document json = new Document();
		json.append("room", room).append("message", message);
		String body = json.toJson();
		//System.out.println(body);
		request.setRequestProperty("Content-type", "application/json");
		HttpResponse res = request.sendRequest(body);
		return (res != null) ? res.getResponseText() : "null";
	}

	public void run(){
		Calendar cal = Calendar.getInstance();
		String date = String.format("今日は%d年%d月%d日(%s）です",
			cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DATE),
			WEEKS[cal.get(Calendar.DAY_OF_WEEK)]);
		String talk = messages[(int)(Math.random() * messages.length)];

		if( SWITCH ){
			sendMessage(room, date + "\n" + talk);
		}else{
			System.out.println("HubotDailyTalk: room:" + room + ", msg:" + date + "\n" + talk);
		}
	}
}
