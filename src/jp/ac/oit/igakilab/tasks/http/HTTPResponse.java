package jp.ac.oit.igakilab.tasks.http;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponse {
	private int status;
	private String response;
	private String url;
	private String method;
	private Map<String,String> headers;

	public HTTPResponse(int s0){
		status = s0;
		headers = new HashMap<String,String>();
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getResponseText() {
		return response;
	}
	public void setResponseText(String response) {
		this.response = response;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Map<String,String> getHeaders(){
		return headers;
	}

	public String getHeader(String key){
		return headers.get(key);
	}

	public void printResponse(PrintStream stream){
		stream.println("status: " + status);
		stream.println("url: " + url);
		stream.println("method: " + method);
		stream.println("headers:");
		for(String key : headers.keySet()){
			stream.println("  " + key + ": " + headers.get(key));
		}
		stream.println("responseText: " + response);
	}
}
