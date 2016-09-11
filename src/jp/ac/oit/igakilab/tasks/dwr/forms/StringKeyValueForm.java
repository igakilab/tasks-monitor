package jp.ac.oit.igakilab.tasks.dwr.forms;

public class StringKeyValueForm {
	private String key;
	private String value;

	public StringKeyValueForm(){};

	public StringKeyValueForm(String key, String value){
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
