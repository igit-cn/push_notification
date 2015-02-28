package com.yidian.push.utils;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class MailTimerEvaluator implements TriggeringEventEvaluator {
	private long minTimeInterval = 10 * 60 * 1000; 
	
	private long last = 0;
	
	@Override
	public boolean isTriggeringEvent(LoggingEvent event) {
		boolean ok = event.getLevel().isGreaterOrEqual(Level.ERROR);
		if (!ok) return false;
		if (event.timeStamp - last > minTimeInterval) {
			last = event.timeStamp;
			return true;
		} else {
			return false;
		}
	}

	public void setMinTimeInterval(long minTimeInterval) {
		this.minTimeInterval = minTimeInterval;
	}
}
