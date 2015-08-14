package ca.uhnresearch.pughlab.tracker.security;

import javax.sql.DataSource;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/testContextDatabase.xml" })
public class JdbcAuthorizingRealmTest {

	@Autowired
    private DataSource dataSource;
	
	private JdbcAuthorizingRealm realm;
	
	@Before
	public void setUp() {
		realm = new JdbcAuthorizingRealm();
		realm.setDataSource(dataSource);
	}

	@Test
	public void testSupports() {
		AuthenticationToken token = new UsernamePasswordToken();
		Assert.assertFalse(realm.supports(token));
	}

}
