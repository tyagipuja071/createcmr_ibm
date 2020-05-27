/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.la.br;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.DuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.DuplicateContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * {@link AutomationElement} implementation for the duplicate request check for
 * brazil
 * 
 * @author RangoliSaxena
 * 
 */
public class BrazilDupCMRCheckElement extends DuplicateCheckElement {

  private static final Logger LOG = Logger.getLogger(BrazilDupCMRCheckElement.class);

  public BrazilDupCMRCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  /**
   * Checks for duplicate cmr based on vat
   * 
   * @param entityManager
   * @param requestData
   * @param addr
   * @param sqlKey
   */
  private List<DuplicateContainer> getDupCMRList(EntityManager entityManager, String vat, String addrType, String sqlKey) {
    String sql = ExternalizedQuery.getSql(sqlKey);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("VAT", vat);
    query.setParameter("KTOKD", addrType);
    query.setForReadOnly(true);
    List<DuplicateContainer> matches = new ArrayList<DuplicateContainer>();
    DuplicateContainer match = null;
    List<Object[]> results = query.getResults();
    if (results != null) {
      for (Object[] result : results) {
        // 0 = cmr no
        // 1 = name1
        // 2 = name2
        // 3 = vat
        match = new DuplicateContainer();
        match.setVat((String) result[3]);
        if (match.getVat().equals(vat)) {
          match.setCmrNo((String) result[0]);
          match.setName1((String) result[1]);
          match.setName2((String) result[2]);
          matches.add(match);
        }
      }
    }
    return matches;
  }

  /**
   * Checks for duplicate cmr based on brazil's logic for UI integ
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   */
  public List<DuplicateContainer> checkCMRDuplicacy(EntityManager entityManager, String vatZS01, String vatZI01, String scenario) throws Exception {
    LOG.debug("Start Executing Duplicate CMR Check for Brazil from UI.");
    String sqlKey = "";
    List<DuplicateContainer> cmrList = new ArrayList<DuplicateContainer>();
    List<DuplicateContainer> cmrListInstallAts = new ArrayList<DuplicateContainer>();
    List<DuplicateContainer> cmrListLeasing = new ArrayList<DuplicateContainer>();

    if ("5COMP".equalsIgnoreCase(scenario) || "5PRIP".equalsIgnoreCase(scenario) || "SOFTL".equalsIgnoreCase(scenario)
        || "CROSS".equalsIgnoreCase(scenario) || "INTER".equalsIgnoreCase(scenario) || "NEW".equalsIgnoreCase(scenario)
        || "IBMEM".equalsIgnoreCase(scenario) || "PRIPE".equalsIgnoreCase(scenario)) {
      return new ArrayList<DuplicateContainer>();
    } else {
      if (!"INTER".equalsIgnoreCase(scenario) && !"BUSPR".equalsIgnoreCase(scenario) && !"LEASI".equalsIgnoreCase(scenario)) {
        sqlKey = "BR.AUTO.GET_DUP_CMR_VAT_OTHERS";
        cmrList = getDupCMRList(entityManager, vatZS01, "ZS01", sqlKey);
      } else if ("INTER".equalsIgnoreCase(scenario)) {
        sqlKey = "BR.AUTO.GET_DUP_CMR_VAT";
        cmrList = getDupCMRList(entityManager, vatZS01, "ZS01", sqlKey);
      } else if ("BUSPR".equalsIgnoreCase(scenario)) {
        sqlKey = "BR.AUTO.GET_DUP_CMR_VAT_BP";
        cmrList = getDupCMRList(entityManager, vatZS01, "ZS01", sqlKey);
      } else if ("LEASI".equalsIgnoreCase(scenario)) {
        sqlKey = "BR.AUTO.GET_DUP_CMR_VAT";
        cmrList = getDupCMRList(entityManager, vatZS01, "ZS01", sqlKey);
        if (cmrList != null && cmrList.size() > 0 && vatZI01 != null) {
          cmrListInstallAts = getDupCMRList(entityManager, vatZI01, "ZI01", sqlKey);
          for (DuplicateContainer cmr : cmrList) {
            boolean found = false;
            for (DuplicateContainer cmrI : cmrListInstallAts) {
              if (cmrI.getCmrNo().equals(cmr.getCmrNo())) {
                found = true;
              }
            }
            if (found) {
              cmrListLeasing.add(cmr);
            }
          }
          if (cmrListLeasing == null || cmrListLeasing.size() == 0) {
            return new ArrayList<DuplicateContainer>();
          }
        }
      }

      if (cmrListLeasing != null && cmrListLeasing.size() > 0) {
        LOG.debug("Duplicate CMR Matches found For Leasing scenario..");
        LOG.debug(cmrListLeasing.size() + " record(s) found.");
        if (cmrListLeasing.size() > 20) {
          cmrListLeasing = cmrListLeasing.subList(0, 20);
        }
        LOG.debug("End Executing Duplicate CMR Check for Brazil from UI.");
        return cmrListLeasing;
      } else if (cmrList != null && cmrList.size() > 0) {
        LOG.debug(cmrList.size() + " record(s) found.");
        if (cmrList.size() > 20) {
          cmrList = cmrList.subList(0, 20);
        }
        LOG.debug("End Executing Duplicate CMR Check for Brazil from UI.");
        return cmrList;
      }
    }
    LOG.debug("End Executing Duplicate CMR Check for Brazil from UI.");
    return cmrList;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String zs01Kunnr = zs01.getSapNo() != null ? zs01.getSapNo().substring(1) : null;
    // build result
    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    MatchingOutput output = new MatchingOutput();

    try {
      ChangeLogListener.setManager(entityManager);

      // if the request is an update, do not check duplicate cmr except for
      // reactivate
      if ("U".equals(admin.getReqType()) && !"REAC".equalsIgnoreCase(admin.getReqReason())) {
        result.setDetails("Skipping request check for Update Requests.");
        result.setResults("Skipped");
        result.setOnError(false);
      } else if ("C".equals(admin.getReqType()) && ("5COMP".equalsIgnoreCase(data.getCustSubGrp()) || "5PRIP".equalsIgnoreCase(data.getCustSubGrp())
          || "SOFTL".equalsIgnoreCase(data.getCustSubGrp()) || "CROSS".equalsIgnoreCase(data.getCustSubGrp())
          || "INTER".equalsIgnoreCase(data.getCustSubGrp()) || "NEW".equalsIgnoreCase(data.getCustSubGrp())
          || "IBMEM".equalsIgnoreCase(data.getCustSubGrp()) || "PRIPE".equalsIgnoreCase(data.getCustSubGrp()))) {
        result.setDetails("Skipping request check for scenarios.");
        result.setResults("Skipped");
        result.setOnError(false);
      } else {
        Addr soldTo = requestData.getAddress("ZS01");
        Addr installAt = requestData.getAddress("ZI01");
        List<DuplicateContainer> cmrList = checkCMRDuplicacy(entityManager, soldTo.getVat(), installAt != null ? installAt.getVat() : "",
            data.getCustSubGrp());

        if (cmrList != null && !cmrList.isEmpty()) {
          StringBuilder details = new StringBuilder();
          details.append(cmrList.size() + " record(s) found. ");
          if (cmrList.size() > 10) {
            cmrList = cmrList.subList(0, 10);
            details.append("Showing top 10 matches only.");
          }
          int itemNo = 1;
          details.append("\n");
          for (DuplicateContainer cmr : cmrList) {
            output.addMatch(getProcessCode(), "CMR_NO", cmr.getCmrNo(), "CMR Matching", "100%", "CMR", itemNo);
            details.append("\nCMR No. = " + cmr.getCmrNo()).append("\n");
            details.append("Name = " + cmr.getName1()).append(!StringUtils.isBlank(cmr.getName2()) ? " " + cmr.getName2() : "").append("\n");
            details.append("VAT/CNPJ = " + cmr.getVat()).append("\n");
            engineData.put("cmrMatching", cmr.getCmrNo());
            itemNo++;
          }
          result.setResults("Matches Found.");
          result.setProcessOutput(output);
          if (!StringUtils.isEmpty(admin.getDupCmrReason())) {
            details.append("\n\nDuplicate CMR reason provided: " + admin.getDupCmrReason());
          } else {

            // add support to override duplicate CMR checks later
            requestData.getAdmin().setMatchIndc("C");
            engineData.addRejectionComment("DUPC", "Duplicate CMR matches found.", StringUtils.join(cmrList, ", "), zs01Kunnr);
            result.setOnError(true);
          }
          result.setDetails(details.toString().trim());
        } else {
          result.setDetails("Duplicate Request Check passed successfully.");
          result.setResults("No Matches Found");
          result.setOnError(false);
        }
      }
      if (!result.isOnError()) {
        if ("C".equals(admin.getReqType())) {
          // only update the scorecard if it's a create request. for update
          // don't touch the scorecard
          // let UI scorecard or import cmr element scorecard retain their
          // values
          AppUser user = (AppUser) engineData.get(AutomationEngineData.APP_USER);
          Scorecard scorecard = requestData.getScorecard();
          scorecard.setFindCmrResult("No Data");
          scorecard.setFindCmrTs(SystemUtil.getActualTimestamp());
          scorecard.setFindCmrUsrId(user.getIntranetId());
          scorecard.setFindCmrUsrNm(user.getBluePagesName());
          entityManager.merge(scorecard);
        }
      }
    } finally {
      ChangeLogListener.clearManager();
    }
    return result;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.BR_DUP_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "Brazil - Duplicate CMR Check";
  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.Matching;
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    String field = match.getId().getMatchKeyName();
    Data data = requestData.getData();
    if (Arrays.asList("CMR_NO").contains(field)) {
      setEntityValue(data, field, match.getId().getMatchKeyValue());
    }
    return true;
  }

  /**
   * Sets the data value by finding the relevant column having the given field
   * name
   * 
   * @param entity
   * @param fieldName
   * @param value
   */
  protected void setEntityValue(Object entity, String fieldName, Object value) {
    boolean fieldMatched = false;
    for (Field field : entity.getClass().getDeclaredFields()) {
      // match the entity name to field name
      fieldMatched = false;
      Column col = field.getAnnotation(Column.class);
      if (col != null && fieldName.toUpperCase().equals(col.name().toUpperCase())) {
        fieldMatched = true;
      } else if (field.getName().toUpperCase().equals(fieldName.toUpperCase())) {
        fieldMatched = true;
      }
      if (fieldMatched) {
        try {
          field.setAccessible(true);
          try {
            Method set = entity.getClass().getDeclaredMethod("set" + (field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)),
                value != null ? value.getClass() : String.class);
            if (set != null) {
              set.invoke(entity, value);
            }
          } catch (Exception e) {
            field.set(entity, value);
          }
        } catch (Exception e) {
          LOG.trace("Field " + fieldName + " cannot be assigned. Error: " + e.getMessage());
        }
      }
    }
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }

}
