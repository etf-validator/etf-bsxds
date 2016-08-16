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

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtil.DATA_STORAGE;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class TestItemTypeDaoTest {

	private static WriteDao<TestItemTypeDto> writeDao;

	@BeforeClass
	public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException, StoreException {
		BsxTestUtil.ensureInitialization();
		writeDao = ((WriteDao) DATA_STORAGE.getDao(TestItemTypeDto.class));
	}

	@Before
	public void clean() {
		try {
			writeDao.delete(BsxTestUtil.ASSERTION_TYPE_1.getId());
		} catch (ObjectWithIdNotFoundException | StoreException e) {}
	}

	@Test
	public void test_1_1_existsAndAddAndDelete() throws StoreException, ObjectWithIdNotFoundException {
		BsxTestUtil.existsAndAddAndDeleteTest(BsxTestUtil.ASSERTION_TYPE_1);
	}

	@Test
	public void test_2_0_getById() throws StoreException, ObjectWithIdNotFoundException {
		final PreparedDto<TestItemTypeDto> preparedDto = BsxTestUtil.addAndGetByIdTest(BsxTestUtil.ASSERTION_TYPE_1);

		writeDao.delete(BsxTestUtil.ASSERTION_TYPE_1.getId());
		TestCase.assertFalse(writeDao.exists(BsxTestUtil.ASSERTION_TYPE_1.getId()));
	}

}
