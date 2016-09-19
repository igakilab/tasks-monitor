package jp.ac.oit.igakilab.tasks.cron.samples;

import it.sauronsoftware.cron4j.Scheduler;

public class CronEnglishPhrase implements Runnable{
	public static void main(String[] args){
		createScheduler("* * * * *").start();
	}

	public static Scheduler createScheduler(String schedule){
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(schedule, new CronEnglishPhrase());
		return scheduler;
	}

	private EnglishPhrase phrases;

	public CronEnglishPhrase(){
		phrases = new EnglishPhrase();
	}

	public void run(){
		EnglishPhrase.Entry phrase = phrases.random();
		System.out.println(phrase.getSentence() + "(" + phrase.getTranslation() + ")");
	}
}
