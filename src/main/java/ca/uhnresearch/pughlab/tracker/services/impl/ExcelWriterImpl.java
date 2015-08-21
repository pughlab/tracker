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
import ca.uhnresearch.pughlab.tracker.services.ExcelWriter;

public class ExcelWriterImpl implements ExcelWriter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String EXCEL_NAMESPACE = "urn:schemas-microsoft-com:office:spreadsheet";
	
	private DocumentBuilderFactory documentBuilderFactory;

	@Override
	public Document getExcelDocument(ViewDataResponse data) {
		
		Document doc = null;
				
		try {
			documentBuilderFactory.setNamespaceAware(true);			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			doc = builder.newDocument();
						
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Failed to generate XML parser");
		}
		
		logger.debug("Writing Excel XML data");
		
		Element rootElement = doc.createElementNS(EXCEL_NAMESPACE, "ss:Workbook");
		rootElement.setAttribute("xmlns:ss", EXCEL_NAMESPACE);
		doc.appendChild(rootElement);
		
		writeStyles(doc, rootElement);
		writeWorksheets(doc, rootElement, data);

		return doc;
	}
	
	private void writeWorksheets(Document doc, Element parent, ViewDataResponse data) {
		Element worksheet = doc.createElement("ss:Worksheet");
		worksheet.setAttribute("ss:Name", "Data");
		
		Element table = doc.createElement("ss:Table");
		
		parent.appendChild(worksheet);
		worksheet.appendChild(table);
		
		writeTableBody(doc, table, data);
	}
	
	private void writeStyles(Document doc, Element parent) {
		Element styles = doc.createElement("ss:Styles");
		parent.appendChild(styles);
		
		Element style = doc.createElement("ss:Style");
		style.setAttribute("ss:ID", "date1");
		styles.appendChild(style);
		
		Element format = doc.createElement("ss:NumberFormat");
		format.setAttribute("ss:Format", "Short Date");
		style.appendChild(format);
	}
	
	private void writeTableBody(Document doc, Element parent, ViewDataResponse data) {
		Element start = doc.createElement("ss:Row");
		parent.appendChild(start);
		for (Attributes column : data.getAttributes()) {
			writeStringCell(doc, start, column.getLabel());
		}
		
		for (JsonNode row : data.getRecords()) {
			Element rowElement = doc.createElement("ss:Row");
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
	
	private void writeDateCell(Document doc, Element parent, String date) {
		Element cell = doc.createElement("ss:Cell");
		cell.setAttribute("ss:StyleID", "date1");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "DateTime");
		body.appendChild(doc.createTextNode(date));
		
		cell.appendChild(body);
	}
	

	private void writeBooleanCell(Document doc, Element parent, Boolean data) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "String");
		body.appendChild(doc.createTextNode(data ? "Yes" : "No"));
		
		cell.appendChild(body);
	}
	
	private void writeNumberCell(Document doc, Element parent, Number data) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "Number");
		body.appendChild(doc.createTextNode(data.toString()));
		
		cell.appendChild(body);
	}
	
	private void writeEmptyCell(Document doc, Element parent) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
	}
	
	private void writeNotAvailableCell(Document doc, Element parent) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);

		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "Error");
		body.appendChild(doc.createTextNode("#N/A"));

		cell.appendChild(body);
	}

	private void writeStringCell(Document doc, Element parent, String data) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "String");
		body.appendChild(doc.createTextNode(data));
		
		cell.appendChild(body);
	}

	/**
	 * @param documentBuilderFactory the documentBuilderFactory to set
	 */
	public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}
}
