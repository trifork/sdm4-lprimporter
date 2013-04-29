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
package dk.nsi.sdm4.lpr.dao.impl;

import dk.nsi.sdm4.lpr.common.exception.DAOException;
import dk.nsi.sdm4.lpr.common.splunk.SplunkLogger;
import dk.nsi.sdm4.lpr.dao.LPRWriteDAO;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

public class LPRDAOImpl implements LPRWriteDAO {
	private static final SplunkLogger log = new SplunkLogger(LPRDAOImpl.class);

	@Autowired
	JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert basedataInserter;
	private NamedParameterJdbcTemplate namedParamsTemplate;

	@PostConstruct
	protected void setupInserter() {
		// vi bruger primært SimpleJdbcInsert fordi det er den letteste måde at få fat i den genererede primærnøgle fra databasen
		basedataInserter = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("LPR")
				.usingColumns("patientCpr", "relationType", "organisationIdentifier", "admittedStart", "admittedEnd", "lprReference")
				.usingGeneratedKeyColumns("pk");
	}

	@PostConstruct
	protected void setupNamedParamJdbcTemplate() {
		namedParamsTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}

	@Override
	public long insertOrUpdate(LPR lpr) throws DAOException {
		long pk;

		try {
			pk = insertOrUpdateBaseData(lpr);
			if (log.isDebugEnabled()) {
				log.debug("LPR inserted", "LPR", lpr.toString(), "PK", pk+"");
			}
		} catch (DataAccessException e) {
			throw new DAOException("Unable to insert " + lpr.toString(), e);
		}

		return pk;
	}

	@Override
    public void deleteByLprReference(String lprReference) throws DAOException {
	    try {
		    int numRows = jdbcTemplate.update("DELETE FROM LPR WHERE lprReference = ?", lprReference);
	        if(log.isDebugEnabled()) {
	            log.debug("Deleted " + numRows + " for lpr reference " + lprReference);
	        }
        } catch (DataAccessException e) {
            throw new DAOException("Unable to delete by lpr reference " + lprReference, e);
        }
    }

    @Override
    public boolean containsLPRRecordWithReference(String reference) {
        long cnt = jdbcTemplate.queryForLong("SELECT COUNT(1) FROM LPR WHERE lprReference = ?", reference);
        return (cnt > 0);
    }

    /**
     * Inserts a LPR record if the lprReference doesn't exists in the database,
     * If the lprReference exists already, all values are updated
     */
    private long insertOrUpdateBaseData(LPR lpr) throws DAOException {
        if (!(lpr.getRelationType().definesHospital() || lpr.getRelationType().definesDoctor())) {
            throw new AssertionError("Error in LPR module - every LPR must have either hospital or doctor org. id.");
        }

        long pk = 0;
        // Select to see if lprReference exists
        try {
	        pk = jdbcTemplate.queryForLong("SELECT pk FROM LPR where lprReference = ?", lpr.getLprReference());
        } catch (IncorrectResultSizeDataAccessException sizeEx) {
	        log.debug("Search for existing LPR with reference", "reference", lpr.getLprReference(), "numberOfExistingRows", "" + sizeEx.getActualSize());
	        // this is expected, and will just lead us to the else part of the following if statement
        }

        if(pk != 0) {
            // update
            namedParamsTemplate.update("UPDATE LPR SET patientCpr = :patientCpr, relationType = :relationType, organisationIdentifier = :organisationIdentifier, admittedStart = :admittedStart, admittedEnd = :admittedEnd WHERE lprReference = :lprReference", new LprSqlParamsSource(lpr));
        } else {
            // insert
            pk = basedataInserter.executeAndReturnKey(new LprSqlParamsSource(lpr)).longValue();
        }

        return pk;
    }

    @Override
    public LPR getUsingPrimaryKey(long pk) throws DAOException {
	    try {
		    LPR lprFromResultSet = jdbcTemplate.queryForObject("SELECT * FROM LPR WHERE pk=?", new LPRRowMapper(), pk);
		    log.debug("Found LPR using primary key", "primaryKey", Long.toString(pk), "LPR",
				    lprFromResultSet.toString());
		    return lprFromResultSet;
	    } catch (IncorrectResultSizeDataAccessException e) {
		    if (e.getActualSize() == 0) {
			    throw new DAOException("No LPR with primary key " + pk);
		    } else {
			    throw new DAOException("Found " + e.getActualSize() + " rows for primary key " + pk + ", expected only 1");
		    }
        } catch (RuntimeException e) {
            throw new DAOException("Unable to retrieve LPR with primary key " + pk, e);
        }
    }

	private class LprSqlParamsSource extends MapSqlParameterSource {
		public LprSqlParamsSource(LPR lpr) {
			super();

			super.addValue("patientCpr", lpr.getPatientCpr().getHashedCpr());
			super.addValue("relationType", lpr.getRelationType().name());

			if (lpr.getRelationType().definesHospital()) {
				super.addValue("organisationIdentifier", lpr.getHospitalOrganisationIdentifier().toString());
			}
			if (lpr.getRelationType().definesDoctor()) {
				super.addValue("organisationIdentifier", lpr.getDoctorOrganisationIdentifier().toString());
			}

			Timestamp startTimeStamp = new Timestamp(lpr.getAdmittedInterval().getStart().getMillis());
			super.addValue("admittedStart", startTimeStamp);

			if (lpr.getAdmittedInterval().isOpenEnded()) {
				super.addValue("admittedEnd", null);
			} else {
				Timestamp endTimeStamp = new Timestamp(lpr.getAdmittedInterval().getEnd().getMillis());
				super.addValue("admittedEnd", endTimeStamp);
			}

			super.addValue("lprReference", lpr.getLprReference());
		}
	}
}
