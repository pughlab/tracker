package ca.uhnresearch.pughlab.tracker.services.impl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;

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
	
	protected abstract void write(Document doc, ViewDataResponse data);

	/**
	 * @param documentBuilderFactory the documentBuilderFactory to set
	 */
	public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}
	
	/**
	 * Main method to write a table body as a set of rows.
	 * @param doc
	 * @param parent
	 * @param data
	 */
	protected void writeTableBody(Document doc, Element parent, ViewDataResponse data) {
		Element start = makeRowElement(doc, parent);
		for (Attributes column : data.getAttributes()) {
			writeHeaderCell(doc, start, column.getLabel());
		}
		
		for (JsonNode row : data.getRecords()) {
			Element rowElement = makeRowElement(doc, parent);
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
	
	protected abstract void writeHeaderCell(Document doc, Element parent, String data);
	
	protected abstract void writeEmptyCell(Document doc, Element parent);
	
	protected abstract void writeNotAvailableCell(Document doc, Element parent);
	
	protected abstract void writeStringCell(Document doc, Element parent, String value);

	protected abstract void writeBooleanCell(Document doc, Element parent, Boolean value);

	protected abstract void writeDateCell(Document doc, Element parent, String value);

	protected abstract void writeNumberCell(Document doc, Element parent, Number value);
	
	protected abstract Element makeRowElement(Document doc, Element parent);
}
