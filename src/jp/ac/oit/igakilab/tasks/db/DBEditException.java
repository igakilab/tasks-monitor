package jp.ac.oit.igakilab.tasks.db;

public class DBEditException extends Exception{
	private static final long serialVersionUID = 1L;

	public static final int ID_NOTDEFINED = 101;
	public static final int ID_ALSO_REGISTED = 102;
	public static final int ID_NOT_REGISTED = 103;

	private int type;

	public DBEditException(int type, String msg){
		super(msg);
		this.type = type;
	}

	public DBEditException(int type){
		super();
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
