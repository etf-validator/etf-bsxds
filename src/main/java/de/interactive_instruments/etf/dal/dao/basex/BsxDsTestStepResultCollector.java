/**
 * Copyright 2010-2017 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.dal.dao.basex;

import java.io.*;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import de.interactive_instruments.etf.testdriver.AbstractTestCollector;
import de.interactive_instruments.etf.testdriver.AbstractTestStepResultCollector;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
final class BsxDsTestStepResultCollector extends AbstractTestStepResultCollector implements BsxDsResultCollectorWriter {

	private final XmlTestResultWriter writer;
	private final File testCaseResultFile;
	private final ByteArrayOutputStream bos;
	private final List<String> testStepAttachmentIds;

	/**
	 * Ctor for called Test Steps
	 *
	 * @param parentCollector
	 */
	BsxDsTestStepResultCollector(final AbstractTestCollector parentCollector, final List<String> testStepAttachmentIds,
			final String testStepId, final long startTimestamp) {
		super(parentCollector, testStepId);
		this.testStepAttachmentIds = testStepAttachmentIds;
		bos = new ByteArrayOutputStream(512);
		testCaseResultFile = null;
		try {
			writer = new XmlTestResultWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(bos, "UTF-8"));
			writer.writeStartTestStepResult(testStepId, startTimestamp);
		} catch (XMLStreamException e) {
			throw new IllegalStateException(e);
		}
	}

	public void writeTo(OutputStream outputStream) throws IOException {
		if (bos != null) {
			bos.flush();
			bos.writeTo(outputStream);
		} else {
			IOUtils.copy(new FileInputStream(testCaseResultFile), outputStream);
		}
	}

	@Override
	protected String startTestStepResult(final String resultedFrom, final long startTimestamp) throws Exception {
		return writer.writeStartTestStepResult(resultedFrom, startTimestamp);
	}

	@Override
	protected String startTestAssertionResult(final String resultedFrom, final long startTimestamp) throws Exception {
		return writer.writeStartTestAssertionResult(resultedFrom, startTimestamp);
	}

	@Override
	protected String endTestStepResult(final String testModelItemId, final int status, final long stopTimestamp)
			throws Exception {
		if (!testStepAttachmentIds.isEmpty()) {
			writer.addAttachmentRefs(testStepAttachmentIds);
			testStepAttachmentIds.clear();
		}
		return writer.writeEndTestStepResult(testModelItemId, status, stopTimestamp);
	}

	@Override
	protected String endTestAssertionResult(final String testModelItemId, final int status, final long stopTimestamp)
			throws Exception {
		return writer.writeEndTestAssertionResult(testModelItemId, status, stopTimestamp);
	}

	@Override
	protected void startInvokedTests() {
		try {
			writer.writeStartInvokedTests();
		} catch (XMLStreamException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void endInvokedTests() {
		try {
			writer.writeEndInvokedTests();
		} catch (XMLStreamException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void startTestAssertionResults() {
		try {
			writer.writeStartTestAssertionResults();
		} catch (XMLStreamException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void endTestAssertionResults() {
		try {
			writer.writeEndTestAssertionResults();
		} catch (XMLStreamException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void doAddMessage(final String s) {
		writer.addMessage(s);
	}

	@Override
	protected void doAddMessage(final String s, final Map<String, String> map) {
		writer.addMessage(s, map);
	}

	@Override
	protected void doAddMessage(final String s, final String... strings) {
		writer.addMessage(s, strings);
	}

	@Override
	protected void notifyError() {
		// TODO
	}

	@Override
	protected AbstractTestCollector createCalledTestCaseResultCollector(final AbstractTestCollector parentCollector,
			final String testModelItemId, final long startTimestamp) {
		return new BsxDsTestCaseResultCollector(this, testStepAttachmentIds, testModelItemId, startTimestamp);
	}

	@Override
	protected AbstractTestCollector createCalledTestStepResultCollector(final AbstractTestCollector parentCollector,
			final String testModelItemId, final long startTimestamp) {
		return new BsxDsTestStepResultCollector(this, testStepAttachmentIds, testModelItemId, startTimestamp);
	}

	@Override
	protected void mergeResultFromCollector(final AbstractTestCollector collector) {
		try {
			writer.flush();
			((BsxDsResultCollectorWriter) collector).writeTo(bos);
		} catch (Exception e) {
			logger.error("Failed to append collector results: ", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected String currentResultItemId() {
		return writer.currentResultItemId();
	}

	@Override
	public void release() {

	}
}
