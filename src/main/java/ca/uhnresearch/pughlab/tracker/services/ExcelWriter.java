package ca.uhnresearch.pughlab.tracker.services;

import org.w3c.dom.Document;

import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;

public interface ExcelWriter {
	Document getExcelDocument(ViewDataResponse data);
}
