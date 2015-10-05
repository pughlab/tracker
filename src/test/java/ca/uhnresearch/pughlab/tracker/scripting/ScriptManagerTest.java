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
}
