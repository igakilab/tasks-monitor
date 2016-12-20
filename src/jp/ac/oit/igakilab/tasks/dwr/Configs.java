package jp.ac.oit.igakilab.tasks.dwr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.cron.UpdateTrelloBoardActions;
import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;
import jp.ac.oit.igakilab.tasks.db.TrelloBoardsDB;
import jp.ac.oit.igakilab.tasks.dwr.forms.StringKeyValueForm;
import jp.ac.oit.igakilab.tasks.hubot.HubotSendMessage;
import jp.ac.oit.igakilab.tasks.hubot.HubotTaskNotify;
import jp.ac.oit.igakilab.tasks.scripts.SlackChannelTaskNotify;

public class Configs {
	public Configs(){};

	public StringKeyValueForm[] getProperties(){
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();
		Properties properties = System.getProperties();

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			result.add(new StringKeyValueForm(key, properties.getProperty(key)));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}

	public StringKeyValueForm[] getAppProperties(){
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();
		Map<String,String> properties = AppProperties.global.getProperties();

		for(String key : properties.keySet()){
			result.add(new StringKeyValueForm(key, properties.get(key)));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}

	public StringKeyValueForm[] getChildAppProperties(String upperkey){
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();
		Map<String,String> properties = AppProperties.global
			.getChildProperties(upperkey);

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			result.add(new StringKeyValueForm(key, properties.get(key)));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}

	public StringKeyValueForm[] getEnviromentVariables(){
		Map<String,String> envs = System.getenv();
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();

		for(Entry<String,String> entry : envs.entrySet()){
			result.add(new StringKeyValueForm(entry.getKey(), entry.getValue()));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}

	public void clearTrelloActionsCache(){
		MongoClient client = TasksMongoClientBuilder.createClient();

		//clear actions cache
		MongoCollection<Document> trelloBoardActions =
			client.getDatabase("tasks-monitor").getCollection("trello_board_actions");
		trelloBoardActions.deleteMany(new Document());

		//clear lastupdate data
		MongoCollection<Document> trelloBoard =
			client.getDatabase("tasks-monitor").getCollection("trello_boards");
		trelloBoard.updateMany(new Document(), Updates.set("lastUpdate", null));

		client.close();
	}

	public void updateTrelloActionsCache(){
		UpdateTrelloBoardActions updater = new UpdateTrelloBoardActions();
		updater.run();
	}

	public boolean setBoardSlackNotification(String boardId, boolean b){
		MongoClient c = TasksMongoClientBuilder.createClient();
		TrelloBoardsDB bdb = new TrelloBoardsDB(c);
		boolean res = bdb.setSlackNotifyEnabled(boardId, b);
		c.close();
		return res;
	}

	public String hubotSendMessageTest(String dest)
	throws IOException{
		if( !AppProperties.global.hasValue("tasks.hubot.url") ){
			return "test: hubotのurlが設定されていません";
		}

		String url = AppProperties.global.get("tasks.hubot.url");
		HubotSendMessage hdt = new HubotSendMessage(url);
		if( dest != null ){
			hdt.send(dest, "このメッセージはテストです");
			return "送信しました";
		}else{
			return "宛先を指定してください";
		}
	}

	private String testHubotBoardTaskNotification(String boardId, int date){
		MongoClient c = TasksMongoClientBuilder.createClient();
		String hubotUrl = AppProperties.global.get("tasks.hubot.url");
		HubotTaskNotify m = new HubotTaskNotify(hubotUrl);

		SlackChannelTaskNotify notifer = new SlackChannelTaskNotify(c, m);
		notifer.setHeader("テスト送信");

		if( date >= 0 ){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, date);
			notifer.setNotifyLine(cal.getTime());
		}

		boolean res;
		if( boardId != null ){
			res = notifer.execute(boardId);
		}else{
			res = notifer.execute();
		}

		return "URL: " + hubotUrl + " RESULT: " + res;
	}

	public String testBoardTaskNotifyBD(String boardId, int date){
		return testHubotBoardTaskNotification(boardId, date);
	}

	public String testBoardTaskNotifyB(String boardId){
		return testHubotBoardTaskNotification(boardId, -1);
	}

	public String testBoardTaskNotifyD(int date){
		return testHubotBoardTaskNotification(null, date);
	}

	public String testBoardTaskNotify(){
		return testHubotBoardTaskNotification(null, -1);
	}
}
