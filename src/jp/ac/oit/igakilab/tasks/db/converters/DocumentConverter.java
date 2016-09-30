package jp.ac.oit.igakilab.tasks.db.converters;

import org.bson.Document;

public interface DocumentConverter<T> {
	public Document convert(T data);
}
