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

import de.interactive_instruments.IFile;
import de.interactive_instruments.TimeUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigPropertyHolder;
import org.basex.BaseX;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basex Data Storage
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class BsxDataStorage implements BsxDbCtx, DataStorage {

	private final Logger logger = LoggerFactory.getLogger(BsxDataStorage.class);

	private final JAXBContext jaxbContext;

	private static final String ETF_DB_NAME = "etf-ds";

	private static final String resPrefix = "eclipselink/classes/";

	static final String DECLARATIONS =
			"declare namespace etf='" + EtfConstants.ETF_XMLNS + "'; " +
			"declare namespace xs='http://www.w3.org/2001/XMLSchema'; " +
			"declare namespace xsi='http://www.w3.org/2001/XMLSchema-instance'; ";
	private final Map<Class, Dao> daoMapping;

	private IFile storeDir;
	private IFile backupDir;

	private Context ctx;
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	private ConfigPropertyHolder configProperties;

	BsxDataStorage() throws JAXBException {
		logger.debug("Preparing BsxDataStorage");

		final ClassLoader cL = getClass().getClassLoader();
		final Map<String, Source> metadataSources = Collections.unmodifiableMap(new HashMap<String, Source>() {{
			put("de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto",
					new StreamSource(cL.getResourceAsStream(resPrefix+
							"de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto.xml")));
		}});

		{
			final Map<Class, Dao> tmpDaoMapping = new HashMap<>();
			tmpDaoMapping.put(TestObjectDto.class, new TestObjectDao(this));
			daoMapping = Collections.unmodifiableMap(tmpDaoMapping);
		}

		final Map<String, Object> properties = Collections.unmodifiableMap(new HashMap<String, Object>() {{
			put(JAXBContextProperties.OXM_METADATA_SOURCE, metadataSources);
			put(JAXBContextProperties.DEFAULT_TARGET_NAMESPACE, EtfConstants.ETF_XMLNS);
		}});

		jaxbContext = JAXBContext.newInstance(daoMapping.keySet().toArray(new Class[daoMapping.size()]), properties);

		logger.info("BsxDataStorage {} prepared", this.getClass().getPackage().getImplementationVersion());
		logger.info("using BaseX {} ", BaseX.class.getPackage().getImplementationVersion());
		logger.info("using EclipseLink MOXy {} ", JAXBContext.class.getPackage().getImplementationVersion());
	}

	@Override
	public synchronized void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
		if (!this.initialized.get()) {
			logger.debug("Initializing BsxDataStorage");
			this.configProperties.expectAllRequiredPropertiesSet();

			try {
				// Initialize
				this.storeDir = this.configProperties.getPropertyAsFile(EtfConstants.ETF_DATASOURCE_DIR).expandPath("obj");
				this.backupDir = this.configProperties.getPropertyAsFile(EtfConstants.ETF_BACKUP_DIR);
				this.storeDir.ensureDir();

				new Set("AUTOFLUSH", "false").execute(ctx);
				new Set("TEXTINDEX", "true").execute(ctx);
				new Set("ATTRINDEX", "true").execute(ctx);
				new Set("FTINDEX", "true").execute(ctx);
				new Set("MAXLEN", "80").execute(ctx);
				new Set("UPDINDEX", "true").execute(ctx);

				// Do not use autooptimize here "However, updates can take much longer, so this option
				// should only be activated for medium-sized databases." (BaseX docu)
				try {
					new Open(ETF_DB_NAME).execute(ctx);
				} catch (Exception e) {
					try {
						logger.warn("Opening of the data source failed, recreating it");
						reset();
					} catch (Exception e2) {
						throw new InitializationException("Recreation of etf-ds store failed ", e2);
					}
				}
				logger.info(new InfoDB().execute(ctx));
				this.initialized.set(true);
				notifyAll();
			} catch (IOException e) {
				throw new InitializationException(e);
			}
			logger.info("BsxDataStorage initialized");
			return;
		}
		throw new InvalidStateTransitionException("Data source already initialized");
	}

	@Override public boolean isInitialized() {
		return this.initialized.get();
	}

	@Override public Map<Class, Dao> getDaoMappings() {
		return daoMapping;
	}

	@Override
	public synchronized void reset() throws StoreException {
		this.initialized.set(false);
		long start = System.currentTimeMillis();
		try {
			try {
				new DropDB(ETF_DB_NAME).execute(ctx);
			} catch (Exception e) {
				ExcUtils.supress(e);
			}
			new CreateDB(ETF_DB_NAME).execute(ctx);
			System.gc();
			long added = 0;
			long skipped = 0;
			for (File file : storeDir.listFiles(file1 -> !file1.isDirectory() && file1.getName().endsWith("xml"))) {
				try {
					new Add(file.getName(), file.getAbsolutePath()).execute(ctx);
					added++;
				} catch (Exception e) {
					skipped++;
					logger.error("Failed to add file {} (no. {}) to data source:", file.getAbsolutePath(), added, e);
				}
			}
			logger.info("{} files added, {} files skipped", added, skipped);
			new Flush().execute(ctx);
			logger.info("Optimizing " + ETF_DB_NAME);
			new OptimizeAll().execute(ctx);
			new Open(ETF_DB_NAME).execute(ctx);
		} catch (BaseXException e) {
			throw new StoreException(e.getMessage());
		}
		logger.info("Recreated store in: " + TimeUtils.currentDurationAsMinsSeconds(start));
		this.initialized.set(true);
	}

	@Override public String createBackup() throws StoreException {
		// TODO not implemented in this version
		final String bakName = "ETFDS-"+TimeUtils.dateToIsoString(new Date());
		try {
			new CreateBackup(bakName).execute(getBsxCtx());
		} catch (BaseXException e) {
			new StoreException(e.getMessage());
		}
		return bakName;
	}

	@Override public List<String> getBackupList() {
		// TODO not implemented in this version
		return null;
	}

	@Override public void restoreBackup(final String name) throws StoreException {
		// TODO not implemented in this version
		try {
			new Restore(name).execute(getBsxCtx());
		} catch (BaseXException e) {
			new StoreException(e.getMessage());
		}
	}

	@Override public Context getBsxCtx() {
		if(this.initialized.get()) {
			return this.ctx;
		}
		synchronized (getClass()) {
			if (!this.initialized.get()) {
				logger.warn("Datasource busy, waiting for initialization");
				long timeoutInMinutes = 12;
				try {
					wait(timeoutInMinutes * 60000);
				} catch (final InterruptedException e) {
					ExcUtils.supress(e);
				}
				if (!this.initialized.get()) {
					final String errMsg = "Data source not up after " + timeoutInMinutes + " minutes";
					logger.error(errMsg);
					throw new IllegalStateException(errMsg);
				}
			}
		}
		return this.ctx;
	}

	@Override public IFile getStoreDir() {
		return this.storeDir;
	}

	@Override public JAXBContext getJaxbCtx() {
		return this.jaxbContext;
	}

	@Override public ConfigPropertyHolder getConfigurationProperties() {
		return configProperties;
	}

	@Override public void release() {
		this.initialized.set(false);
		try {
			new Close().execute(ctx);
			ctx.close();
		} catch (BaseXException e) {
			logger.error("Error during source shutdown ", e);
		} finally {
			logger.info("BsxDataStorage released");
		}
	}
}
