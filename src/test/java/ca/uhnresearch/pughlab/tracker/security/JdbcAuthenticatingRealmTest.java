package ca.uhnresearch.pughlab.tracker.security;

import org.junit.Assert;

import javax.sql.DataSource;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/testContextDatabase.xml" })
public class JdbcAuthenticatingRealmTest {
	
	@Autowired
    private DataSource dataSource;
	
	private JdbcAuthenticatingRealm realm;
	
	@Before
	public void setUp() {
		realm = new JdbcAuthenticatingRealm();
		realm.setDataSource(dataSource);
		realm.setCredentialsMatcher(new BcryptPasswordMatcher());
		realm.setAuthenticationQuery("SELECT HASH FROM USERS WHERE USERNAME = LOWER(?)");
	}

	@Test
	public void testBasicAuthentication() {
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername("stuart");
		token.setPassword("stuart".toCharArray());
		
		AuthenticationInfo auth = realm.getAuthenticationInfo(token);
		Assert.assertNotNull(auth);	
	}
	
}
