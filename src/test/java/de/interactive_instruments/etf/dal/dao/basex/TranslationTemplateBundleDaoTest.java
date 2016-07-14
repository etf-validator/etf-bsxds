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

package de.interactive_instruments.etf.dal.dao.basex;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.Locale;

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestConstants.DATA_STORAGE;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TranslationTemplateBundleDaoTest {

	private static WriteDao<TranslationTemplateBundleDto> writeDao;

	@BeforeClass
	public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException, StoreException {
		BsxTestConstants.ensureInitialization();
		writeDao = ((WriteDao)DATA_STORAGE.getDao(TranslationTemplateBundleDto.class));
		BsxTestConstants.DATA_STORAGE.reset();
	}

	@Before
	public void clean() {
		try {
			writeDao.delete(BsxTestConstants.TTB_1.getId());
		} catch (ObjectWithIdNotFoundException | StoreException e) {
		}
	}

	@Test
	public void test_1_1_existsAndAddAndDelete() throws StoreException, ObjectWithIdNotFoundException {
		assertNotNull(writeDao);
		assertTrue(writeDao.isInitialized());
		assertFalse(writeDao.exists(BsxTestConstants.TTB_1.getId()));
		writeDao.add(BsxTestConstants.TTB_1);
		assertTrue(writeDao.exists(BsxTestConstants.TTB_1.getId()));
		writeDao.delete(BsxTestConstants.TTB_1.getId());
		assertFalse(writeDao.exists(BsxTestConstants.TTB_1.getId()));
	}

	@Test
	public void test_2_getById() throws StoreException, ObjectWithIdNotFoundException {
		assertFalse(writeDao.exists(BsxTestConstants.TTB_1.getId()));
		writeDao.add(BsxTestConstants.TTB_1);
		assertTrue(writeDao.exists(BsxTestConstants.TTB_1.getId()));
		final PreparedDto<TranslationTemplateBundleDto> preparedDto = writeDao.
				getById(BsxTestConstants.TTB_1.getId());

		// Check internal ID
		assertEquals(BsxTestConstants.TTB_1.getId(), preparedDto.getDtoId());
		final TranslationTemplateBundleDto dto =  preparedDto.getDto();
		assertNotNull(dto);
		assertEquals(BsxTestConstants.TTB_1.getId(), dto.getId());

		assertNotNull(dto.getTranslationTemplateCollection("Template.1"));
		assertArrayEquals(new String[] { "de", "en"},
				dto.getTranslationTemplateCollection("Template.1").getLanguages().toArray(new String[2]));

		assertNull(dto.getTranslationTemplate("en", "Template.1"));
		assertNotNull(dto.getTranslationTemplate("Template.1","en"));
		assertEquals("Template.1", dto.getTranslationTemplate("Template.1","en").getName());
		assertEquals(Locale.ENGLISH.toLanguageTag(), dto.getTranslationTemplate("Template.1","en").getLanguage());
		assertEquals("Template.1 with three tokens: {TOKEN.3} {TOKEN.1} {TOKEN.2}",
				dto.getTranslationTemplate("Template.1","en").getStrWithTokens());

		assertNull(dto.getTranslationTemplate("de", "Template.2"));
		assertNotNull(dto.getTranslationTemplate("Template.2","de"));
		assertEquals("Template.2", dto.getTranslationTemplate("Template.2","de").getName());
		assertEquals(Locale.GERMAN.toLanguageTag(), dto.getTranslationTemplate("Template.2","de").getLanguage());
		assertEquals("Template.2 mit drei tokens: {TOKEN.5} {TOKEN.4} {TOKEN.6}",
				dto.getTranslationTemplate("Template.2","de").getStrWithTokens());

		writeDao.delete(BsxTestConstants.TTB_1.getId());
		assertFalse(writeDao.exists(BsxTestConstants.TTB_1.getId()));
	}

}
