package jp.ac.oit.igakilab.tasks.sprints;

import java.util.Calendar;
import java.util.Date;

public class Sprint {
	public static Calendar roundDate(Date d0){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d0);
		Calendar ncal = Calendar.getInstance();
		ncal.clear();
		ncal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		ncal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		ncal.set(Calendar.DATE, cal.get(Calendar.DATE));
		return ncal;
	}


	private Date beginDate;
	private Date finishDate;

	public Sprint(){
		beginDate = null;
		finishDate = null;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = roundDate(beginDate).getTime();
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = roundDate(finishDate).getTime();
	}
}
