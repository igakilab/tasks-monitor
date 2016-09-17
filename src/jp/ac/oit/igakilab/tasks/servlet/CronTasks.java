package jp.ac.oit.igakilab.tasks.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.LogRecorder;
import jp.ac.oit.igakilab.tasks.cron.HubotTasksNotification;
import jp.ac.oit.igakilab.tasks.cron.UpdateTrelloBoardActions;
import jp.ac.oit.igakilab.tasks.cron.samples.HubotDailyTalk;
import jp.ac.oit.igakilab.tasks.cron.samples.SampleCron;

public class CronTasks implements ServletContextListener{
	LogRecorder logger;
	SampleCron cron;
	Scheduler hello, dailyTalk, updateTrelloBoardActions, tasksNotification;

	public CronTasks(){
		logger = new LogRecorder("cron-test.txt", true);
		cron = new SampleCron();
	}

	public void contextInitialized(ServletContextEvent event){
		logger.addSingleLog("Cron Initialization.", true);
		hello = cron.schedule("* * * * *", null);
		hello.start();
		dailyTalk = HubotDailyTalk.createSchedule("* * * * *", "http://localhost:8090", "shell");
		dailyTalk.start();
		updateTrelloBoardActions = UpdateTrelloBoardActions.createScheduler("* * * * *");
		updateTrelloBoardActions.start();
		tasksNotification = HubotTasksNotification.createScheduler("* * * * *", null);
		tasksNotification.start();
	}

	public void contextDestroyed(ServletContextEvent event){
		logger.addSingleLog("Server shutdown.", true);
		hello.stop();
		dailyTalk.stop();
		updateTrelloBoardActions.stop();
		tasksNotification.stop();
	}
}
