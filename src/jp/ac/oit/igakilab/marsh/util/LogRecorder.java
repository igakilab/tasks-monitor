package jp.ac.oit.igakilab.marsh.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogRecorder{
	private static DateFormat gdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static PrintWriter createWriter(String fname, boolean add)
	throws IOException{
		return new PrintWriter(
			new BufferedWriter(
				new FileWriter(fname, add)
			)
		);
	}

	public static void cleanFile(String fname) throws IOException{
		PrintWriter writer = createWriter(fname, false);
		writer.print("");
		writer.close();
	}


	private PrintWriter writer;
	private String fileName;
	private boolean a_mode;

	public LogRecorder(String f0, boolean m0){
		fileName = f0;
		a_mode = m0;
	}

	public void openFile() throws IOException {
		writer = createWriter(fileName, a_mode);
	}

	public void closeFile(){
		writer.close();
		writer = null;
	}

	public boolean isFileOpened(){
		return writer != null;
	}

	public void addLog(String msg, boolean timestamp) throws IOException {
		String str = "";

		if( timestamp ){
			str += "[" + gdf.format(Calendar.getInstance().getTime()) + "]";
		}
		str += " " + msg;

		if( writer != null ){
			writer.println(str);
		}
	}

	public void addSingleLog(String msg, boolean timestamp){
		if( writer != null ){
			try {
				addLog(msg, timestamp);
			}catch(IOException e){
			}
		}else{
			try {
				openFile();
				addLog(msg, timestamp);
				closeFile();
			}catch(IOException e){
				System.err.print(e.getMessage());
			}
		}
	}
}