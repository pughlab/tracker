package ca.uhnresearch.pughlab.tracker.services.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;

public class HtmlWriterImpl extends AbstractXMLWriter implements Writer {

	@Override
	protected void write(Document doc, ViewDataResponse data) {
		Element rootElement = doc.createElement("html");
		doc.appendChild(rootElement);
		
		Element headElement = doc.createElement("head");
		rootElement.appendChild(headElement);
		writeStyles(doc, headElement);

		Element bodyElement = doc.createElement("body");
		rootElement.appendChild(bodyElement);
		
		writeWorksheets(doc, bodyElement, data);
	}

	private void writeWorksheets(Document doc, Element parent, ViewDataResponse data) {
		Element table = doc.createElement("table");
		parent.appendChild(table);
		
		writeTableBody(doc, table, data);
	}

	@Override
	protected Element makeRowElement(Document doc, Element parent) {
		Element start = doc.createElement("tr");
		parent.appendChild(start);
		return start;
	}
	
	@Override
	protected void writeHeaderCell(Document doc, Element parent, String data) {
		Element body = doc.createElement("th");
		body.appendChild(doc.createTextNode(data));
		parent.appendChild(body);
	}

	@Override
	protected void writeStringCell(Document doc, Element parent, String data) {
		Element body = doc.createElement("td");
		body.appendChild(doc.createTextNode(data));
		parent.appendChild(body);
	}
	
	@Override
	protected void writeDateCell(Document doc, Element parent, String value) {
		writeStringCell(doc, parent, value);
	}
	
	@Override
	protected void writeBooleanCell(Document doc, Element parent, Boolean data) {
		writeStringCell(doc, parent, data ? "Yes" : "No");
	}
	
	@Override
	protected void writeNumberCell(Document doc, Element parent, Number data) {
		writeStringCell(doc, parent, data.toString());
	}
	
	@Override
	protected void writeNotAvailableCell(Document doc, Element parent) {
		writeStringCell(doc, parent, "#N/A");
	}
	
	@Override
	protected void writeEmptyCell(Document doc, Element parent) {
		writeStringCell(doc, parent, "");
	}
	
	protected void writeStyles(Document doc, Element parent) {
		return;
	}
}
