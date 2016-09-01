package jp.ac.oit.igakilab.tasks.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
	public static void main(String[] args){
		HTTPRequest request = new HTTPRequest("GET", "http://api.trello.com/1/members/me");
		request.setParameter("key", "67ad72d3feb45f7a0a0b3c8e1467ac0b")
			.setParameter("token", "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
		request.setErrorHander(new ConnectionErrorHander(){
			public void onError(Exception e0){
				System.err.println(e0.getMessage());
				e0.printStackTrace();
			}
		});

		HTTPResponse response = request.sendRequest();
		response.printResponse(System.out);
	}


	interface ConnectionErrorHander{
		public void onError(Exception e0);
	}

	private String url;
	private String method;
	private Map<String,String> params;
	private ConnectionErrorHander errorHander;
	private boolean followRedirects = true;

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

	public HTTPRequest setErrorHander(ConnectionErrorHander handler){
		errorHander = handler;
		return this;
	}

	public HTTPRequest setFollowRedirects(boolean a0){
		followRedirects = a0;
		return this;
	}

	public String generateUrlParameter(){
		StringBuffer buffer = new StringBuffer();
		for(String key : params.keySet()){
			if( buffer.length() > 0 ){
				buffer.append('&');
			}
			buffer.append(key).append('=').append(params.get(key));
		}
		return buffer.toString();
	}

	public String generateUrl(){
		if( params.size() > 0 ){
			return url;
		}else{
			return url + "?" + generateUrlParameter();
		}
	}

	private HttpURLConnection createConnection(URL url, String method)
	throws IOException, ProtocolException{
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		if( method.equals("POST") ){
			connection.setDoOutput(true);
		}
		connection.setRequestMethod(method);
		return connection;
	}



	private String receiveResponseText(HttpURLConnection conn)
	throws IOException{
		InputStreamReader isr = new InputStreamReader(conn.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		String tmp;
		StringBuffer response = new StringBuffer();
		while( (tmp = reader.readLine()) != null ){
			response.append(tmp);
		}
		return response.toString();
	}

	public HTTPResponse sendRequest(){
		HttpURLConnection connection = null;
		int statusCode;
		boolean redirect = false;

		do {
			URL objUrl = new URL(generateUrl());
			connection = createConnection(objUrl, method);
			statusCode = connection.getResponseCode();

			if(
				statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
				statusCode == HttpURLConnection.HTTP_MOVED_TEMP
			){
				String loc = connection.getHeaderField("Location");
				if( loc != null ){
					objUrl = new URL(loc);
					redirect = true;
				}
			}else{
				redirect = false;
			}
		} while( redirect );

		HTTPResponse response = new HTTPResponse(statusCode);
		response.setUrl(generateUrl());
		response.setMethod(method);
		for(String key : connection.getHeaderFields().keySet()){
			if( key != null ){
				String value = connection.getHeaderField(key);
			}
		}


		try{
			URL objUrl = new URL(generateUrl());

			connection = (HttpURLConnection)objUrl.openConnection();
			if( method.equals("POST") ){
				connection.setDoOutput(true);
			}
			connection.setRequestMethod(method);

			statusCode = connection.getResponseCode();
			if( statusCode == HttpURLConnection.HTTP_OK ){
				text = receiveResponseText(connection);
			}
		}catch(Exception e0){
			if( errorHander != null ){
				errorHander.onError(e0);
			}
			if( connection != null ){
				connection.disconnect();
			}
			return null;
		}

		HTTPResponse response = new HTTPResponse(statusCode);
		response.setUrl(generateUrl());
		response.setMethod(method);
		for(String key : connection.getHeaderFields().keySet()){
			if( key != null ){
				String value = connection.getHeaderField(key);
				if( value != null ){
					response.getHeaders().put(key, value);
				}
			}
		}
		response.setResponseText(text);

		connection.disconnect();
		return response;
	}


}
