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
	protected Element makeCellElement(Document doc, Element parent) {
		Element start = doc.createElement("ss:Cell");
		parent.appendChild(start);
		return start;
	}
	
	@Override
	protected void labelCell(Document doc, Element cell, String label) {
		if (label == null) {
			return;
		}
		cell.setAttribute("ss:StyleID", label);
	}
	
	@Override
	protected void write(Document doc, ViewDataResponse data) {
		Element rootElement = doc.createElementNS(EXCEL_NAMESPACE, "ss:Workbook");
		rootElement.setAttribute("xmlns:ss", EXCEL_NAMESPACE);
		doc.appendChild(rootElement);
		
		writeStyles(doc, rootElement);
		writeData(doc, rootElement, data);
	}

	@Override
	protected void writeData(Document doc, Element parent, ViewDataResponse data) {
		Element worksheet = doc.createElement("ss:Worksheet");
		worksheet.setAttribute("ss:Name", "Data");
		
		Element table = doc.createElement("ss:Table");
		
		parent.appendChild(worksheet);
		worksheet.appendChild(table);
		
		writeTableBody(doc, table, data);
	}
	
	private void addColourStyle(Document doc, Element styles, String name, String value) {
		Element style = doc.createElement("ss:Style");
		style.setAttribute("ss:ID", name);
		Element interior = doc.createElement("ss:Interior");
		interior.setAttribute("ss:Color", value);
		interior.setAttribute("ss:Pattern", "Solid");
		style.appendChild(interior);
		styles.appendChild(style);
		
		style = doc.createElement("ss:Style");
		String datedName = "date1-" + name;
		style.setAttribute("ss:ID", datedName);
		interior = doc.createElement("ss:Interior");
		interior.setAttribute("ss:Color", value);
		interior.setAttribute("ss:Pattern", "Solid");
		style.appendChild(interior);
		Element format = doc.createElement("ss:NumberFormat");
		format.setAttribute("ss:Format", "Short Date");
		style.appendChild(format);

		styles.appendChild(style);
	}
	
	/**
	 * Generates the styles element. Styles are different to those in CSS
	 * as we're only allowed a single style id. As a workaround, we 
	 * pre-generate the styles, which is basically 2 * n where n is the
	 * number of labels, each with and without a date. Each is also
	 * required to be sorted by name key.
	 */
	@Override
	protected void writeStyles(Document doc, Element parent) {
		Element styles = doc.createElement("ss:Styles");
		parent.appendChild(styles);
		
		Element style = doc.createElement("ss:Style");
		style.setAttribute("ss:ID", "date1");
		Element format = doc.createElement("ss:NumberFormat");
		format.setAttribute("ss:Format", "Short Date");
		style.appendChild(format);
		styles.appendChild(style);

		addColourStyle(doc, styles, "label1", "#CBD5E8");
		addColourStyle(doc, styles, "label2", "#B3E2CD");
		addColourStyle(doc, styles, "label3", "#FDCDAC");
		addColourStyle(doc, styles, "label4", "#E6F5C9");
		addColourStyle(doc, styles, "label5", "#F4CAE4");
		addColourStyle(doc, styles, "label6", "#FFF2AE");
		addColourStyle(doc, styles, "label7", "#F1E2CC");
		addColourStyle(doc, styles, "label8", "#CCCCCC");
	}
	
	@Override
	protected void writeDateCell(Document doc, Element cell, String date) {
		if (cell.hasAttribute("ss:StyleID")) {
			cell.setAttribute("ss:StyleID", "date1-" + cell.getAttribute("ss:StyleID"));
		} else {
			cell.setAttribute("ss:StyleID", "date1");
		}

		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "DateTime");
		body.appendChild(doc.createTextNode(date));
		
		cell.appendChild(body);
	}
	
	@Override
	protected void writeBooleanCell(Document doc, Element cell, Boolean data) {
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "String");
		body.appendChild(doc.createTextNode(data ? "Yes" : "No"));
		
		cell.appendChild(body);
	}
	
	@Override
	protected void writeNumberCell(Document doc, Element cell, Number data) {
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "Number");
		body.appendChild(doc.createTextNode(data.toString()));
		
		cell.appendChild(body);
	}
	
	@Override
	protected void writeEmptyCell(Document doc, Element cell) {
		// Nothing to do
	}
	
	@Override
	protected void writeNotAvailableCell(Document doc, Element cell) {
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "Error");
		body.appendChild(doc.createTextNode("#N/A"));

		cell.appendChild(body);
	}
	
	@Override
	protected void writeStringCell(Document doc, Element cell, String data) {
		Element body = doc.createElement("ss:Data");
		body.setAttribute("ss:Type", "String");
		
		String newlinedData = data.replace("\n", "\r");
		body.appendChild(doc.createTextNode(newlinedData));
		
		cell.appendChild(body);
	}

}
