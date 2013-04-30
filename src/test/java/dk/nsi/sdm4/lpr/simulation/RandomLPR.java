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
package dk.nsi.sdm4.lpr.simulation;

import dk.nsi.sdm4.lpr.relation.model.GeneralInterval;
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.nsi.sdm4.lpr.relation.model.LPR.LprRelationType;

public class RandomLPR {

    // Needed for some tests
    public static Map<LPR, String> unhashedPatientCprnumbers = new HashMap<LPR, String>();

    private boolean saveUnhashedCprNumbers;

    RandomDataUtilForTestPurposes testDataUtil;

    public RandomLPR() {
        testDataUtil = new RandomDataUtilForTestPurposes();
        saveUnhashedCprNumbers = true;
    }

    public LPR randomLPR() {
        return build();
    }

    public void setSeed(long seed) {
        testDataUtil.setSeed(seed);
    }

    public void saveUnhashedCprNumbers() {
        saveUnhashedCprNumbers = true;
    }

    public void doNotSaveUnhashedCprNumbers() {
        saveUnhashedCprNumbers = false;
    }

    public List<LPR> randomLPRs(int n) {
        List<LPR> result = new ArrayList<LPR>();

        for (int i = 0; i < n; i++) {
            result.add(randomLPR());
        }

        return result;
    }

    private String patientCpr;
    private String lprReference;
    private LprRelationType relationType;
    private String organisationIdentifier;
    private GeneralInterval admittedInterval;

    private LPR build() {
        setRandomPatientCpr();
        setRandomLprReference();
        setRandomOrganisationIdentifier();
        setRandomAdmittetTime();

        LPR lpr = LPR.newInstance(HashedCpr.buildFromUnhashedString(patientCpr), admittedInterval, lprReference,
                relationType, organisationIdentifier);

        if (saveUnhashedCprNumbers) {
            unhashedPatientCprnumbers.put(lpr, patientCpr);
        }

        return lpr;
    }

    private void setRandomPatientCpr() {
        patientCpr = testDataUtil.randomCpr();
    }

    private void setRandomLprReference() {
        lprReference = testDataUtil.randomExternalLprReference();
    }

    private void setRandomOrganisationIdentifier() {
        List<LprRelationType> types = new ArrayList<LprRelationType>();
        for (LprRelationType type : LprRelationType.values()) {
            types.add(type);
        }

        relationType = testDataUtil.choice(types);
        if (relationType.definesHospital()) {
            organisationIdentifier = testDataUtil.randomHospitalOrganisationIdentifier().toString();
        } else if (relationType.definesDoctor()) {
            organisationIdentifier = testDataUtil.randomDoctorOrganisationIdentifier().toString();
        } else {
            throw new AssertionError("Should only be those two types.");
        }
    }

    private void setRandomAdmittetTime() {
        DateTime earliestAdmittionStart = new DateTime(2010, 12, 24, 0, 0, 0, 0);
        DateTime latestAdmittionStart = new DateTime(2011, 1, 31, 0, 0, 0, 0);
        DateTime admittedStart = testDataUtil.randomTime(earliestAdmittionStart, latestAdmittionStart);

        DateTime earliestAdmittionEnd = admittedStart.plusDays(1);
        DateTime latestAdmittionEnd = new DateTime(2011, 2, 28, 0, 0, 0, 0);
        DateTime admittedEnd = testDataUtil.randomTime(earliestAdmittionEnd, latestAdmittionEnd);

        // relationType must be set
        assert(relationType != null);
        if (relationType == LprRelationType.DISCHARGED_TO_UNIT) {
            admittedEnd = admittedStart.plusDays(1);
        }

        admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));
    }
}
