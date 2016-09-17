package jp.ac.oit.igakilab.tasks.cron;

import org.bson.Document;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.tasks.http.HttpRequest;

public class HubotTasksNotification implements Runnable{public static Scheduler createScheduler(String schedule, String url){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new HubotTasksNotification(url));
		return scheduler;
	}

	private String hubotUrl;

	public HubotTasksNotification(String hubotUrl){
		this.hubotUrl = hubotUrl;
	}

	/*
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

	public void notify(String room, String message){
		if( hubotUrl != null ){
			sendMessage(room, message);
		}else{
			System.out.format("TASKS NOTIFICATION\nroom: %s\nmessage:%s\n",
				room, message);
		}
	}

	public void run(){
		MongoClient client = TasksMongoClientBuilder.createClient();
		BoardDBDriver bdb = new BoardDBDriver(client);
		TrelloBoardActionsDB adb = new TrelloBoardActionsDB(client);

		List<Board> boards = bdb.getBoardList();
		if( boards.size() > 0 ){
			Board dbBoard = boards.get(0);
			List<TrelloAction> actions = adb.getTrelloActions(
				dbBoard.getId(), new DocumentTrelloActionParser());

			TrelloActionsBoard board = new TrelloActionsBoard();
			board.addActions(actions);
			board.build();

			notify("shell", board.getName());
		}
	}
	*/

	public void run(){
		String[] rooms = {"koike"};

		for(String room : rooms){
			if( hubotUrl != null ){
				HttpRequest request = new HttpRequest("POST", hubotUrl + "/hubot/send_message");
				request.setErrorHandler((e0) -> e0.printStackTrace());

				Document json = new Document("room", room);
				request.setRequestProperty("Content-type", "application/json");
				/*HttpResponse res = */request.sendRequest(json.toJson());
			}else{
				System.out.println("send request " + room);
			}
		}
	}
}
