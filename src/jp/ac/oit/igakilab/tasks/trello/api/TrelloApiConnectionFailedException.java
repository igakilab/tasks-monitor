package jp.ac.oit.igakilab.tasks.trello.api;

public class TrelloApiConnectionFailedException extends Exception{
	private int statusCode;
	private boolean isHttpError;

	TrelloApiConnectionFailedException(int statusCode, boolean isHttpError, String msg){
		super(msg);
		this.statusCode = statusCode;
		this.isHttpError = isHttpError;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public boolean isHttpError() {
		return isHttpError;
	}
}
