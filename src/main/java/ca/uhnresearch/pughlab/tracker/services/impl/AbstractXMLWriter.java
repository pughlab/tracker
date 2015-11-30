package ca.uhnresearch.pughlab.tracker.services.impl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;

public abstract class AbstractXMLWriter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private DocumentBuilderFactory documentBuilderFactory;

	public Document getXMLDocument(ViewDataResponse data) {
		
		Document doc = null;
				
		try {
			documentBuilderFactory.setNamespaceAware(true);			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			doc = builder.newDocument();
						
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Failed to generate XML parser");
		}
		
		logger.debug("Writing XML data");
		
		write(doc, data);
		
		return doc;
	}
	
	/**
	 * @param documentBuilderFactory the documentBuilderFactory to set
	 */
	public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}
	
	private JsonNode getStateLabels(ViewDataResponse data) {
		JsonNode options = data.getStudy().getOptions();
		if (options == null || ! options.isObject() || ! options.has("stateLabels") || ! options.get("stateLabels").isObject()) {
			return NullNode.instance;
		} else {
			return options.get("stateLabels");
		}
	}
	
	private String getAttributeTag(JsonNode path, String attribute) {
		if (! path.has("$notes")) {
			return null;
		}
		path = path.get("$notes");
		if (! path.has(attribute)) {
			return null;
		}
		path = path.get(attribute);
		if (! path.has("tags")) {
			return null;
		}
		path = path.get("tags");
		if (! path.isArray()) {
			return null;
		}
		path = path.get(0);
		if (path.isTextual()) {
			return path.asText();
		} else {
			return null;
		}
	}
	
	/**
	 * Main method to write a table body as a set of rows.
	 * @param doc
	 * @param parent
	 * @param data
	 */
	protected void writeTableBody(Document doc, Element parent, ViewDataResponse data) {
		
		JsonNode stateLabels = getStateLabels(data);
		
		Element start = makeRowElement(doc, parent);
		for (Attributes column : data.getAttributes()) {
			Element headerCell = makeHeaderCellElement(doc, start);
			writeStringCell(doc, headerCell, column.getLabel());
		}
		
		for (JsonNode row : data.getRecords()) {
			Element rowElement = makeRowElement(doc, parent);
			String label = null;
			
			if (row.has("$state")) {
				String state = row.get("$state").asText();
				if (stateLabels.has(state)) {
					label = stateLabels.get(state).asText();
				}
			}
			for (Attributes column : data.getAttributes()) {
				JsonNode value = row.get(column.getName());
				Element cell = makeCellElement(doc, rowElement);
				
				String tag = getAttributeTag(row, column.getName());
				
				labelCell(doc, cell, tag != null ? tag : label);
				
				if (value == null || value.isMissingNode() || value.isNull()) {
					writeEmptyCell(doc, cell);
				} else if (value.isObject() && value.has("$notAvailable")) {
					writeNotAvailableCell(doc, cell);
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_STRING) ||
						   column.getType().equals(Attributes.ATTRIBUTE_TYPE_OPTION)) {
					writeStringCell(doc, cell, value.asText());
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_BOOLEAN)) {
					writeBooleanCell(doc, cell, value.asBoolean());
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_DATE)) {
					writeDateCell(doc, cell, value.asText());
				} else if (column.getType().equals(Attributes.ATTRIBUTE_TYPE_NUMBER)) {
					writeNumberCell(doc, cell, value.numberValue());
				} else {
					throw new RuntimeException("Invalid type: " + column.getType());
				}
			}
		}
	}
	
	protected abstract void write(Document doc, ViewDataResponse data);
	
	protected abstract void writeStyles(Document doc, Element parent);
	
	protected abstract void writeData(Document doc, Element parent, ViewDataResponse data);
		
	protected abstract void writeEmptyCell(Document doc, Element cell);
	
	protected abstract void writeNotAvailableCell(Document doc, Element cell);
	
	protected abstract void writeStringCell(Document doc, Element cell, String value);

	protected abstract void writeBooleanCell(Document doc, Element cell, Boolean value);

	protected abstract void writeDateCell(Document doc, Element cell, String value);

	protected abstract void writeNumberCell(Document doc, Element cell, Number value);
	
	protected abstract Element makeRowElement(Document doc, Element parent);

	protected abstract Element makeCellElement(Document doc, Element parent);
	
	protected abstract void labelCell(Document doc, Element cell, String label);

	protected Element makeHeaderCellElement(Document doc, Element parent) {
		return makeCellElement(doc, parent);
	}
}
