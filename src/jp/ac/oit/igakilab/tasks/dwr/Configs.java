package jp.ac.oit.igakilab.tasks.dwr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.dwr.forms.StringKeyValueForm;

public class Configs {
	public Configs(){};

	public StringKeyValueForm[] getProperties(){
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();
		Properties properties = System.getProperties();

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			result.add(new StringKeyValueForm(key, properties.getProperty(key)));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}

	public StringKeyValueForm[] getAppProperties(){
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();
		Map<String,String> properties = AppProperties.getPropertiesMap();

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			result.add(new StringKeyValueForm(key, properties.get(key)));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}

	public StringKeyValueForm[] getChildAppProperties(String upperkey){
		List<StringKeyValueForm> result = new ArrayList<StringKeyValueForm>();
		Map<String,String> properties = AppProperties.getChildProperties(upperkey);

		for(Object keyObj : properties.keySet()){
			String key = (String)keyObj;
			result.add(new StringKeyValueForm(key, properties.get(key)));
		}

		return result.toArray(new StringKeyValueForm[result.size()]);
	}
}
