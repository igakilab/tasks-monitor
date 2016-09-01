package jp.ac.oit.igakilab.tasks.http;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
	String url;
	String method;
	Map<String,String> params;

	public HTTPRequest(){
		params = new HashMap<String,String>();
	}

	public HTTPRequest(String method, String url){
		this();
		setMethod(method);
		setUrl(url);
	}

	public HTTPRequest(String method, String url, Map<String,String> params){
		this();
		setMethod(method);
		setUrl(url);
		for(String key : params.keySet()){
			setParameter(key, params.get(key));
		}
	}

	public HTTPRequest setMethod(String method){
		this.method = method;
		return this;
	}

	public String getMethod(){
		return method;
	}

	public HTTPRequest setUrl(String url){
		int idx = url.indexOf('?');
		this.url = idx >= 0 ?
			url.substring(0, Math.max(idx-1, 0)) : url;
		return this;
	}

	public String getUrl(){
		return url;
	}

	public void clearParameters(){
		params.clear();
	}

	public HTTPRequest setParameter(String key, String value){
		params.put(key, value);
		return this;
	}

	public String getParameter(String key){
		return params.get(key);
	}

	public Map<String,String> getParameters(){
		return params;
	}
	
	
	


}
