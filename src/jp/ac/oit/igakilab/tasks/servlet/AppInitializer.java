package jp.ac.oit.igakilab.tasks.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.AppProperties;

public class AppInitializer implements ServletContextListener{
	private DebugLog logger;
	private String HOMEPATH;

	public AppInitializer(){
		//set homepath
		HOMEPATH = System.getenv("CATALINA_HOME");
		if( HOMEPATH == null ){
			Exception e0 = new Exception("HOMEPATH is undefined");
			e0.printStackTrace();
			HOMEPATH = "";
		}else{
			HOMEPATH += "/";
		}

		//init logger
		DebugLog.LOG_DIR = HOMEPATH + "logs/tasks-monitor/";
		logger = new DebugLog("AppInitializer");
	}

	private boolean loadPropertyFile(String filename){
		File file = new File(HOMEPATH + "conf/" + filename);
		if( file.exists() ){
			try{
				System.getProperties().load(new FileInputStream(file));
			}catch(IOException e0){
				logger.log(DebugLog.LS_EXCEPTION, "loadPropertyFile", e0.getMessage());
				return false;
			}
			return true;
		}else{
			logger.log(DebugLog.LS_WARN, "loadPropertyFile", filename + "is not found");
			return false;
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		logger.log(DebugLog.LS_INFO, "Server Started");

		//AppProperties init
		loadPropertyFile("test.properties");
		loadPropertyFile("tasks.properties");

		Map<String,String> systemProperties = new HashMap<String,String>();
		for(Object key : System.getProperties().keySet()){
			systemProperties.put((String)key, System.getProperty((String)key));
		}
		AppProperties.global.importPropertiesMap(systemProperties, "test");
		AppProperties.global.importPropertiesMap(systemProperties, "tasks");

		if(
			AppProperties.global.get("tasks.trello.key") == null ||
			AppProperties.global.get("tasks.trello.token") == null
		){
			AppProperties.global.set("tasks.trello.key", "67ad72d3feb45f7a0a0b3c8e1467ac0b");
			AppProperties.global.set("tasks.trello.token",
				"268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
		}

		logger.log(DebugLog.LS_INFO, "AppProperties configured");

		logger.log(DebugLog.LS_INFO, "App Initialized!");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		logger.log(DebugLog.LS_INFO, "Server shutdown");
	}
}