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
	}

	@Test
	public void testTransactionFilterErrorTest() throws IOException, ServletException {
		
		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);
		FilterChain filterChain = createMock(FilterChain.class);
		
		filterChain.doFilter(request, response);
		expectLastCall().andThrow(new RuntimeException("Something Bad Happened"));

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
	}
		
}
