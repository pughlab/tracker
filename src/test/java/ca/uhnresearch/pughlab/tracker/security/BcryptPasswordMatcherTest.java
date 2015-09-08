package ca.uhnresearch.pughlab.tracker.security;

import static org.easymock.EasyMock.*;
import org.junit.Assert;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.junit.Before;
import org.junit.Test;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;

public class BcryptPasswordMatcherTest {
	
	private CredentialsMatcher matcher;
	
	private UsernamePasswordToken token;
	
	@Before
	public void initialize() {
		matcher = new BcryptPasswordMatcher();

		token = createMock(UsernamePasswordToken.class);
		String password = "archaeopteryx";
		char passwordArray[] = password.toCharArray();
		expect(token.getPassword()).andStubReturn(passwordArray);
		replay(token);
}

	@Test
	public void testSuccess() {
		String credentials = "$2y$11$mCd3GelBmDBE8M4vk1buyeMc1Jq8n8pEJb.Wo20T0G7o/XySvmT4.";
		char credentialsArray[] = credentials.toCharArray();

		AuthenticationInfo info = createMock(AuthenticationInfo.class);
		expect(info.getCredentials()).andStubReturn(credentialsArray);
		replay(info);
		
		Assert.assertTrue(matcher.doCredentialsMatch(token,  info));
		verify(token);
		verify(info);
	}

	@Test
	public void testFailure() {
		
		String credentials = "$2y$11$mCd3GelBmDBE8M4vk1buyeMc1Jq8n8pEJb.Wo20T0G7o/XySvmT3.";
		char credentialsArray[] = credentials.toCharArray();

		AuthenticationInfo info = createMock(AuthenticationInfo.class);
		expect(info.getCredentials()).andStubReturn(credentialsArray);
		replay(info);
		
		Assert.assertFalse(matcher.doCredentialsMatch(token,  info));		
		verify(token);
		verify(info);
	}
}
