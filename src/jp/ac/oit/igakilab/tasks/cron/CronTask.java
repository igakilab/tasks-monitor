package jp.ac.oit.igakilab.tasks.cron;

import it.sauronsoftware.cron4j.Scheduler;
import jp.ac.oit.igakilab.marsh.util.DebugLog;

public class CronTask implements Runnable{
	public static void main(String[] args){
		Scheduler s = createScheduler("* * * * *");
		s.start();
	}

	public static Scheduler createScheduler(String schedule){
		return createScheduler(schedule, new CronTask());
	}

	public static Scheduler createScheduler(String schedule, CronTask task){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, task);
		return scheduler;
	}


	private DebugLog logger;
	protected String taskName;

	public CronTask(){
		logger = new DebugLog("cronlog");
		taskName = "CronTask";
	}

	protected void log(int type, String msg){
		if( logger != null ){
			logger.log(type, taskName, msg);
		}
	}

	@Override
	public void run(){
		log(DebugLog.LS_INFO, "CRON TASK TRIGGERED");
		execute();
		log(DebugLog.LS_INFO, "CRON TASK FINISHED");
	}

	public void execute(){
		System.out.println("hello cron4j!");
	}
}
