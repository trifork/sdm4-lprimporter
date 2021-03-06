/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
 * (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.sdm4.lpr.parsers;

import dk.nsi.sdm4.core.parser.Parser;
import dk.nsi.sdm4.core.parser.ParserException;
import dk.nsi.sdm4.lpr.common.exception.InvalidDoctorOrganisationIdentifier;
import dk.nsi.sdm4.lpr.common.splunk.SplunkLogger;
import dk.nsi.sdm4.lpr.dao.LPRWriteDAO;
import dk.sdsd.nsp.slalog.api.SLALogItem;
import dk.sdsd.nsp.slalog.api.SLALogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LPRParser implements Parser {
    private static final SplunkLogger log = new SplunkLogger(LPRParser.class);

	@Autowired
	LPRWriteDAO dao;

	@Autowired
	TransactionTemplate transactionTemplate;

	@Value("${spooler.lprimporter.batchsize}")
	protected int batchSize;

    @Autowired
    private SLALogger slaLogger;

	private int progressBatchSize = 10000;

	List<LprAction> batch = new ArrayList<LprAction>(batchSize);

	/**
	 * @see Parser#process(java.io.File, String)
	 */
	@Override
	public void process(File dataset, String identifier) throws ParserException {
		File file = findSingleFileOrComplain(dataset);

        SLALogItem slaLogItem = slaLogger.createLogItem(getHome()+".process", "SDM4."+getHome()+".process");
        slaLogItem.setMessageId(identifier);
        slaLogItem.addCallParameter(Parser.SLA_INPUT_NAME, dataset.getAbsolutePath());

        BufferedReader bf = null;
        String line = null;
        try {
            FileReader reader = new FileReader(file);
            bf = new BufferedReader(reader);

	        long counter = 0;
	        while ((line = bf.readLine()) != null) {
	            if (!line.startsWith("#")) {
                    LprAction action = LPRLineParser.parseLine(line);
                    batch.add(action);
                    counter++;
                    if (counter % progressBatchSize == 0) {
                        log.info("Progress: " + counter);
                    }
	                if (batch.size() == batchSize) {
		                commitBatch();
	                }
                }
            }

	        commitBatch(); // commit den rest der kan være fra sidste gennemløb

            slaLogItem.addCallParameter(Parser.SLA_RECORDS_PROCESSED_MAME, ""+counter);
            slaLogItem.setCallResultOk();
            slaLogItem.store();
        } catch (Exception e) {
            slaLogItem.setCallResultError("LPRImporter failed - Cause: " + e.getMessage());
            slaLogItem.store();
            // This is potentially a security issue as the raw data is exposed (but it is hashed)
            throw new ParserException(e.getMessage() + " in line \"" + line +  "\" of file " + file.getAbsolutePath(), e);
        } finally {
            closeQuietly(bf);
        }
    }

    @Override
	public String getHome() {
		return "lprimporter";
	}

	private File findSingleFileOrComplain(File dataset) {
		if (dataset == null) {
			throw new ParserException("Dataset cannot be null");
		}

		File[] files = dataset.listFiles();
		assert files != null;
		if (files.length == 0) {
			throw new ParserException("Dataset " + dataset.getAbsolutePath() + " is empty. Will not continue.");
		}

		if (files.length > 1) {
			throw new ParserException("Dataset " + dataset.getAbsolutePath() + " contains " + files.length + " files, I only expected 1. Will not continue.");
		}

		return files[0];
	}

	private void closeQuietly(BufferedReader bf) {
		if (bf != null) {
			try {
				bf.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

	private void commitBatch() {
		transactionTemplate.execute(new TransactionCallback<Void>() {
			@Override
			public Void doInTransaction(TransactionStatus status) {
				if (batch.size() > 0) {
					log.info("Committing batch", "batchsize", ""+batch.size());
					for (LprAction action : batch) {
						action.execute(dao);
					}
					batch.clear();
				}
				return null; // kun for at gøre TransactionCallback-interfacet glad, ingen bruger en returværdi til noget
			}
		});
	}
}
