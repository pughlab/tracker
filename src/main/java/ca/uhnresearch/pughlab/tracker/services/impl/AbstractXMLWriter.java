package ca.uhnresearch.pughlab.tracker.services.impl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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
}
