package jp.ac.oit.igakilab.tasks.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jp.ac.oit.igakilab.marsh.util.LogRecorder;

public class CronJob implements ServletContextListener{
	public String text = "fleshly";

	public void contextInitialized(ServletContextEvent event){
		System.out.println("initialization!");
    	LogRecorder logger = new LogRecorder("init-test.txt", true);
    	logger.addSingleLog("initialization!", true);
	}

	public void contextDestroyed(ServletContextEvent event){
		System.out.println("destroyed!");
	}
}
