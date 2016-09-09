package jp.ac.oit.igakilab.tasks.cron;

import java.util.Date;

import it.sauronsoftware.cron4j.Scheduler;

public class SampleCron {
	static public class CronTask implements Runnable {
		@Override
		public void run(){
			System.out.println(new Date() + " : Hello cron4j");
		}
	}

	public static void main(String[] args){
		SampleCron app = new SampleCron();
		try{
			app.schedulerSimple();
			System.out.println("Press Ctrl+C to stop.");
			Thread.sleep(100000000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	public Scheduler schedulerSimple(){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule("* * * * *", new CronTask()); //every minute
		scheduler.start();
		return scheduler;
	}
}
