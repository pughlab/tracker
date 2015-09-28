package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.Request;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class RequestAttributes {
	
	private static final String STUDY_ATTRIBUTE = "study";
	
	private static final String VIEW_ATTRIBUTE = "view";

	private static final String ROLE_ATTRIBUTE = "role";

	private static final String QUERY_ATTRIBUTE = "query";

	private static final String ENTITY_ATTRIBUTE = "entity";

	private static final String ATTRIBUTE_ATTRIBUTE = "attribute";

	/**
	 * Helper method to read a study from a request attribute
	 * @param request
	 * @return
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
	 * @return
	 */
	public static View getRequestView(Request request) {
		return (View) request.getAttributes().get(RequestAttributes.VIEW_ATTRIBUTE);
	}

	/**
	 * Helper method to write a view into a request attribute
	 * @param request
	 * @param s
	 */
	public static void setRequestView(Request request, View v) {
		request.getAttributes().put(RequestAttributes.VIEW_ATTRIBUTE, v);
	}

	/**
	 * Helper method to read a role from a request attribute
	 * @param request
	 * @return
	 */
	public static Role getRequestRole(Request request) {
		return (Role) request.getAttributes().get(RequestAttributes.ROLE_ATTRIBUTE);
	}

	/**
	 * Helper method to write a role into a request attribute
	 * @param request
	 * @param s
	 */
	public static void setRequestRole(Request request, Role r) {
		request.getAttributes().put(RequestAttributes.ROLE_ATTRIBUTE, r);
	}

	/**
	 * Helper method to read a query from a request attribute
	 * @param request
	 * @return
	 */
	public static CaseQuery getRequestCaseQuery(Request request) {
		return (CaseQuery) request.getAttributes().get(RequestAttributes.QUERY_ATTRIBUTE);
	}

	/**
	 * Helper method to write a query into a request attribute
	 * @param request
	 * @param s
	 */
	public static void setRequestCaseQuery(Request request, CaseQuery q) {
		request.getAttributes().put(RequestAttributes.QUERY_ATTRIBUTE, q);
	}

	/**
	 * Helper method to read an entity from a request attribute
	 * @param request
	 * @return
	 */
	public static Cases getRequestEntity(Request request) {
		return (Cases) request.getAttributes().get(RequestAttributes.ENTITY_ATTRIBUTE);
	}

	/**
	 * Helper method to write an entity into a request attribute
	 * @param request
	 * @param s
	 */
	public static void setRequestEntity(Request request, Cases c) {
		request.getAttributes().put(RequestAttributes.ENTITY_ATTRIBUTE, c);
	}

	/**
	 * Helper method to read an attribute from a request attribute
	 * @param request
	 * @return
	 */
	public static Attributes getRequestAttribute(Request request) {
		return (Attributes) request.getAttributes().get(RequestAttributes.ATTRIBUTE_ATTRIBUTE);
	}

	/**
	 * Helper method to write an attribute into a request attribute
	 * @param request
	 * @param s
	 */
	public static void setRequestAttribute(Request request, Attributes a) {
		request.getAttributes().put(RequestAttributes.ATTRIBUTE_ATTRIBUTE, a);
	}
}