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
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionFilter implements Filter {
	
	private PlatformTransactionManager transactionManager;
	
	private final Logger logger = LoggerFactory.getLogger(TransactionFilter.class);
	
	private int retryLimit = 3;

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void destroy() {
		logger.trace("Destroying TransactionFilter");
	}
	
	private void tryFilter(TransactionTemplate template, final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws TransactionException {
		TransactionCallbackWithoutResult cb = new TransactionCallbackWithoutResult() {

			@Override
			public void doInTransactionWithoutResult(TransactionStatus ts) {
		    	logger.trace("Called doInTransaction: {}", request.toString());
                try {
                	filterChain.doFilter(request, response);
                } catch (IOException e) {
                    logger.error("Transaction set to rollback due to exception: {}", e.getMessage());
                    ts.setRollbackOnly();
                } catch (ServletException e) {
                    logger.error("Transaction set to rollback due to exception: {}", e.getMessage());
                    ts.setRollbackOnly();
                }
			}
		};
		
		template.execute(cb);
	}
	
	public void doFilter(final ServletRequest request, 
			             final ServletResponse response,
						 final FilterChain filterChain) 
		throws IOException, ServletException {
		logger.trace("Called doFilter: {}", request.toString());
		int currentRetry = 0;
		
		TransactionTemplate template = new TransactionTemplate(transactionManager);

		while(true) {
			
			try {
				tryFilter(template, request, response, filterChain);
			} catch (ConcurrencyFailureException e) {
				if(currentRetry++ < retryLimit) {
					continue;
				} else {
					logger.error("Failed to successfully complete transaction after {} retries: {}", retryLimit, e.getMessage());
				}
			} catch (Exception e) {
				logger.error("Failed to successfully complete transaction: {}", e.getMessage());
			}
			
			return;
			
		}
	}

	public void init(FilterConfig config) throws ServletException {
		logger.trace("Initialising TransactionFilter");
	}

	/**
	 * @return the retryLimit
	 */
	public int getRetryLimit() {
		return retryLimit;
	}

	/**
	 * @param retryLimit the retryLimit to set
	 */
	public void setRetryLimit(int retryLimit) {
		this.retryLimit = retryLimit;
	}

}
