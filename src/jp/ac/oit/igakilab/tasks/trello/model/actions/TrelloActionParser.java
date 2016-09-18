package jp.ac.oit.igakilab.tasks.trello.model.actions;

public interface TrelloActionParser<T> {
	public TrelloAction parse(T data);
}
