package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

public interface DocumentParser<T> {
	public T parse(Document doc);
}
