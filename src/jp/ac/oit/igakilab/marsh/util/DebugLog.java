package jp.ac.oit.igakilab.marsh.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DebugLog {
	public static final int LS_INFO = 101;
	public static final int LS_WARN = 102;
	public static final int LS_ERROR = 103;
	public static final int LS_EXCEPTION = 104;
	public static final int LS_FATAL = 105;

	public static String LOG_DIR = "LabMemberManager/logs/";
	public static String DEFAULT_NAME = "default";

	public static String getSubjectString(int code){
		switch( code ){
			case LS_INFO: return "<INFO>";
			case LS_WARN: return "<WARN>";
			case LS_ERROR: return "<ERROR>";
			case LS_EXCEPTION: return "<EXCEPTION>";
			case LS_FATAL: return "<FATAL>";
			default: return "";
		}
	}

	public static boolean createDirectory(){
		File dir = new File(LOG_DIR);
		if( !dir.exists() ){
			return dir.mkdirs();
		}else{
			return true;
		}
	}

	public static String getLogFileName(String mname, Date date){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return "logs_" + mname + "_" + df.format(date) + ".txt";
	}

	public static void writeLog(String mname, String msg){
		String file_path = LOG_DIR + getLogFileName(mname, Calendar.getInstance().getTime());
		createDirectory();
		LogRecorder recorder = new LogRecorder(file_path, true);
		recorder.addSingleLog(msg, true);
	}


	public static void logm(String module, int type, String func, String msg){
		String write_msg = func + ": " + getSubjectString(type) + " " + msg;
		writeLog(module, write_msg);
	}

	public static void logm(String module, int type, String msg){
		String write_msg = getSubjectString(type) + " " + msg;
		writeLog(module, write_msg);
	}

	public static void logm(String module, String func, String msg){
		String write_msg = func + ": " + msg;
		writeLog(module, write_msg);
	}

	public static void logm(String module, String msg){
		writeLog(module, msg);
	}

	public static void logs(int type, String func, String msg){
		logm(DEFAULT_NAME, type, func, msg);
	}

	public static void logs(int type, String msg){
		logm(DEFAULT_NAME, type, msg);
	}

	public static void logs(String func, String msg){
		logm(DEFAULT_NAME, func, msg);
	}

	public static void logs(String msg){
		logm(DEFAULT_NAME, msg);
	}

	public static void out(String msg){
		logs(msg);
	}

//インスタンス化
	String module_name;

	public DebugLog(String m0){
		module_name = m0;
	}

	public void log(int type, String func, String msg){
		logm(module_name, type, func, msg);
	}

	public void log(int type, String msg){
		logm(module_name, type, msg);
	}

	public void log(String func, String msg){
		logm(module_name, func, msg);
	}

	public void log(String msg){
		logm(module_name, msg);
	}
}
