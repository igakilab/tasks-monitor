package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

public interface DocumentConverter<T> {
	public T parse(Document doc);
	public Document convert(T data);
}
