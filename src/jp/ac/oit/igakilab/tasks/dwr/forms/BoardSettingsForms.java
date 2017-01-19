package jp.ac.oit.igakilab.tasks.dwr.forms;

import java.util.Date;

public class BoardSettingsForms {
	public static class Info{
		private Date lastUpdate;
		private boolean slackNotifyEnabled;
		private Integer slackMeetingNotifyHour;

		public Info(){
			lastUpdate = null;
			slackNotifyEnabled = false;
		}

		public Date getLastUpdate() {
			return lastUpdate;
		}

		public void setLastUpdate(Date lastUpdate) {
			this.lastUpdate = lastUpdate;
		}

		public boolean isSlackNotifyEnabled() {
			return slackNotifyEnabled;
		}

		public void setSlackNotifyEnabled(boolean slackNotifyEnabled) {
			this.slackNotifyEnabled = slackNotifyEnabled;
		}

		public Integer getSlackMeetingNotifyHour() {
			return slackMeetingNotifyHour;
		}

		public void setSlackMeetingNotifyHour(Integer slackMeetingNotifyHour) {
			this.slackMeetingNotifyHour = slackMeetingNotifyHour;
		}
	}
}
