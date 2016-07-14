/*
 * Copyright ${year} interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.interactive_instruments.etf.dal.dao.basex.transformers;

import de.interactive_instruments.etf.dal.dao.basex.BsxDataStorage;
import de.interactive_instruments.etf.model.EID;
import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.mappings.transformers.FieldTransformer;
import org.eclipse.persistence.sessions.Session;

/**
 * Writes a EID to XML as String
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class EidFieldTransformer implements FieldTransformer {

	private AbstractTransformationMapping mapping;

	@Override public void initialize(final AbstractTransformationMapping mapping) {
		this.mapping = mapping;
		this.mapping.getFieldTransformations().get(0).getField().setPrimaryKey(true);
		this.mapping.getFieldTransformations().get(0).getField().setType(String.class);
	}

	@Override public Object buildFieldValue(final Object instance, final String fieldName, final Session session) {
		final EID eid = (EID) mapping.getAttributeValueFromObject(instance);
		return BsxDataStorage.ID_PREFIX + eid.getId();
	}
}
