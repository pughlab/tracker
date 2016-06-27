package ca.uhnresearch.pughlab.tracker.services.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;

public class HtmlWriterImpl extends AbstractXMLWriter implements Writer {

	@Override
	protected void labelCell(Document doc, Element cell, String label) {
		if (label == null) {
			return;
		}
		if (cell.hasAttribute("class")) {
			label = cell.getAttribute("class") + " " + label;
		}
		cell.setAttribute("class", label);
	}
	
	@Override
	protected void write(Document doc, ViewDataResponse data) {
		final Element rootElement = doc.createElement("html");
		doc.appendChild(rootElement);
		
		final Element headElement = doc.createElement("head");
		rootElement.appendChild(headElement);
		writeStyles(doc, headElement);

		final Element bodyElement = doc.createElement("body");
		rootElement.appendChild(bodyElement);
		
		writeData(doc, bodyElement, data);
	}

	@Override
	protected void writeData(Document doc, Element parent, ViewDataResponse data) {
		final Element table = doc.createElement("table");
		parent.appendChild(table);
		
		writeTableBody(doc, table, data);
	}

	@Override
	protected Element makeRowElement(Document doc, Element parent) {
		final Element start = doc.createElement("tr");
		parent.appendChild(start);
		return start;
	}
	
	@Override
	protected Element makeCellElement(Document doc, Element parent) {
		final Element start = doc.createElement("td");
		parent.appendChild(start);
		return start;

	}
	
	@Override
	protected Element makeHeaderCellElement(Document doc, Element parent) {
		final Element start = doc.createElement("th");
		parent.appendChild(start);
		return start;

	}

	private void writeRawStringCell(Document doc, Element cell, String data) {
		cell.appendChild(doc.createTextNode(data));
	}
	
	@Override
	protected void writeStringCell(Document doc, Element cell, String data) {
		final String newlinedData = data.replace("\n", "<br>\n");
		writeRawStringCell(doc, cell, newlinedData);
	}
	
	@Override
	protected void writeDateCell(Document doc, Element parent, String value) {
		writeRawStringCell(doc, parent, value);
	}
	
	@Override
	protected void writeBooleanCell(Document doc, Element parent, Boolean data) {
		writeRawStringCell(doc, parent, data ? "Yes" : "No");
	}
	
	@Override
	protected void writeNumberCell(Document doc, Element parent, Number data) {
		writeRawStringCell(doc, parent, data.toString());
	}
	
	@Override
	protected void writeNotAvailableCell(Document doc, Element parent) {
		writeRawStringCell(doc, parent, "#N/A");
	}
	
	@Override
	protected void writeEmptyCell(Document doc, Element parent) {
		writeRawStringCell(doc, parent, "");
	}
	
	@Override
	protected void writeStyles(Document doc, Element parent) {
		final Element styles = doc.createElement("style");
		styles.setAttribute("type", "text/css");
		
		// Now let's add some CSS code for the styles
		styles.appendChild(doc.createTextNode(".label0 { background-color: inherit; }\n"));
		styles.appendChild(doc.createTextNode(".label1 { background-color: #CBD5E8; }\n"));
		styles.appendChild(doc.createTextNode(".label2 { background-color: #B3E2CD; }\n"));
		styles.appendChild(doc.createTextNode(".label3 { background-color: #FDCDAC; }\n"));
		styles.appendChild(doc.createTextNode(".label4 { background-color: #E6F5C9; }\n"));
		styles.appendChild(doc.createTextNode(".label5 { background-color: #F4CAE4; }\n"));
		styles.appendChild(doc.createTextNode(".label6 { background-color: #FFF2AE; }\n"));
		styles.appendChild(doc.createTextNode(".label7 { background-color: #F1E2CC; }\n"));
		styles.appendChild(doc.createTextNode(".label8 { background-color: #CCCCCC; }\n"));
		
		parent.appendChild(styles);
	}
}
