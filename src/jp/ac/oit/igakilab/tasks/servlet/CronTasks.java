package jp.ac.oit.igakilab.tasks.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jp.ac.oit.igakilab.marsh.util.LogRecorder;
import jp.ac.oit.igakilab.tasks.cron.HubotDailyTalk;
import jp.ac.oit.igakilab.tasks.cron.SampleCron;

public class CronTasks implements ServletContextListener{
	LogRecorder logger;
	SampleCron cron;

	public CronTasks(){
		logger = new LogRecorder("cron-test.txt", true);
		cron = new SampleCron();
	}

	public void contextInitialized(ServletContextEvent event){
		logger.addSingleLog("Cron Initialization.", true);
		cron.schedulerSimple();
		HubotDailyTalk.createSchedule("* * * * *", "http://localhost:8080", "shell").start();
	}

	public void contextDestroyed(ServletContextEvent event){
		logger.addSingleLog("Server shutdown.", true);
	}
}
