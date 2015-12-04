package ca.uhnresearch.pughlab.tracker.security;

import javax.sql.DataSource;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

// import static org.easymock.EasyMock.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:**/testContextDatabase.xml", "classpath:**/testContextSecurity.xml" })
public class JdbcAuthorizingRealmTest {

	@Autowired
    private DataSource dataSource;
	
	@Autowired
	private JdbcAuthorizingRealm authorizationRealm;
	
	@Test
	public void testSupports() {
		AuthenticationToken token = new UsernamePasswordToken();
		Assert.assertFalse(authorizationRealm.supports(token));
	}

	@Test
	public void testAuthorization() {
		PrincipalCollection principal = new SimplePrincipalCollection();
		Object result = ReflectionTestUtils.invokeMethod(authorizationRealm, "doGetAuthorizationInfo", principal);
		Assert.assertTrue(result instanceof AuthorizationInfo);
	}

	@Test
	public void testAuthorizationWithPrincipal() {
		PrincipalCollection principal = new SimplePrincipalCollection("morungos@gmail.com", "mock");
		Object result = ReflectionTestUtils.invokeMethod(authorizationRealm, "doGetAuthorizationInfo", principal);
		Assert.assertTrue(result instanceof AuthorizationInfo);
		
		AuthorizationInfo info = (AuthorizationInfo) result;
		
		Assert.assertTrue(info.getRoles().contains("ROLE_ADMIN"));
		Assert.assertEquals(1, info.getRoles().size());
		
		Assert.assertTrue(info.getStringPermissions().contains("*"));
		Assert.assertEquals(1, info.getStringPermissions().size());
	}

	@Test
	public void testAuthorizationWithNonAdminPrincipal() {
		PrincipalCollection principal = new SimplePrincipalCollection("anca", "mock");
		Object result = ReflectionTestUtils.invokeMethod(authorizationRealm, "doGetAuthorizationInfo", principal);
		Assert.assertTrue(result instanceof AuthorizationInfo);
		
		AuthorizationInfo info = (AuthorizationInfo) result;
		
		Assert.assertTrue(info.getRoles().contains("ROLE_DEMO_TRACK"));
		Assert.assertEquals(1, info.getRoles().size());
		
		Assert.assertTrue(info.getStringPermissions().contains("DEMO:view"));
		Assert.assertTrue(info.getStringPermissions().contains("DEMO:read:track"));
		Assert.assertTrue(info.getStringPermissions().contains("DEMO:write:track"));
		Assert.assertTrue(info.getStringPermissions().contains("DEMO:attribute:*:*"));
		Assert.assertEquals(4, info.getStringPermissions().size());
	}
}
