package ca.uhnresearch.pughlab.tracker.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class ScriptManager {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Object> initialBindings = new HashMap<String, Object>();
		
	private ScriptContext context;
	
	private ScriptEngine engine;
	
	ScriptManager(Resource scriptResource, Map<String, Object> initialBindings) {
		
		this.initialBindings = initialBindings;
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		engine = mgr.getEngineByName("javascript");
		engine.setBindings(getInitialBindings(engine), ScriptContext.ENGINE_SCOPE);
		context = engine.getContext();
		
		logger.debug("Initializing scheduled job");
		
		evaluateResource(scriptResource, getContext());
	}
	
	public ScriptContext getContext() {
		return context;
	}
	
	public Object evaluateString(String in, ScriptContext context) {
		return evaluateReader(new StringReader(in), context);
	}
	
	public ScriptContext withContext(Map<String, Object> merge) {
		Bindings bindings = engine.createBindings();
		bindings.putAll(getContext().getBindings(ScriptContext.ENGINE_SCOPE));
		bindings.putAll(merge);
		ScriptContext newContext = new SimpleScriptContext();
		newContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		return newContext;
	}
	
	public Object evaluateInputStream(InputStream in, ScriptContext context) {
		return evaluateReader(new InputStreamReader(in), context);
	}
	
	public Object evaluateReader(Reader reader, ScriptContext context) {
		Object result = null;
		try {
			result = engine.eval(reader, context);
			
		} catch (ScriptException e) {
			logger.error("Syntax error in script: " + e.getLocalizedMessage());
		}

		return result;
	}
	
	public Object evaluateResource(Resource scriptResource, ScriptContext context) {
		InputStream in;
		
		try {
			in = scriptResource.getInputStream();
			logger.info("Loading script resource: {}", scriptResource.getURL().toString());
		} catch (IOException e) {
			logger.warn("Can't find a script resource: {}", scriptResource);
			return null;
		}

		return evaluateInputStream(in, context);
	}
	
	public Bindings getInitialBindings(ScriptEngine engine) {
		Bindings bindings = engine.createBindings();
		for(Map.Entry<String, Object> e : initialBindings.entrySet()) {
			bindings.put(e.getKey(), e.getValue());
		}
		return bindings;
	}

	/**
	 * Called periodically by a scheduled task
	 */
    protected void execute() throws JobExecutionException {
    	// Currently does nothing
    }
}
