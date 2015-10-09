package ca.uhnresearch.pughlab.tracker.services.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;

public class ExcelWriterImpl extends AbstractXMLWriter implements Writer {
	
	private final String EXCEL_NAMESPACE = "urn:schemas-microsoft-com:office:spreadsheet";
	
	@Override
	protected Element makeRowElement(Document doc, Element parent) {
		Element start = doc.createElement("ss:Row");
		parent.appendChild(start);
		return start;
	}
	
	@Override
	protected void write(Document doc, ViewDataResponse data) {
		Element rootElement = doc.createElementNS(EXCEL_NAMESPACE, "ss:Workbook");
		rootElement.setAttribute("xmlns:ss", EXCEL_NAMESPACE);
		doc.appendChild(rootElement);
		
		writeStyles(doc, rootElement);
		writeWorksheets(doc, rootElement, data);
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
	
	protected void writeDateCell(Document doc, Element parent, String date) {
		Element cell = doc.createElement("ss:Cell");
		cell.setAttribute("ss:StyleID", "date1");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "DateTime");
		body.appendChild(doc.createTextNode(date));
		
		cell.appendChild(body);
	}
	

	@Override
	protected void writeBooleanCell(Document doc, Element parent, Boolean data) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "String");
		body.appendChild(doc.createTextNode(data ? "Yes" : "No"));
		
		cell.appendChild(body);
	}
	
	@Override
	protected void writeNumberCell(Document doc, Element parent, Number data) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "Number");
		body.appendChild(doc.createTextNode(data.toString()));
		
		cell.appendChild(body);
	}
	
	@Override
	protected void writeEmptyCell(Document doc, Element parent) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
	}
	
	@Override
	protected void writeNotAvailableCell(Document doc, Element parent) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);

		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "Error");
		body.appendChild(doc.createTextNode("#N/A"));

		cell.appendChild(body);
	}
	
	@Override
	protected void writeHeaderCell(Document doc, Element parent, String data) {
		writeStringCell(doc, parent, data);
	}

	@Override
	protected void writeStringCell(Document doc, Element parent, String data) {
		Element cell = doc.createElement("ss:Cell");
		parent.appendChild(cell);
		
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "String");
		body.appendChild(doc.createTextNode(data));
		
		cell.appendChild(body);
	}

}
