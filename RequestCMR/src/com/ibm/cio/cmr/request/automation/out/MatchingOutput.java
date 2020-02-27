/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.MatchingElement;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.AutomationMatchingPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Represents a matched record by an {@link AutomationElement} process
 * 
 * @author JeffZAMORA
 * 
 */
public class MatchingOutput implements AutomationOutput {

  private static final Logger LOG = Logger.getLogger(MatchingOutput.class);

  private List<MatchRecord> matches = new ArrayList<MatchRecord>();

  private List<AutomationMatching> recordedMatches = new ArrayList<AutomationMatching>();

  /**
   * Adds a matching record to the output
   * 
   * @param matchKeyName
   * @param matchKeyId
   * @param matchGradeType
   * @param matchGradeValue
   * @param recordType
   */
  public void addMatch(String processCode, String matchKeyName, String matchKeyValue, String matchGradeType, String matchGradeValue,
      String recordType, int itemNo) {
    MatchRecord record = new MatchRecord();
    record.setMatchGradeType(matchGradeType);
    record.setMatchGradeValue(matchGradeValue);
    record.setMatchKeyName(matchKeyName);
    record.setMatchKeyValue(matchKeyValue);
    record.setRecordType(recordType);
    record.setItemNo(itemNo);
    record.setProcessCode(processCode);
    this.matches.add(record);
  }

  @Override
  public void apply(EntityManager entityManager, AppUser user, RequestData requestData, long resultId, long itemNo, String processCd,
      boolean activeEngine) throws CmrException {
    long reqId = requestData.getAdmin().getId().getReqId();
    if (!activeEngine) {
      LOG.debug("Querying match records from DB..");
      // the data here is not on the container, query
      String sql = ExternalizedQuery.getSql("AUTOMATION.GET_MATCHING_ALL");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("RESULT_ID", resultId);
      // query.setParameter("ITEM_NO", itemNo);
      query.setParameter("PROCESS_CD", processCd);
      query.setForReadOnly(true);

      List<AutomationMatching> matches = query.getResults(AutomationMatching.class);
      if (matches != null) {
        for (AutomationMatching match : matches) {
          if (itemNo == match.getId().getItemNo()) {
            addMatch(match.getId().getProcessCd(), match.getId().getMatchKeyName(), match.getId().getMatchKeyValue(), match.getMatchGradeTyp(),
                match.getMatchGradeValue(), match.getRecordTyp(), match.getId().getItemNo());
          }

          match.setImportedIndc("N");
          match.setLastUpdtBy(user.getIntranetId());
          match.setLastUpdtTs(SystemUtil.getActualTimestamp());
          entityManager.merge(match);
        }

      }
    }

    for (MatchRecord match : this.matches) {
      // try to get the element
      if (match.getItemNo() != itemNo) {
        break;
      }
      Admin admin = requestData.getAdmin();
      Class<? extends AutomationElement<?>> elementClass = AutomationElementRegistry.getInstance().get(match.getProcessCode());
      if (elementClass != null) {
        AutomationElement<?> element = initializeElement(elementClass);
        if (element != null && element instanceof MatchingElement) {
          LOG.debug("Importing highest match (Item " + match.getItemNo() + ") from the matches..");
          LOG.trace("Importing match " + match.getMatchKeyName() + "=" + match.getMatchKeyValue() + " to Request " + reqId);
          MatchingElement matchingElement = (MatchingElement) element;
          AutomationMatching matchRecord = findMatchRecord(entityManager, match, resultId, match.getItemNo());
          if (!StringUtils.isBlank(matchRecord.getId().getMatchKeyName()) && !StringUtils.isBlank(matchRecord.getId().getMatchKeyValue())) {
            boolean imported = matchingElement.importMatch(entityManager, requestData, matchRecord);
            if (imported) {

              LOG.debug("Data imported from matching. Saving request records..");

              matchRecord.setImportedIndc("Y");
              matchRecord.setLastUpdtBy(user.getIntranetId());
              matchRecord.setLastUpdtTs(SystemUtil.getActualTimestamp());
              entityManager.merge(matchRecord);

              admin.setLastUpdtBy(user.getIntranetId());
              admin.setLastUpdtTs(SystemUtil.getActualTimestamp());
              entityManager.merge(admin);

              Data data = requestData.getData();
              entityManager.merge(data);
            }
          }
        }
      }
    }
  }

  @Override
  public void recordData(EntityManager entityManager, long resultId, AppUser user, RequestData requestData) throws Exception {
    if (this.matches != null && !this.matches.isEmpty()) {
      for (MatchRecord record : this.matches) {
        if (!StringUtils.isBlank(record.getMatchKeyName()) && !StringUtils.isBlank(record.getMatchKeyValue())) {
          AutomationMatching match = new AutomationMatching();
          AutomationMatchingPK pk = new AutomationMatchingPK();
          pk.setAutomationResultId(resultId);
          pk.setMatchKeyName(record.getMatchKeyName());
          pk.setMatchKeyValue(record.getMatchKeyValue());
          pk.setProcessCd(record.getProcessCode());
          pk.setItemNo(record.getItemNo());
          match.setId(pk);

          Timestamp ts = SystemUtil.getActualTimestamp();
          match.setCreateBy(user.getIntranetId());
          match.setCreateTs(ts);
          match.setImportedIndc("N");
          match.setLastUpdtBy(user.getIntranetId());
          match.setLastUpdtTs(ts);
          match.setMatchGradeTyp(record.getMatchGradeType());
          match.setMatchGradeValue(record.getMatchGradeValue());
          match.setRecordTyp(record.getRecordType());

          LOG.trace("Creating match record for " + record.getMatchKeyName() + " = " + record.getMatchKeyValue());
          entityManager.persist(match);

          this.recordedMatches.add(match);
        }
      }
    }
  }

  /**
   * Finds the corresponding {@link AutomationMatching} record for the given
   * inputs
   * 
   * @param entityManager
   * @param record
   * @param resultId
   * @param itemNo
   * @return
   */
  private AutomationMatching findMatchRecord(EntityManager entityManager, MatchRecord record, long resultId, int itemNo) {
    AutomationMatchingPK pk = new AutomationMatchingPK();
    pk.setAutomationResultId(resultId);
    pk.setMatchKeyName(record.getMatchKeyName());
    pk.setMatchKeyValue(record.getMatchKeyValue());
    pk.setProcessCd(record.getProcessCode());
    pk.setItemNo(itemNo);

    // try first to match with current recorded data to avoid 2x queries
    AutomationMatching match = findFromRecordedData(record, resultId, itemNo);
    if (match == null) {
      match = entityManager.find(AutomationMatching.class, pk);
    }
    if (match == null) {
      match = new AutomationMatching();
      match.setId(pk);
      match.setMatchGradeTyp(record.getMatchGradeType());
      match.setMatchGradeValue(record.getMatchGradeValue());
      match.setRecordTyp(record.getRecordType());
    }
    return match;
  }

  /**
   * For an active engine, get the matched {@link AutomationMatching} record
   * from recorded data
   * 
   * @param record
   * @param resultId
   * @param itemNo
   * @return
   */
  private AutomationMatching findFromRecordedData(MatchRecord record, long resultId, int itemNo) {
    for (AutomationMatching match : this.recordedMatches) {
      if (match.getId().getAutomationResultId() == resultId && match.getId().getItemNo() == itemNo
          && record.getMatchKeyName().equals(match.getId().getMatchKeyName()) && record.getMatchKeyValue().equals(match.getId().getMatchKeyValue())) {
        LOG.trace("Match record found in recorded data..");
        return match;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private AutomationElement<?> initializeElement(Class<? extends AutomationElement<?>> elementClass) {
    if (elementClass != null) {
      try {
        Constructor<AutomationElement<?>> constructor = (Constructor<AutomationElement<?>>) elementClass.getConstructor(String.class, String.class,
            boolean.class, boolean.class);
        return constructor.newInstance("DUMMY", null, false, false);
      } catch (Exception e) {
        LOG.warn("Element for class " + elementClass.getSimpleName() + " cannot be determined via registry.");
      }
    }
    return null;
  }

}
