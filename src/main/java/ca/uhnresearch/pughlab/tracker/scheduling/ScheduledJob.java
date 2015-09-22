package ca.uhnresearch.pughlab.tracker.scheduling;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.quartz.JobExecutionException;

public class ScheduledJob  {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String fileUrl = null;
	
	private ScriptContext context;
	
	ScheduledJob() {
		this(new ClassPathResource("tracker.js"));
	}
	
	ScheduledJob(Resource scriptResource) {
		
		InputStream in;
		
		try {
			in = scriptResource.getInputStream();
			logger.info("Loading script resource: {}", scriptResource.getURL().toString());
		} catch (IOException e) {
			logger.warn("Can't find a script resource: {}", scriptResource);
			return;
		}
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("javascript");
		engine.setBindings(getInitialBindings(engine), ScriptContext.ENGINE_SCOPE);
		context = engine.getContext();
		
		logger.debug("Initializing scheduled job");

		Reader reader = new InputStreamReader(in);
		
		try {
			engine.eval(reader, context);
			
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}
	
	public Bindings getInitialBindings(ScriptEngine engine) {
		Bindings bindings = engine.createBindings();
		bindings.put("console", logger);
		return bindings;
	}
	
    protected void execute() throws JobExecutionException {
        // logger.debug("Scheduled ping");
    }

	/**
	 * @return the fileUrl
	 */
	public String getFileUrl() {
		return fileUrl;
	}

	/**
	 * @param fileUrl the fileUrl to set
	 */
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
}
