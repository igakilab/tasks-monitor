package jp.ac.oit.igakilab.tasks.db;

public interface DBInterface<T>{
	
	public boolean insert(T data);
	public boolean upsert(T data);
	
}
