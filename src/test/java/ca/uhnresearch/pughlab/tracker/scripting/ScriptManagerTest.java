package ca.uhnresearch.pughlab.tracker.scripting;

import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;

import org.quartz.JobExecutionException;
import org.springframework.core.io.Resource;

public class ScriptManagerTest {

	/**
	 * Checks successful instantiation
	 * @throws IOException
	 */
	@Test
	public void testSuccessfulInstantiation() throws IOException {
		
		String inputString = "";
		
		InputStream input = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
		
		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubReturn(input);
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		Assert.assertNotNull(manager);
	}

	/**
	 * Checks that even when the resource open fails, we get a script manager.
	 * @throws IOException
	 */
	@Test
	public void testFailingInstantiation() throws IOException {
		
		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubThrow(new IOException("Failed to open resource"));
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		Assert.assertNotNull(manager);
	}

	/**
	 * Checks that even when the resource open fails, we get a script manager.
	 * @throws IOException
	 */
	@Test
	public void testFailingScript() throws IOException {
		
		String inputString = "function foo() { ";
		
		InputStream input = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));

		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubReturn(input);
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		Assert.assertNotNull(manager);
	}
	
	/**
	 * Checks successful instantiation
	 * @throws IOException
	 */
	@Test
	public void testBindings() throws IOException {
		
		String inputString = "test = test + 1; console.log('Value should be 123:', test);";
		
		InputStream input = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
		
		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubReturn(input);
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("test", 122);
		bindings.put("console", new JSLogger());
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		Assert.assertNotNull(manager);
	}

	/**
	 * Checks successful instantiation
	 * @throws IOException
	 * @throws JobExecutionException 
	 */
	@Test
	public void testExecute() throws IOException, JobExecutionException {
		
		String inputString = "";
		
		InputStream input = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
		
		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubReturn(input);
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("test", 122);
		bindings.put("console", new JSLogger());
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		manager.execute();
	}

	/**
	 * Checks successful execution of later values
	 * @throws IOException
	 * @throws JobExecutionException 
	 */
	@Test
	public void testExecuteString() throws IOException, JobExecutionException {
		
		String inputString = "";
		
		InputStream input = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
		
		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubReturn(input);
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("test", 122);
		bindings.put("console", new JSLogger());
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		
		String testString = "test = test + 1; console.log('Value should be 123:', test); test + 2;";
		Object result = manager.evaluateString(testString, manager.getContext());
		
		Assert.assertEquals(125.0, result);
	}

	/**
	 * Checks successful execution of later values
	 * @throws IOException
	 * @throws JobExecutionException 
	 */
	@Test
	public void testExecuteStringContext() throws IOException, JobExecutionException {
		
		String inputString = "";
		
		InputStream input = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
		
		Resource resource = createMock(Resource.class);
		expect(resource.getInputStream()).andStubReturn(input);
		expect(resource.getURL()).andStubReturn(new URL("file:///"));
		replay(resource);
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("test", 122);
		bindings.put("console", new JSLogger());
		
		ScriptManager manager = new ScriptManager(resource, bindings);
		
		String testString = "test2 = test2 + 1; console.log('Value should be 123:', test2); test2 + 2;";
		Map<String, Object> merge = new HashMap<String, Object>();
		merge.put("test2", 678);
		ScriptContext updated = manager.withContext(merge);
		Object result = manager.evaluateString(testString, updated);
		
		Assert.assertEquals(681.0, result);
	}
}
