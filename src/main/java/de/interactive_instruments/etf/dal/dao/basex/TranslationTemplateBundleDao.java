/**
 * Copyright 2010-2016 interactive instruments GmbH
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

import java.util.List;

import org.basex.core.BaseXException;

import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.StorageException;

/**
 * Translation Template Bundle Data Access Object
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
final class TranslationTemplateBundleDao extends AbstractStreamWriteDao<TranslationTemplateBundleDto> {

	protected TranslationTemplateBundleDao(final BsxDsCtx ctx) throws StorageException {
		super("/etf:TranslationTemplateBundle", "TranslationTemplateBundle", ctx,
				(dsResultSet) -> dsResultSet.getTranslationTemplateBundles());
	}

	@Override
	protected void doCleanAfterDelete(final EID eid) throws BaseXException {

	}

	@Override
	public Class<TranslationTemplateBundleDto> getDtoType() {
		return TranslationTemplateBundleDto.class;
	}

}
