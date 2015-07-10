package ca.uhnresearch.pughlab.tracker.scheduling;

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
import org.quartz.JobExecutionException;

public class ScheduledJob  {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ScriptContext context;
	
	ScheduledJob() {
		
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("tracker.js");
		if (in == null) {
			return;
		}
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("javascript");
		engine.setBindings(getInitialBindings(engine), ScriptContext.ENGINE_SCOPE);
		context = engine.getContext();
		
		logger.debug("Initializing scheduled job");

		Reader reader = new InputStreamReader(in);
		
		try {
			Object obj = engine.eval(reader, context);
			
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
}
