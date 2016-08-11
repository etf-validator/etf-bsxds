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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.basex.core.BaseXException;
import org.basex.core.cmd.XQuery;

import de.interactive_instruments.etf.dal.dao.OutputFormatStreamable;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * Abstract class for a prepared XQuery statement whose result can be directly
 * streamed.
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
abstract class AbstractBsxPreparedDto implements OutputFormatStreamable {

	protected final XQuery xquery;
	protected final BsxDsCtx ctx;

	public AbstractBsxPreparedDto(final XQuery xquery, final BsxDsCtx ctx) {
		this.xquery = xquery;
		this.ctx = ctx;
	}

	/**
	 * Streams the result from the BaseX database to the Output Format directly
	 * through a piped stream
	 *
	 * @param outputFormat the Output Format
	 * @param arguments transformation arguments
	 * @param outputStream transformed output stream
	 */
	public void streamTo(final OutputFormat outputFormat, final PropertyHolder arguments, final OutputStream outputStream) {
		try {
			final PipedInputStream in = new PipedInputStream();
			final PipedOutputStream out = new PipedOutputStream(in);
			new Thread(() -> {
				try {
					xquery.execute(ctx.getBsxCtx(), out);
				} catch (final IOException e) {
					throw new IllegalStateException(e);
				} finally {
					try {
						out.close();
					} catch (IOException e) {
						ExcUtils.suppress(e);
					}
				}
			}).start();
			outputFormat.streamTo(arguments, in, outputStream);
			// xquery.execute(ctx.getBsxCtx(), outputStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected void logError(final Throwable e) {
		ctx.getLogger().error("Query Exception: {}", ExceptionUtils.getRootCauseMessage(e));
		if (ctx.getLogger().isDebugEnabled()) {
			try {
				if (ctx.getLogger().isTraceEnabled()) {
					ctx.getLogger().trace("Query: {}", xquery.toString());
					if (ExceptionUtils.getRootCause(e) != null &&
							ExceptionUtils.getRootCause(e) instanceof NullPointerException) {
						ctx.getLogger().trace("NullPointerException may indicate an invalid mapping!");
					}
				}
				Thread.sleep((long) (Math.random() * 2450));
				ctx.getLogger().debug("Query result: {}", xquery.execute(ctx.getBsxCtx()));
			} catch (InterruptedException | BaseXException e2) {
				ExcUtils.suppress(e2);
			}
		}
	}
}
