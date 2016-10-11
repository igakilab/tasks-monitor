package jp.ac.oit.igakilab.tasks.db;

import com.mongodb.MongoClient;

import jp.ac.oit.igakilab.tasks.AppProperties;

public class TasksMongoClientBuilder {
	private static String DEFAULT_HOST = "localhost";
	private static int DEFAULT_PORT = 27017;

	private static MongoClient cachedClient = null;

	private static String dbHost(){
		return AppProperties.globalGet("tasks.db.host", DEFAULT_HOST);
	}

	private static int dbPort(){
		String portNum = AppProperties.globalGet("tasks.db.port");
		if( portNum != null ){
			int port = 0;
			try{
				port = Integer.parseInt(portNum);
			}catch(NumberFormatException e0){
				return DEFAULT_PORT;
			}
			return port;
		}
		return DEFAULT_PORT;
	}

	public static MongoClient createClient(String host, int port){
		if( host != null && port != 0 ){
			return new MongoClient(host, port);
		}else if( host != null ){
			return new MongoClient(host);
		}else{
			return new MongoClient();
		}
	}

	public static MongoClient createClient(){
		return createClient(dbHost(), dbPort());
	}

	public static MongoClient initCachedClient(){
		if( cachedClient != null ){
			cachedClient.close();
		}
		cachedClient = createClient();
		return cachedClient;
	}

	public static MongoClient getCachedClient(){
		if( cachedClient == null ){
			initCachedClient();
		}
		return cachedClient;
	}

	public static boolean closeCachedClient(){
		if( cachedClient != null ){
			cachedClient.close();
			return true;
		}else{
			return false;
		}
	}
}
