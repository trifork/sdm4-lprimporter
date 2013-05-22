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

import dk.nsi.sdm4.core.parser.ParserException;
import dk.nsi.sdm4.lpr.common.exception.HashException;
import dk.nsi.sdm4.lpr.common.util.Hasher;
import dk.nsi.sdm4.lpr.relation.model.GeneralInterval;
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import static dk.nsi.sdm4.lpr.relation.model.LPR.LprRelationType;
import static org.junit.Assert.assertEquals;

/**
 * Data for these tests are taken from BR_190520111525.CSV.
 */
public class LprLineParserOnRealDataTest {

    @Test
    public void testParseFirstLine() throws ParserException {
        String line = "8001047;F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C;07-01-2009 12:50;08-01-2009 16:00;P;80010470F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C060120090800";
        String organisationIdentifierString = "8001047";
        HashedCpr patientCpr = HashedCpr.buildFromHashedString("F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C");
        DateTime admittedStart = new DateTime(2009, 1, 7, 12, 50, 0, 0);
        DateTime admittedEnd = new DateTime(2009, 1, 8, 16, 0, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));
        LprRelationType relationType = LprRelationType.PROCEDURE_UNIT;
        String lprReference = "80010470F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C060120090800";
        LPR expected = LPR.newInstance(patientCpr, admittedInterval, lprReference, relationType,
		        organisationIdentifierString);
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        assertEquals(expected, action.lprForInsertion);
    }

    @Test
    public void testParseSecondLine() throws ParserException {
        String line = "6008050;F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C;06-01-2009 08:00;08-01-2009 16:00;S;60080500F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C060120090800";
        String organisationIdentifierString = "6008050";
        HashedCpr patientCpr = HashedCpr.buildFromHashedString("F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C");
        DateTime admittedStart = new DateTime(2009, 1, 6, 8, 0, 0, 0);
        DateTime admittedEnd = new DateTime(2009, 1, 8, 16, 0, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));
        LprRelationType relationType = LprRelationType.PARENT_UNIT;
        String lprReference = "60080500F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C060120090800";
        LPR expected = LPR.newInstance(patientCpr, admittedInterval, lprReference, relationType,
                organisationIdentifierString);
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        assertEquals(expected, action.lprForInsertion);
    }

    @Test
    public void testParseThirdLine() throws ParserException {
        String line = "8001399;F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C;06-01-2009 13:45;08-01-2009 16:00;P;80013990F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C060120090800";
        String organisationIdentifierString = "8001399";
        HashedCpr patientCpr = HashedCpr.buildFromHashedString("F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C");
        DateTime admittedStart = new DateTime(2009, 1, 6, 13, 45, 0, 0);
        DateTime admittedEnd = new DateTime(2009, 1, 8, 16, 0, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));
        LprRelationType relationType = LprRelationType.PROCEDURE_UNIT;
        String lprReference = "80013990F39AA0D612FFFE9ED10B8FA2D72FC272AE75A00C060120090800";
        LPR expected = LPR.newInstance(patientCpr, admittedInterval, lprReference, relationType,
                organisationIdentifierString);
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        assertEquals(expected, action.lprForInsertion);
    }

    @Test
    public void testParseFourthLine() throws ParserException {
        String line = "6008050;11FED4C82913149F63B1944706DFC7C707C27C7C;28-01-2011 00:00;09-02-2011 00:00;S;6008050011FED4C82913149F63B1944706DFC7C707C27C7C280120110000";
        String organisationIdentifierString = "6008050";
        HashedCpr patientCpr = HashedCpr.buildFromHashedString("11FED4C82913149F63B1944706DFC7C707C27C7C");
        DateTime admittedStart = new DateTime(2011, 1, 28, 0, 0, 0, 0);
        DateTime admittedEnd = new DateTime(2011, 2, 9, 0, 0, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));
        LprRelationType relationType = LprRelationType.PARENT_UNIT;
        String lprReference = "6008050011FED4C82913149F63B1944706DFC7C707C27C7C280120110000";
        LPR expected = LPR.newInstance(patientCpr, admittedInterval, lprReference, relationType,
                organisationIdentifierString);
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        assertEquals(expected, action.lprForInsertion);
    }

    @Test
    public void testParseFifhtLine() throws ParserException {
        String line = "8001089;11FED4C82913149F63B1944706DFC7C707C27C7C;09-02-2011 00:00;09-02-2011 00:00;U;8001089011FED4C82913149F63B1944706DFC7C707C27C7C280120110000";
        String organisationIdentifierString = "8001089";
        HashedCpr patientCpr = HashedCpr.buildFromHashedString("11FED4C82913149F63B1944706DFC7C707C27C7C");
        DateTime admittedStart = new DateTime(2011, 2, 9, 0, 0, 0, 0);
        DateTime admittedEnd = new DateTime(2011, 2, 10, 0, 0, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));
        LprRelationType relationType = LprRelationType.DISCHARGED_TO_UNIT;
        String lprReference = "8001089011FED4C82913149F63B1944706DFC7C707C27C7C280120110000";
        LPR expected = LPR.newInstance(patientCpr, admittedInterval, lprReference, relationType,
                organisationIdentifierString);
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        assertEquals(expected, action.lprForInsertion);
    }

    @Test
    public void testParseSixthLine() throws ParserException {
        String line = "123456;0B0E20A894AC8C363EC84CDE7CD73C6FB1953046;18-05-2011 00:00;;Y;123456010B0E20A894AC8C363EC84CDE7CD73C6FB1953046180520110000";
        String organisationIdentifierString = "123456";
        HashedCpr patientCpr = HashedCpr.buildFromHashedString("0B0E20A894AC8C363EC84CDE7CD73C6FB1953046");
        DateTime admittedStart = new DateTime(2011, 5, 18, 0, 0, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.openInterval(admittedStart);
        LprRelationType relationType = LprRelationType.DUSAS;
        String lprReference = "123456010B0E20A894AC8C363EC84CDE7CD73C6FB1953046180520110000";
        LPR expected = LPR.newInstance(patientCpr, admittedInterval, lprReference, relationType,
                organisationIdentifierString);
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        assertEquals(expected, action.lprForInsertion);
    }

    // Jira SDM-28
    @Test
    public void testParseShortYdernumLine() {
        String line = "9915;AFAFEDABCDE031D41A0E44216EE200C107CFF52B;15-11-2012 00:00;;Y;99150019AFAFEDABCDE031D41A0E44216EE200C107CFF52B201211150000";
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
    }

    @Test
    public void testParseDeletion() throws ParserException {
        String line = "123456;;18-05-2011 00:00;;X;123456010B0E20A894AC8C363EC84CDE7CD73C6FB1953046180520110000";
        String lprReference = "123456010B0E20A894AC8C363EC84CDE7CD73C6FB1953046180520110000";
        LprAction action = LPRLineParser.parseLine(line);
        assertEquals(LprAction.ActionType.DELETION, action.actionType);
        assertEquals(lprReference, action.lprReferenceForDeletion);
    }

    @Test
    public void testHashingWithDataFromLogicaFirstCpr() throws HashException {
        String cpr = "0101372BN2";
        String expected = "97A1EBC3FC81212FC8AFFD5BAA0D8FBD08A611AE";
        assertEquals(expected, Hasher.hash(cpr));
    }

    @Test
    public void testHashingWithDataFromLogicaSecondCpr() throws HashException {
        String cpr = "0101372BB2";
        String expected = "9881274A808B2B80D37645EEBE58EC32150D5A5C";
        assertEquals(expected, Hasher.hash(cpr));
    }
}
