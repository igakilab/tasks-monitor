package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.db.converters.ProjectDataDocumentConverter.ProjectData;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class ProjectDataDocumentConverter
implements DocumentConverter<ProjectData>{
	public static class ProjectData{
		private String id;
		private String name;

		public ProjectData(String i0){
			id = i0;
			name = null;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public ProjectDataDocumentConverter(){}

	public ProjectData parse(Document doc){
		if( doc.containsKey("id") ){
			ProjectData data = new ProjectData(doc.getString("id"));
			DocumentValuePicker picker = new DocumentValuePicker(doc);
			data.setName(picker.getString("name", null));
			return data;
		}
		return null;
	}

	public Document convert(ProjectData data){
		Document doc = new Document();
		doc.append("id", data.getId());
		if( data.getName() != null )
			doc.append("name", data.getName());
		return doc;
	}




}
