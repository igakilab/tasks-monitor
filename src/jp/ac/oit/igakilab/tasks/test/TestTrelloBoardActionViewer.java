package jp.ac.oit.igakilab.tasks.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import jp.ac.oit.igakilab.tasks.db.TasksMongoClientBuilder;

public class TestTrelloBoardActionViewer {
	public static void main(String[] args){
		MongoClient client = TasksMongoClientBuilder.createClient();
		MongoCollection<Document> col = client
			.getDatabase("tasks-monitor").getCollection("trello_board_actions");

		FindIterable<Document> result = col.find(Filters.eq("boardId", "57d3f5cac2c3720549a9b8c1"));

		showActions(toList(result));

		client.close();
	}

	private static <T> List<T> toList(Iterable<T> iterable){
		List<T> list = new ArrayList<T>();
		for(T obj : iterable){
			list.add(obj);
		}
		return list;
	}

	private static void showActions(List<Document> actions){
		DateFormat pdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		DateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
		for(int i=0; i<actions.size(); i++){
			Document action = actions.get(i);
			Document memberCreator = (Document)action.get("memberCreator");
			Date date;
			try{ date = pdf.parse(action.getString("date"));
			}catch(ParseException e0){ date = null; }
			System.out.format("%2d: %s %s %s\n",
				i, df.format(date),
				action.getString("type"), memberCreator.getString("fullName"));
			//System.out.println(action.toJson());

			showData(i, action);

			/*
			Document data = (Document)action.get("data");
			System.out.format("%2d: \t %s\n", i, data.toJson());
			Document dataOld = (Document)data.get("old");
			if( dataOld != null ){
				System.out.format("%2d: \t %s\n", i, dataOld.toJson());
			}
			*/
		}
	}

	private static void showData(int num, Document action){
		String actionType = action.getString("type");
		if( actionType.equals("createBoard") || actionType.equals("updateBoard") ){
			Document board = (Document) ((Document)action.get("data")).get("board");
			String actLabel = actionType.equals("createBoard") ? "CREATE" : "UPDATE";
			System.out.format("%2d: \tBOARD %s %s\n", num, actLabel, board.toJson());
		}else if( actionType.equals("createList") || actionType.equals("updateList") ){
			Document list = (Document) ((Document)action.get("data")).get("list");
			String actLabel = actionType.equals("createList") ? "CREATE" : "UPDATE";
			System.out.format("%2d: \tLIST %s %s\n", num, actLabel, list.toJson());
		}else if( actionType.equals("createCard") || actionType.equals("updateCard") ){
			Document card = (Document) ((Document)action.get("data")).get("card");
			String actLabel = actionType.equals("createCard") ? "CREATE" : "UPDATE";
			System.out.format("%2d: \tCARD %s %s\n", num, actLabel, card.toJson());
		}else if( actionType.equals("addMemberToBoard") || actionType.equals("addMemberToCard") ){
			Document member = (Document)action.get("member");
			String targetLabel = actionType.equals("addMemberToCard") ? "CARD" : "BOARD";
			System.out.format("%2d: \t%s ADDMEMBER %s\n", num, targetLabel, member.toJson());
		}else{
			System.out.format("%2d: \tignore action type:%s\n", num, actionType);
		}
	}


}
