package ca.uhnresearch.pughlab.tracker.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.quartz.JobExecutionException;

public class ScheduledJob  {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	ScheduledJob() {
		logger.info("Initializing scheduled job");
	}
	
    protected void execute() throws JobExecutionException {
        logger.info("Scheduled ping");
    }
}
