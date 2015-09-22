package ca.uhnresearch.pughlab.tracker.services.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
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

	private void writeTableBody(Document doc, Element parent, ViewDataResponse data) {
		Element start = doc.createElement("tr");
		parent.appendChild(start);
		for (Attributes column : data.getAttributes()) {
			writeHeaderCell(doc, start, column.getLabel());
		}
		
		for (JsonNode row : data.getRecords()) {
			Element rowElement = doc.createElement("tr");
			parent.appendChild(rowElement);
			for (Attributes column : data.getAttributes()) {
				JsonNode value = row.get(column.getName());
				if (value == null || value.isMissingNode() || value.isNull()) {
					writeEmptyCell(doc, rowElement);
				} else if (value.isObject() && value.has("$notAvailable")) {
					writeNotAvailableCell(doc, rowElement);
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_STRING) ||
						   column.getType().equals(Attributes.ATTRIBUTE_TYPE_OPTION)) {
					writeStringCell(doc, rowElement, value.asText());
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_BOOLEAN)) {
					writeBooleanCell(doc, rowElement, value.asBoolean());
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_DATE)) {
					writeDateCell(doc, rowElement, value.asText());
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_NUMBER)) {
					writeNumberCell(doc, rowElement, value.numberValue());
				} else {
					throw new RuntimeException("Invalid type: " + column.getType());
				}
			}
		}
	}
	
	private void writeHeaderCell(Document doc, Element parent, String data) {
		Element body = doc.createElement("th");
		body.appendChild(doc.createTextNode(data));
		parent.appendChild(body);
	}

	private void writeStringCell(Document doc, Element parent, String data) {
		Element body = doc.createElement("td");
		body.appendChild(doc.createTextNode(data));
		parent.appendChild(body);
	}
	
	private void writeDateCell(Document doc, Element parent, String value) {
		writeStringCell(doc, parent, value);
	}
	
	private void writeBooleanCell(Document doc, Element parent, Boolean data) {
		writeStringCell(doc, parent, data ? "Yes" : "No");
	}
	
	private void writeNumberCell(Document doc, Element parent, Number data) {
		writeStringCell(doc, parent, data.toString());
	}
	
	private void writeNotAvailableCell(Document doc, Element parent) {
		writeStringCell(doc, parent, "#N/A");
	}
	
	private void writeEmptyCell(Document doc, Element parent) {
		writeStringCell(doc, parent, "");
	}
	
	private void writeStyles(Document doc, Element parent) {
		return;
	}
}
