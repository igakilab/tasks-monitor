package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.db.SprintResultsDB;
import jp.ac.oit.igakilab.tasks.sprints.SprintResult;

public class SprintResultDocumentConverter
implements DocumentParser<SprintResult>{
	private SprintResultsDB db;
	private SprintResultCardDocumentConverter srcdc;

	public SprintResultDocumentConverter(SprintResultsDB db){
		this.db = db;
		this.srcdc = new SprintResultCardDocumentConverter();
	}

	public SprintResult parse(Document doc){
		if( doc.containsKey("sprintId") ){
			SprintResult data = new SprintResult(doc.getString("sprintId"));

			if( doc.containsKey("createdAt") ){
				data.setCreatedAt(doc.getDate("createdAt"));
			}else{
				data.setCreatedAt(db.getCreatedDateBySprintId(data.getSprintId()));
			}

			db.getSprintResultCardsBySprintId(data.getSprintId(), srcdc).forEach(
				(card -> data.addSprintCard(card)));

			return data;
		}else{
			return null;
		}
	}
}
