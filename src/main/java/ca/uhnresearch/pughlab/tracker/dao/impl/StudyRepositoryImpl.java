package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;

import com.mysema.query.sql.SQLQuery;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.*;
import static ca.uhnresearch.pughlab.tracker.domain.QStudies.studies;

public class StudyRepositoryImpl implements StudyRepository {
	
	private final Logger logger = LoggerFactory.getLogger(StudyRepositoryImpl.class);

	private QueryDslJdbcTemplate template;

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Returns a list of studies
     * @param study
     * @return
     */
    public List<Studies> getAllStudies() {
		logger.debug("Looking for all studies");

    	SQLQuery sqlQuery = template.newSqlQuery().from(studies);
    	List<Studies> studyList = template.query(sqlQuery, studies);
    	logger.info("Got some studies: {}", studyList.toString());

    	return studyList;
    }

}