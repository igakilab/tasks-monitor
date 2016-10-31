package jp.ac.oit.igakilab.tasks.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.DebugLog;
import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.cron.HubotBoardTaskNotification;
import jp.ac.oit.igakilab.tasks.cron.HubotTasksNotification;
import jp.ac.oit.igakilab.tasks.cron.UpdateTrelloBoardActions;
import jp.ac.oit.igakilab.tasks.cron.samples.HubotDailyTalk;
import jp.ac.oit.igakilab.tasks.cron.samples.SampleCron;

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

	private void initAppProperties(){
		//importPropertyFiles
		loadPropertyFile("tasks.properties");

		Map<String,String> systemProperties = new HashMap<String,String>();
		System.getProperties().forEach((key, value) ->
			systemProperties.put((String)key, (String)value));

		AppProperties.global.importPropertiesMap(systemProperties, "tasks");

		AppProperties.global.setIfNotHasValue("tasks.trello.key",
			"67ad72d3feb45f7a0a0b3c8e1467ac0b");
		AppProperties.global.setIfNotHasValue("tasks.trello.token",
			"268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");
	}

	private Scheduler hello, boardUpdater, hubotDailyTalk, tasksNotifer;
	private Scheduler boardTasksNotifer;
	private void initCronTasks(){
		SampleCron cron = new SampleCron();
		hello = cron.schedule("* * * * *", null);
		hello.start();

		boardUpdater = UpdateTrelloBoardActions.createScheduler("*/10 * * * *");
		boardUpdater.start();

		String hubotUrl = AppProperties.global.get("tasks.hubot.url");
		if( hubotUrl != null ){
			hubotDailyTalk = HubotDailyTalk.createSchedule("0 9 * * *", hubotUrl, "botbot");
			hubotDailyTalk.start();
			tasksNotifer = HubotTasksNotification.createScheduler("5 9 * * *", hubotUrl, true);
			tasksNotifer.start();
			boardTasksNotifer = HubotBoardTaskNotification.createScheduler("* * * * *", hubotUrl);
			boardTasksNotifer.start();
		}
	}

	private void destroyCronTasks(){
		hello.stop();
		boardUpdater.stop();
		if( hubotDailyTalk != null ) hubotDailyTalk.stop();
		if( tasksNotifer != null ) tasksNotifer.stop();
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		logger.log(DebugLog.LS_INFO, "Server Started");

		initAppProperties();
		logger.log(DebugLog.LS_INFO, "AppProperties configured");

		initCronTasks();
		logger.log(DebugLog.LS_INFO, "Cron initialized.");

		logger.log(DebugLog.LS_INFO, "App Initialized!");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		destroyCronTasks();
		logger.log(DebugLog.LS_INFO, "Server shutdown");
	}
}
