package ca.uhnresearch.pughlab.tracker.sockets;

import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerSocketEventListener implements AtmosphereResourceEventListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onHeartbeat(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onHeartbeat: {}", arg0);
	}

	@Override
	public void onBroadcast(AtmosphereResourceEvent arg0) {
		
		AtmosphereResource r = arg0.getResource();
        Subject subject = (Subject) r.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
        logger.info("Subject: {}", subject.getPrincipal());

        Object data = arg0.getMessage();
		logger.info("Called onBroadcast: {}", data.toString());

	}

	@Override
	public void onClose(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onClose: {}", arg0);

	}

	@Override
	public void onDisconnect(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onDisconnect: {}", arg0);

	}

	@Override
	public void onPreSuspend(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onPreSuspend: {}", arg0);

	}

	@Override
	public void onResume(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onResume: {}", arg0);

	}

	@Override
	public void onSuspend(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onSuspend: {}", arg0);

	}

	@Override
	public void onThrowable(AtmosphereResourceEvent arg0) {
		// TODO Auto-generated method stub
		logger.info("Called onThrowable: {}", arg0);

	}
}
