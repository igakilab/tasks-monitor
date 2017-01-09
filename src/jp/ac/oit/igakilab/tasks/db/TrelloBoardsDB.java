package jp.ac.oit.igakilab.tasks.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class TrelloBoardsDB {
	public static String DB_NAME = "tasks-monitor";
	public static String COL_NAME = "trello_boards";

	public static class Board{
		static Board convert(Document doc){
			Board board = new Board();
			board.id = doc.getString("id");
			board.lastUpdate = doc.getDate("lastUpdate");
			board.slackNotifyEnabled = doc.getBoolean("slackNotifyEnabled", false);
			board.slackMeetingNotifyHour = doc.getInteger("slackMeetingNotifyHour");
			DocumentValuePicker picker = new DocumentValuePicker(doc);
			board.defaultTags = picker.getStringArray("defaultTags");
			return board;
		}

		private String id;
		private Date lastUpdate;
		private boolean slackNotifyEnabled;
		private Integer slackMeetingNotifyHour;
		private List<String> defaultTags;

		public String getId(){return id;}
		public Date getLastUpdate(){return lastUpdate;}
		public boolean getSlackNotifyEnabled(){return slackNotifyEnabled;}
		public Integer getSlackMeetingNotifyHour(){return slackMeetingNotifyHour;}
		public List<String> getDefaultTags(){return defaultTags;}
	}

	private MongoClient client;

	public TrelloBoardsDB(MongoClient client){
		this.client = client;
	}

	public TrelloBoardsDB(String host, int port){
		this.client = new MongoClient(host, port);
	}

	private MongoCollection<Document> getCollection(){
		return this.client.getDatabase(DB_NAME).getCollection(COL_NAME);
	}

	public boolean boardIdExists(String boardId){
		Bson filter = Filters.eq("id", boardId);
		return getCollection().count(filter) > 0;
	}

	public boolean addBoard(String boardId){
		if( !boardIdExists(boardId) ){
			getCollection().insertOne(new Document("id", boardId));
			return true;
		}
		return false;
	}

	public boolean removeBoard(String boardId){
		Bson filter = Filters.eq("id", boardId);
		DeleteResult res = getCollection().deleteOne(filter);
		return res.getDeletedCount() > 0;
	}

	public Document getBoardById(String boardId){
		Bson filter = Filters.eq("id", boardId);
		return getCollection().find(filter).first();
	}

	public List<Board> getBoardList(){
		List<Board> result = new ArrayList<Board>();
		for(Document doc : getCollection().find()){
			result.add(Board.convert(doc));
		}
		return result;
	}

	public Date getLastUpdateDate(String boardId){
		Document doc = getBoardById(boardId);
		return Board.convert(doc).getLastUpdate();
	}

	public int updateLastUpdateDate(String boardId, Date lastdate){
		Bson filter = Filters.eq("id", boardId);
		Bson update = Updates.set("lastUpdate", lastdate);

		UpdateResult result = getCollection().updateOne(filter, update);
		return (int)result.getModifiedCount();
	}

	public boolean clearLastUpdateDate(String boardId){
		Bson filter = Filters.eq("id", boardId);
		Bson update = Updates.unset("lastUpdate");

		UpdateResult result = getCollection().updateOne(filter, update);
		return result.getModifiedCount() > 0;
	}

	public int clearAllLastUpdateDate(){
		Bson filter = Filters.exists("lastUpdate");
		Bson update = Updates.unset("lastUpdate");

		UpdateResult result = getCollection().updateMany(filter,  update);
		return (int)result.getModifiedCount();
	}

	public boolean getSlackNotifyEnabled(String boardId){
		Document doc = getBoardById(boardId);
		return Board.convert(doc).getSlackNotifyEnabled();
	}

	public boolean setSlackNotifyEnabled(String boardId, boolean b){
		Bson filter = Filters.eq("id", boardId);
		Bson update = Updates.set("slackNotifyEnabled", b);

		UpdateResult res = getCollection().updateOne(filter, update);
		return res.getModifiedCount() > 0;
	}

	public boolean setSlackMeetingNotifyHour(String boardId, Integer hour){
		Bson filter = Filters.eq("id", boardId);
		Bson update = null;
		if( hour != null && hour >= 0 && hour < 24 ){
			update = Updates.set("slackMeetingNotifyHour", hour);
		}else{
			update = Updates.unset("slackMeetingNotifyHour");
		}

		UpdateResult res = getCollection().updateOne(filter, update);
		return res.getModifiedCount() > 0;
	}

	public Integer getSlackMeetingNotifyHour(String boardId){
		Document doc = getBoardById(boardId);
		return Board.convert(doc).getSlackMeetingNotifyHour();
	}

	public List<String> getDefaultTags(String boardId){
		Document doc = getBoardById(boardId);
		return Board.convert(doc).getDefaultTags();
	}

	public boolean addDefaultTags(String boardId, Collection<String> tags){
		List<String> registed = getDefaultTags(boardId);
		List<String> willRegist = new ArrayList<>();
		for(String newTag : tags){
			if( !registed.contains(newTag) ){
				willRegist.add(newTag);
			}
		}

		if( willRegist.size() > 0 ){
			Bson filter = Filters.eq("boardId", boardId);
			Bson updates = Updates.pushEach("defaultTags", willRegist);

			getCollection().updateOne(filter, updates);

			return true;
		}else{
			return false;
		}
	}
}
