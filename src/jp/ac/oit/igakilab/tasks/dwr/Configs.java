package jp.ac.oit.igakilab.tasks.dwr;

import java.util.Map;
import java.util.Properties;

import jp.ac.oit.igakilab.tasks.AppProperties;

public class Configs {
	public Configs(){};

	public String getProperties(){
		StringBuffer buffer = new StringBuffer();
		Properties properties = System.getProperties();

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			buffer.append(key + ": " + properties.getProperty(key) + "\n");
		}

		return buffer.toString();
	}

	public String getAppProperties(){
		StringBuffer buffer = new StringBuffer();
		Map<String,String> properties = AppProperties.getPropertiesMap();

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			buffer.append(key + ": " + properties.get(key) + "\n");
		}

		return buffer.toString();
	}
}
