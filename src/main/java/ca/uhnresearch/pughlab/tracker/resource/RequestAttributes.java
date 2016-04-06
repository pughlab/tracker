package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.Request;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudyAboutResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class RequestAttributes {
	
	private static final String STUDY_ATTRIBUTE = "study";
	
	private static final String VIEW_ATTRIBUTE = "view";

	private static final String ROLE_ATTRIBUTE = "role";

	private static final String QUERY_ATTRIBUTE = "query";

	private static final String PAGER_ATTRIBUTE = "pager";

	private static final String ENTITY_ATTRIBUTE = "entity";

	private static final String ATTRIBUTE_ATTRIBUTE = "attribute";

	private static final String FILTER_ATTRIBUTE = "filter";

	private static final String ABOUT_ATTRIBUTE = "about";
	
	private static final String USER_ATTRIBUTE = "user";

	/**
	 * Helper method to read a study from a request attribute
	 * @param request
	 * @return the request study
	 */
	public static Study getRequestStudy(Request request) {
		return (Study) request.getAttributes().get(RequestAttributes.STUDY_ATTRIBUTE);
	}

	/**
	 * Helper method to write a study into a request attribute
	 * @param request
	 * @param s
	 */
	public static void setRequestStudy(Request request, Study s) {
		request.getAttributes().put(RequestAttributes.STUDY_ATTRIBUTE, s);
	}

	/**
	 * Helper method to read a view from a request attribute
	 * @param request
	 * @return the request view
	 */
	public static View getRequestView(Request request) {
		return (View) request.getAttributes().get(RequestAttributes.VIEW_ATTRIBUTE);
	}

	/**
	 * Helper method to write a view into a request attribute
	 * @param request
	 * @param v
	 */
	public static void setRequestView(Request request, View v) {
		request.getAttributes().put(RequestAttributes.VIEW_ATTRIBUTE, v);
	}

	/**
	 * Helper method to read a role from a request attribute
	 * @param request
	 * @return the request role
	 */
	public static Role getRequestRole(Request request) {
		return (Role) request.getAttributes().get(RequestAttributes.ROLE_ATTRIBUTE);
	}

	/**
	 * Helper method to write a role into a request attribute
	 * @param request
	 * @param r
	 */
	public static void setRequestRole(Request request, Role r) {
		request.getAttributes().put(RequestAttributes.ROLE_ATTRIBUTE, r);
	}

	/**
	 * Helper method to read a query from a request attribute
	 * @param request
	 * @return the request query
	 */
	public static StudyCaseQuery getRequestCaseQuery(Request request) {
		return (StudyCaseQuery) request.getAttributes().get(RequestAttributes.QUERY_ATTRIBUTE);
	}

	/**
	 * Helper method to write a query into a request attribute
	 * @param request
	 * @param q
	 */
	public static void setRequestCaseQuery(Request request, StudyCaseQuery q) {
		request.getAttributes().put(RequestAttributes.QUERY_ATTRIBUTE, q);
	}

	/**
	 * Helper method to read a pager from a request attribute
	 * @param request
	 * @return the request pager
	 */
	public static CasePager getRequestCasePager(Request request) {
		return (CasePager) request.getAttributes().get(RequestAttributes.PAGER_ATTRIBUTE);
	}

	/**
	 * Helper method to write a pager into a request attribute
	 * @param request
	 * @param q
	 */
	public static void setRequestCasePager(Request request, CasePager q) {
		request.getAttributes().put(RequestAttributes.PAGER_ATTRIBUTE, q);
	}

	/**
	 * Helper method to read an entity from a request attribute
	 * @param request
	 * @return the request case
	 */
	public static Cases getRequestEntity(Request request) {
		return (Cases) request.getAttributes().get(RequestAttributes.ENTITY_ATTRIBUTE);
	}

	/**
	 * Helper method to write an entity into a request attribute
	 * @param request
	 * @param c
	 */
	public static void setRequestEntity(Request request, Cases c) {
		request.getAttributes().put(RequestAttributes.ENTITY_ATTRIBUTE, c);
	}

	/**
	 * Helper method to read an attribute from a request attribute
	 * @param request
	 * @return the request attribute
	 */
	public static Attributes getRequestAttribute(Request request) {
		return (Attributes) request.getAttributes().get(RequestAttributes.ATTRIBUTE_ATTRIBUTE);
	}

	/**
	 * Helper method to write an attribute into a request attribute
	 * @param request
	 * @param a
	 */
	public static void setRequestAttribute(Request request, Attributes a) {
		request.getAttributes().put(RequestAttributes.ATTRIBUTE_ATTRIBUTE, a);
	}

	/**
	 * Helper method to read an attribute from a request attribute
	 * @param request
	 * @return the request filter
	 */
	public static ObjectNode getRequestFilter(Request request) {
		return (ObjectNode) request.getAttributes().get(RequestAttributes.FILTER_ATTRIBUTE);
	}

	/**
	 * Helper method to write an attribute into a request attribute
	 * @param request
	 * @param a
	 */
	public static void setRequestFilter(Request request, ObjectNode a) {
		request.getAttributes().put(RequestAttributes.FILTER_ATTRIBUTE, a);
	}

	/**
	 * Helper method to read an attribute from a request attribute
	 * @param request
	 * @return the request filter
	 */
	public static StudyAboutResponse getRequestStudyAbout(Request request) {
		return (StudyAboutResponse) request.getAttributes().get(RequestAttributes.ABOUT_ATTRIBUTE);
	}

	/**
	 * Helper method to write an attribute into a request attribute
	 * @param request
	 * @param a
	 */
	public static void setRequestStudyAbout(Request request, StudyAboutResponse a) {
		request.getAttributes().put(RequestAttributes.ABOUT_ATTRIBUTE, a);
	}

	/**
	 * Helper method to read a user from a request attribute
	 * @param request
	 * @return the request filter
	 */
	public static User getRequestUser(Request request) {
		return (User) request.getAttributes().get(RequestAttributes.USER_ATTRIBUTE);
	}

	/**
	 * Helper method to write a user into a request attribute
	 * @param request
	 * @param a
	 */
	public static void setRequestUser(Request request, User a) {
		request.getAttributes().put(RequestAttributes.USER_ATTRIBUTE, a);
	}
}
