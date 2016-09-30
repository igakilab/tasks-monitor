package jp.ac.oit.igakilab.tasks.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequest {
	public static void main(String[] args){
		/*HttpRequest request = new HttpRequest("GET", "http://api.trello.com/1/members/me");
		request.setParameter("key", "67ad72d3feb45f7a0a0b3c8e1467ac0b")
			.setParameter("token", "268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");*/
		HttpRequest request = new HttpRequest("GET", "http://192.168.1.193/80");

		System.out.println("Request start");
		HttpResponse response = new HttpResponse(0);
		try{
			response = request.sendRequest();
		}catch(MalformedURLException e0){
			System.err.println("MalformedURLException: " + e0.getMessage());
		}catch(ProtocolException e1){
			System.err.println("ProtocolException: " + e1.getMessage());
		}catch(IOException e2){
			System.err.println("IOException: " + e2.getMessage());
			e2.printStackTrace();
		}

		response.printResponse(System.out);
	}

	interface ErrorCatcher<T>{
		public T execute(SendRequestWrapper req);
	}

	public class SendRequestWrapper{
		private HttpRequest req;
		private String body;

		SendRequestWrapper(HttpRequest req, String body){
			this.req = req;
			this.body = body;
		}

		public HttpResponse sendRequest()
		throws MalformedURLException, ProtocolException, IOException{
			return req.sendRequest(body);
		}
	}

	public static final int HTTP_OK = HttpURLConnection.HTTP_OK;

	public static boolean DEFAULT_HANDLER_STACKTRACE_ENABLED = true;
	public static ErrorCatcher<HttpResponse> DEFAULT_HANDLER = (req) -> {
		HttpResponse res;
		try{
			res = req.sendRequest();
		}catch(IOException e0){
			if( DEFAULT_HANDLER_STACKTRACE_ENABLED )
				e0.printStackTrace();
			return null;
		}
		return res;
	};

	private String url;
	private String method;
	private Map<String,String> params;
	private Map<String, String> properties;
	private boolean followRedirects = true;

	public HttpRequest(){
		params = new HashMap<String,String>();
		properties = new HashMap<String, String>();
	}

	public HttpRequest(String method, String url){
		this();
		setMethod(method);
		setUrl(url);
	}

	public HttpRequest(String method, String url, Map<String,String> params){
		this();
		setMethod(method);
		setUrl(url);
		for(String key : params.keySet()){
			setParameter(key, params.get(key));
		}
	}

	public HttpRequest setMethod(String method){
		this.method = method;
		return this;
	}

	public String getMethod(){
		return method;
	}

	public HttpRequest setUrl(String url){
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

	public HttpRequest setParameter(String key, String value){
		params.put(key, value);
		return this;
	}

	public String getParameter(String key){
		return params.get(key);
	}

	public HttpRequest setRequestProperty(String key, String value){
		properties.put(key, value);
		return this;
	}

	public Map<String,String> getParameters(){
		return params;
	}

	public HttpRequest setFollowRedirects(boolean a0){
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
			return url + "?" + generateUrlParameter();
		}else{
			return url;
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
		InputStreamReader isr;
		try{
			isr = new InputStreamReader(conn.getInputStream());
		}catch(FileNotFoundException e0){
			return null;
		}

		BufferedReader reader = new BufferedReader(isr);
		String tmp;
		StringBuffer response = new StringBuffer();
		while( (tmp = reader.readLine()) != null ){
			response.append(tmp);
		}
		return response.toString();
	}

	public HttpResponse sendRequest()
	throws ProtocolException, MalformedURLException, IOException{
		return sendRequest((String)null);
	}

	public HttpResponse sendRequest(String bodyText)
	throws ProtocolException, MalformedURLException, IOException{
		//コネクションオブジェクト、ステータスコード、
		//リダイレクトかどうか 変数を初期化
		HttpURLConnection connection = null;
		int statusCode;

		//〇リクエスト送信

		//コネクション動作用に初期値を設定
		//urlオブジェクトの生成とリダイレクトフラグの初期化
		String tmpUrl = generateUrl();
		URL urlObj = new URL(tmpUrl);
		boolean redirect = false;

		//httpコネクションを行い、データを取得する
		//このdo-whileではfollowRedirectがtrue時に
		//リダイレクションに従ってコネクションを再接続する
		do{
			//デバッグ用
			//System.out.println("send req: " + tmpUrl);

			//コネクションを生成
			connection = createConnection(urlObj, method);

			//プロパティ(ヘッダ)を設定
			for(Entry<String,String> entry : properties.entrySet()){
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}

			//bodyを設定
			if( bodyText != null ){
				BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(
						connection.getOutputStream(), StandardCharsets.UTF_8));

				writer.write(bodyText);
				writer.flush();
			}

			//コネクションを実行？ ステータスコードを取得
			statusCode = connection.getResponseCode();

			//リダイレクションのチェック
			if(
				statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
				statusCode == HttpURLConnection.HTTP_MOVED_TEMP
			){
				//リダイレクト時: 次のurlオブジェクトを生成
				tmpUrl = connection.getHeaderField("Location");
				urlObj = new URL(tmpUrl);

				//リダイレクト時: リダイレクトフラグの設定
				redirect = true;
			}else{
				//非リダイレクト: リダイレクトフラグの解除
				redirect = false;
			}
		} while( redirect && followRedirects );

		//〇通信結果の生成

		//HttpResponseオブジェクトを生成
		HttpResponse response = new HttpResponse(statusCode);

		//設定値を記録
		response.setMethod(method);
		response.setUrl(tmpUrl);

		//ヘッダーを記録
		for(String key : connection.getHeaderFields().keySet()){
			String val = connection.getHeaderField(key);
			response.getHeaders().put(key, val);
		}

		//レスポンスボディを記録
		response.setResponseText(receiveResponseText(connection));

		//コネクションを切断 結果の返却
		connection.disconnect();
		return response;
	}

	public <T> T sendRequest(String body, ErrorCatcher<T> catcher){
		return catcher.execute(new SendRequestWrapper(this, body));
	}

	public <T> T sendRequest(ErrorCatcher<T> catcher){
		return sendRequest(null, catcher);
	}
}
