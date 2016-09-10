package jp.ac.oit.igakilab.tasks.servlet;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.LogRecorder;
import jp.ac.oit.igakilab.tasks.AppProperties;
import jp.ac.oit.igakilab.tasks.cron.HubotDailyTalk;
import jp.ac.oit.igakilab.tasks.cron.SampleCron;

public class CronTasks implements ServletContextListener{
	LogRecorder logger;
	SampleCron cron;
	Scheduler hello, dailyTalk;

	public CronTasks(){
		logger = new LogRecorder("cron-test.txt", true);
		cron = new SampleCron();
	}

	private boolean loadPropertyFile(String filename){
		String filePath = System.getenv("CATALINA_HOME") + "/conf/" + filename;
		try{
			System.getProperties().load(new FileInputStream(filePath));
		}catch(IOException e0){
			e0.printStackTrace();
			return false;
		}
		return true;
	}

	public void contextInitialized(ServletContextEvent event){
		logger.addSingleLog("Cron Initialization.", true);
		hello = cron.schedulerSimple();
		dailyTalk = HubotDailyTalk.createSchedule("* * * * *", "http://localhost:8080", "shell");
		dailyTalk.start();

		loadPropertyFile("test.properties");
		AppProperties.init();
		AppProperties.loadSystemProperties();
	}

	public void contextDestroyed(ServletContextEvent event){
		logger.addSingleLog("Server shutdown.", true);
		hello.stop();
		dailyTalk.stop();
	}
}
