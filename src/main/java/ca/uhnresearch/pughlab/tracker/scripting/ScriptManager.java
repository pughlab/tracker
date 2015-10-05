package ca.uhnresearch.pughlab.tracker.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class ScriptManager {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Object> initialBindings = new HashMap<String, Object>();
		
	private ScriptContext context;
	
	ScriptManager(Resource scriptResource, Map<String, Object> initialBindings) {
		
		this.initialBindings = initialBindings;
		
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
			logger.error("Syntax error in script: " + e.getLocalizedMessage());
		}
	}
	
	public Bindings getInitialBindings(ScriptEngine engine) {
		Bindings bindings = engine.createBindings();
		for(Map.Entry<String, Object> e : initialBindings.entrySet()) {
			bindings.put(e.getKey(), e.getValue());
		}
		return bindings;
	}
}
