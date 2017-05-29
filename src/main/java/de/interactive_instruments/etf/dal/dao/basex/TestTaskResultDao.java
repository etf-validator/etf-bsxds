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

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.validation.Schema;

import org.basex.core.BaseXException;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigProperties;

/**
 * Test Task Result Data Access Object
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class TestTaskResultDao extends AbstractBsxStreamWriteDao<TestTaskResultDto> {

	private final Schema schema;

	private static class ValidationErrorHandler implements ErrorHandler {

		private final Logger logger;

		private ValidationErrorHandler(final Logger logger) {
			this.logger = logger;
		}

		@Override
		public void warning(final SAXParseException exception) throws SAXException {

		}

		@Override
		public void error(final SAXParseException exception) throws SAXException {
			if (!exception.getMessage().startsWith("cvc-id")) {
				throw new SAXException(exception);
			}
		}

		@Override
		public void fatalError(final SAXParseException exception) throws SAXException {
			if (!exception.getMessage().startsWith("cvc-id")) {
				throw new SAXException(exception);
			}
		}
	}

	protected TestTaskResultDao(final BsxDsCtx ctx) throws StorageException, IOException, TransformerConfigurationException {
		super("/etf:TestTaskResult", "TestTaskResult", ctx,
				(dsResultSet) -> dsResultSet.getTestTaskResults());
		schema = ((BsxDataStorage) ctx).getSchema();
		configProperties = new ConfigProperties("etf.webapp.base.url");
	}

	@Override
	protected void doInit() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
		try {
			final XsltOutputTransformer reportTransformer = DsUtils.loadReportTransformer(this);
			outputFormatIdMap.put(reportTransformer.getId(), reportTransformer);
		} catch (IOException | TransformerConfigurationException e) {
			throw new InitializationException(e);
		}
	}

	@Override
	protected void doCleanAfterDelete(final EID eid) throws BaseXException {

	}

	@Override
	public Class<TestTaskResultDto> getDtoType() {
		return TestTaskResultDto.class;
	}

	@Override
	public boolean isDisabled(final EID eid) {
		return false;
	}
}
