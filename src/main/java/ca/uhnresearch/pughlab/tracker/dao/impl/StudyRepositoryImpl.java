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
import static ca.uhnresearch.pughlab.tracker.domain.QViews.views;

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

    /**
     * Returns a named study
     * @param study
     * @return
     */
	public Studies getStudy(String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(studies).where(studies.name.eq(name));
    	Studies study = template.queryForObject(sqlQuery, studies);
    	
    	if (study != null) {
    		logger.info("Got a study: {}", study.toString());
    	} else {
    		logger.info("No study found");
    	}
    	
    	return study;
	}

    /**
     * Returns the list of views associated with a study
     * @param study
     * @return
     */
	public List<Views> getStudyViews(Studies study) {
		logger.debug("Looking for views for study: {}", study.getName());
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.studyId.eq(study.getId()));
    	List<Views> viewList = template.query(sqlQuery, views);
    	logger.info("Got some views: {}", viewList.toString());

		return viewList;
	}

    /**
     * Returns the named view associated with a study
     * @param study
     * @return
     */
	public Views getStudyView(Studies study, String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.name.eq(name).and(views.studyId.eq(study.getId())));
    	Views view = template.queryForObject(sqlQuery, views);
    	
    	if (views != null) {
    		logger.info("Got a view: {}", views.toString());
    	} else {
    		logger.info("No study found");
    	}
    	
    	return view;
	}
}