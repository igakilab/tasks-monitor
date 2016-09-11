package jp.ac.oit.igakilab.tasks.cron;

import java.util.Date;

import it.sauronsoftware.cron4j.Scheduler;

public class SampleCron {
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

	static public class CronTask implements Runnable {
		String message = "Hello cron4j";

		public CronTask(){}

		public CronTask(String msg){
			if( msg != null ) this.message = msg;
		}

		@Override
		public void run(){
			System.out.println(new Date() + " > " + message);
		}
	}

	private Scheduler scheduler;

	public SampleCron(){
		scheduler = new Scheduler();
	}

	public Scheduler schedulerSimple(){
		scheduler.schedule("* * * * *", new CronTask()); //every minute
		scheduler.start();
		return scheduler;
	}

	public Scheduler schedule(String schedule, String msg){
		scheduler.schedule(schedule, new CronTask(msg));
		return scheduler;
	}

	public void start(){
		scheduler.start();
	}

	public void stop(){
		scheduler.stop();
	}
}
