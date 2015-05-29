package ca.uhnresearch.pughlab.tracker.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.scheduling.quartz.QuartzJobBean;

public class ScheduledJob extends QuartzJobBean {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        logger.info("Scheduled ping");
    }
}
