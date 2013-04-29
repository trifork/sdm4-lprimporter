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
import dk.nsi.sdm4.lpr.common.exception.DAOException;
import dk.nsi.sdm4.lpr.config.LprimporterApplicationConfig;
import dk.nsi.sdm4.lpr.dao.LPRTestPurposeDAO;
import dk.nsi.sdm4.lpr.dao.impl.LPRTestPurposeDAOImpl;
import dk.nsi.sdm4.lpr.relation.model.GeneralInterval;
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import dk.nsi.sdm4.lpr.simulation.RandomLPR;
import dk.nsi.sdm4.lpr.testutil.GenerateTestRegisterDumps;
import dk.nsi.sdm4.testutils.TestDbConfiguration;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LPRParserTest.TestConfig.class, LprimporterApplicationConfig.class, TestDbConfiguration.class})
public class LPRParserTest {
	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();

	@Autowired
	private GenerateTestRegisterDumps generator;

	@Autowired
	private Parser parser;

	@Autowired
	private LPRTestPurposeDAO testDao;

	@Autowired
	RandomLPR randomLPR;

	@Configuration
	static class TestConfig {
		@Bean
		public GenerateTestRegisterDumps generator() {
			return new GenerateTestRegisterDumps();
		}

		@Bean
		public RandomLPR randomLPR() {
			RandomLPR randomLPR = new RandomLPR();
			randomLPR.setSeed(GenerateTestRegisterDumps.LPR_SEED);

			return randomLPR;
		}

		@Bean
		public LPRParser parser() {
			return new LPRParser();
		}

		@Bean
		public LPRTestPurposeDAO dao() {
			return new LPRTestPurposeDAOImpl();
		}
	}

	@Before
	public void clearDb() {
		// LPRParser committer sine egen transaktioner med batches af actions, så vi kan ikke bare bruge @Transactional på testen
		testDao.purge();
	}

	@Test
    public void testParsing() throws IOException, DAOException {
        String filename = getPathToNamedFileInEmptyDir(GenerateTestRegisterDumps.LPR_OUTPUT_FILE_NAME);

		final int numberOfRecords = 10;
		generator.generateTestRegisterDumps(new File(filename).getParentFile(), numberOfRecords);

        assertThatJobRunsSuccesfully(filename);

        Set<LPR> seenAsSet = getAllLprsAsSet();
        Set<LPR> expected = makeRandomLprs(numberOfRecords); // uses same seed
                                                             // as the generator

        assertEquals(expected.size(), seenAsSet.size());
        assertEquals(expected, seenAsSet);
    }

    @Test
    public void testDeletion() throws IOException, DAOException {
        assertThatJobParsesFile("LPRParserTest-testInsertion.csv");
        assertEquals(1, getAllLprsAsSet().size());

        assertThatJobParsesFile("LPRParserTest-testDeletion.csv");
        assertEquals(0, getAllLprsAsSet().size());
    }

    // TODO Back in when importer handles SHAK identifers
    @Test
    @Ignore
    public void testUpdate() throws IOException, DAOException {
        assertThatJobParsesFile("LPRParserTest-testInsertion.csv");

        LPR expectedLpr = LPR.newInstance(HashedCpr.buildFromHashedString("0B0E20A894AC8C363EC84CDE7CD73C6FB1953046"),
                GeneralInterval.openInterval(new DateTime(2011, 5, 18, 0, 0, 0, 0)),
                "123456010B0E20A894AC8C363EC84CDE7CD73C6FB1953046180520110000",
                LPR.LprRelationType.fromStringRepresentation("Y"), "123456"); // values
                                                                              // from
                                                                              // file
                                                                              // ...testInsertion.csv
        assertThatDatabaseContainsOnly(expectedLpr);

        assertThatJobParsesFile("LPRParserTest-testUpdate.csv");

        GeneralInterval expectedAdmittedIntervalAfterUpdate = GeneralInterval.closedInterval(new Interval(new DateTime(
                2011, 2, 9, 0, 0, 0, 0), new DateTime(2011, 2, 10, 0, 0, 0, 0))); // note
                                                                                  // that
                                                                                  // parses
                                                                                  // adds
                                                                                  // 1
                                                                                  // day
                                                                                  // to
                                                                                  // the
                                                                                  // end
                                                                                  // time
                                                                                  // in
                                                                                  // the
                                                                                  // file
        LPR expectedLprAfterUpdate = LPR.newInstance(
                HashedCpr.buildFromHashedString("11FED4C82913149F63B1944706DFC7C707C27C7C"),
                expectedAdmittedIntervalAfterUpdate, "123456010B0E20A894AC8C363EC84CDE7CD73C6FB1953046180520110000",
                LPR.LprRelationType.fromStringRepresentation("U"), "8001089"); // values
                                                                               // from
                                                                               // file
                                                                               // ...testInsertion.csv
        assertThatDatabaseContainsOnly(expectedLprAfterUpdate);
    }

    @Test
    public void testParsingOfRealFileFromVendorUsingHandlerDirectlyInsteadOfRegisterImportJob() throws IOException {
	    String filename = getPathToNamedFileInEmptyDir("lpr_2011_10_05_orignal_name_was_BR_051020111448.CSV.csv");
        copyResourceToFile("BR_190520111525.CSV", filename);

        parser.process(new File(filename).getParentFile());
    }

    private void assertThatDatabaseContainsOnly(LPR expectedLpr) throws DAOException {
        Set<LPR> expected = new HashSet<LPR>();
        expected.add(expectedLpr);

        Set<LPR> seenAsSet = getAllLprsAsSet();
        assertEquals(expected.size(), seenAsSet.size());
        assertEquals(expected, seenAsSet);
    }

    private void assertThatJobParsesFile(String resourceNameForFile) throws IOException, DAOException {
	    String filename = getPathToNamedFileInEmptyDir("lpr_foo_" + resourceNameForFile);
        copyResourceToFile(resourceNameForFile, filename);

        assertThatJobRunsSuccesfully(filename);
    }

	private String getPathToNamedFileInEmptyDir(String resourceNameForFile) throws IOException {
		File emptyTmpDir = tmpDir.newFolder();
		return emptyTmpDir.getAbsolutePath() + "/" + resourceNameForFile;
	}

	private void copyResourceToFile(String resourceName, String filename) throws IOException {
	    URL url = this.getClass().getResource(resourceName);
        FileUtils.copyURLToFile(url, new File(filename));
    }

    private void assertThatJobRunsSuccesfully(String filename) {
	    parser.process(new File(filename).getParentFile());
    }

    private Set<LPR> makeRandomLprs(int numberOfRecords) {
        return new HashSet<LPR>(randomLPR.randomLPRs(numberOfRecords));
    }

    private Set<LPR> getAllLprsAsSet() throws DAOException {
	    return new HashSet<LPR>(testDao.getAllLPRs());
    }
}
