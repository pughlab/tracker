package ca.uhnresearch.pughlab.tracker.filter;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public class TransactionFilterTest {
	
	TransactionFilter filter;
	
	@Before
	public void initialize() {
		filter = new TransactionFilter();
	}

	@Test
	public void testTransactionFilter() throws IOException, ServletException {
		
		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);
		FilterChain filterChain = createMock(FilterChain.class);
		
		filterChain.doFilter(request, response);
		expectLastCall();

		replay(request);
		replay(response);
		replay(filterChain);
		
		TransactionStatus txStatus = createMock(TransactionStatus.class);
		replay(txStatus);
		
		PlatformTransactionManager txManager = createMock(PlatformTransactionManager.class);
		expect(txManager.getTransaction(anyObject(TransactionDefinition.class))).andStubReturn(txStatus);
		txManager.rollback(txStatus);
		expectLastCall();
		txManager.commit(txStatus);
		expectLastCall();
		replay(txManager);

		filter.setTransactionManager(txManager);
		
		filter.doFilter(request, response, filterChain);
		
		verify();
	}

	@Test
	public void testTransactionFilterErrorTest() throws IOException, ServletException {
		
		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);
		FilterChain filterChain = createMock(FilterChain.class);
		
		filterChain.doFilter(request, response);
		expectLastCall().andThrow(new RuntimeException("RuntimeException happened"));

		replay(request);
		replay(response);
		replay(filterChain);
		
		TransactionStatus txStatus = createMock(TransactionStatus.class);
		txStatus.setRollbackOnly();
		expectLastCall();
		replay(txStatus);
		
		PlatformTransactionManager txManager = createMock(PlatformTransactionManager.class);
		expect(txManager.getTransaction(anyObject(TransactionDefinition.class))).andStubReturn(txStatus);
		txManager.rollback(txStatus);
		expectLastCall();
		txManager.commit(txStatus);
		expectLastCall();
		replay(txManager);

		filter.setTransactionManager(txManager);
		
		filter.doFilter(request, response, filterChain);

		verify();
	}

	/**
	 * Checks that after a deadlock, the filter will be retried.
	 * @throws IOException
	 * @throws ServletException
	 */
	@Test
	public void testTransactionFilterDeadlockTest() throws IOException, ServletException {
		
		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);
		FilterChain filterChain = createMock(FilterChain.class);
		
		filterChain.doFilter(request, response);
		expectLastCall().andThrow(new ConcurrencyFailureException("ConcurrencyFailureException happened"));
		
		filterChain.doFilter(request, response);
		expectLastCall();

		replay(request);
		replay(response);
		replay(filterChain);
		
		TransactionStatus txStatus1 = createMock(TransactionStatus.class);
		replay(txStatus1);
		
		TransactionStatus txStatus2 = createMock(TransactionStatus.class);
		replay(txStatus2);

		PlatformTransactionManager txManager = createMock(PlatformTransactionManager.class);
		expect(txManager.getTransaction(anyObject(TransactionDefinition.class))).andReturn(txStatus1).once();
		expect(txManager.getTransaction(anyObject(TransactionDefinition.class))).andReturn(txStatus2).once();
		
		txManager.rollback(txStatus1);
		expectLastCall().atLeastOnce();
		txManager.commit(txStatus2);
		expectLastCall();
		replay(txManager);

		filter.setTransactionManager(txManager);
		
		filter.doFilter(request, response, filterChain);
		
		verify(txManager, txStatus1, txStatus2);
	}

	/**
	 * Checks that after a deadlock, the filter will be retried.
	 * @throws IOException
	 * @throws ServletException
	 */
	@Test
	public void testTransactionFilterServletExceptionTest() throws IOException, ServletException {
		
		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);
		FilterChain filterChain = createMock(FilterChain.class);
		
		filterChain.doFilter(request, response);
		expectLastCall().andThrow(new ServletException("ServletException happened"));
		
		filterChain.doFilter(request, response);
		expectLastCall();

		replay(request);
		replay(response);
		replay(filterChain);
		
		TransactionStatus txStatus1 = createMock(TransactionStatus.class);
		txStatus1.setRollbackOnly();
		expectLastCall().once();
		replay(txStatus1);
		
		PlatformTransactionManager txManager = createMock(PlatformTransactionManager.class);
		expect(txManager.getTransaction(anyObject(TransactionDefinition.class))).andReturn(txStatus1).once();
		
		txManager.commit(txStatus1);
		expectLastCall().atLeastOnce();		
		replay(txManager);

		filter.setTransactionManager(txManager);
		
		filter.doFilter(request, response, filterChain);
		
		verify(txManager, txStatus1);
	}

	@Test
	public void testTransactionFilterDestroy() {
		filter.destroy();
	}
		

	@Test
	public void testTransactionFilterInitialize() throws ServletException {
		FilterConfig filterConfig = createMock(FilterConfig.class);
		filter.init(filterConfig);
	}
		
	@Test
	public void testTransactionFilterGetManager() throws ServletException {
		PlatformTransactionManager txManager = createMock(PlatformTransactionManager.class);
		replay(txManager);

		filter.setTransactionManager(txManager);
		
		assertEquals(txManager, filter.getTransactionManager());
		verify();
	}
		
}
