package ca.uhnresearch.pughlab.tracker.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionFilter implements Filter {
	
	private PlatformTransactionManager transactionManager;
	
	private final Logger logger = LoggerFactory.getLogger(TransactionFilter.class);

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void destroy() {
		logger.info("Destroying TransactionFilter");
	}

	public void doFilter(final ServletRequest request, final ServletResponse response,
						 final FilterChain filterChain) 
		throws IOException, ServletException {
		logger.info("Called doFilter: {}", request.toString());
		
		TransactionCallbackWithoutResult cb = new TransactionCallbackWithoutResult(){
			
		    public void doInTransactionWithoutResult(TransactionStatus ts){
				logger.info("Called doInTransactionWithoutResult: {}", request.toString());
		    	try {
		    		filterChain.doFilter(request, response);
		    	} catch (Exception e) {
		    		logger.error("Transaction rollback due to exception: {}", e.getMessage());
		    		ts.setRollbackOnly();
		    	}
				logger.info("Done doInTransactionWithoutResult", request.toString());
		    }
		};
		
		logger.info("Executing TransactionTemplate");
		new TransactionTemplate(transactionManager).execute(cb);
		logger.info("Completed doFilter");
	}

	public void init(FilterConfig config) throws ServletException {
		logger.info("Initialising TransactionFilter");
	}

}
