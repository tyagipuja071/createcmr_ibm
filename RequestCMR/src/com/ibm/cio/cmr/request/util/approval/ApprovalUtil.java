/**
 * 
 */
package com.ibm.cio.cmr.request.util.approval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.DefaultApprovalConditions;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Utility function for default approvals
 * 
 * @author Eduard Bernardo
 * 
 */
public class ApprovalUtil {

  private static final Logger LOG = Logger.getLogger(ApprovalUtil.class);

  public static List<Long> getDefaultApprovalsIds(EntityManager entityManager, String cmrIssuingCntry, long reqId, String requestTyp)
      throws Exception {
    List<Long> defaultApprovalIds = new ArrayList<Long>();

    if (CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(requestTyp) || CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(requestTyp)
        || CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(requestTyp)) {

      // for N, M, E requests - retrieve seqNos for the latest iteration
      List<Integer> seqNos = getSequenceNos(entityManager, reqId, requestTyp);
      for (Integer seqNo : seqNos) {
        List<Long> approvalIds = getDefaultApprovalIds(entityManager, cmrIssuingCntry, reqId, requestTyp, seqNo);
        if (!approvalIds.isEmpty()) {
          defaultApprovalIds.addAll(approvalIds);
        }
      }

      // return all unique default approval ids
      defaultApprovalIds = new ArrayList<>(new HashSet<>(defaultApprovalIds));
    } else {
      defaultApprovalIds = getDefaultApprovalIds(entityManager, cmrIssuingCntry, reqId, requestTyp, 0);
    }

    if (!defaultApprovalIds.isEmpty()) {
      LOG.debug("REQ_ID " + reqId + " : Total # of matched id/s = " + defaultApprovalIds.size());
      for (long id : defaultApprovalIds) {
        LOG.debug("REQ_ID " + reqId + " : Default Approval ID matched = " + id);
      }
    } else {
      LOG.debug("REQ_ID " + reqId + " : Total # of matched id/s = 0");
    }

    return defaultApprovalIds;
  }

  private static List<Long> getDefaultApprovalIds(EntityManager entityManager, String cmrIssuingCntry, long reqId, String requestTyp, int seqNo)
      throws Exception {
    List<Long> defaultApprovalIds = new ArrayList<Long>();

    // retrieve all default_approvals by request type and geo
    List<DefaultApprovals> approvals = getDefaultApprovals(entityManager, cmrIssuingCntry, reqId, requestTyp);
    for (DefaultApprovals approval : approvals) {

      boolean dataMatched = false;
      boolean addrMatched = false;
      String dataMainSql = getMainSql(requestTyp, false);
      String addrMainSql = getMainSql(requestTyp, true);

      // retrieve approval conditions per default approval
      List<DefaultApprovalConditions> dataConditions = getDefaultApprovalConditions(entityManager, approval.getId().getDefaultApprovalId(), false);
      List<DefaultApprovalConditions> addrConditions = getDefaultApprovalConditions(entityManager, approval.getId().getDefaultApprovalId(), true);

      // check data conditions
      if (dataConditions != null && !dataConditions.isEmpty()) {
        PreparedQuery query = new PreparedQuery(entityManager, dataMainSql);

        String whereClause = generateWhereClause(dataConditions, requestTyp);
        if (!StringUtils.isEmpty(whereClause)) {
          query.append(whereClause);
        }

        query.append("and admin.REQ_ID=" + reqId);
        if (query.exists()) {
          dataMatched = true;
        }
      } else {
        dataMatched = true;
      }

      // check addr conditions
      if (addrConditions != null && !addrConditions.isEmpty()) {
        PreparedQuery query = new PreparedQuery(entityManager, addrMainSql);

        String whereClause = generateWhereClause(addrConditions, requestTyp);
        if (!StringUtils.isEmpty(whereClause)) {
          query.append(whereClause);
        }

        query.append("and admin.REQ_ID=" + reqId);
        if (query.exists()) {
          addrMatched = true;
        }
      } else {
        addrMatched = true;
      }

      if (dataMatched && addrMatched) {
        defaultApprovalIds.add(approval.getId().getDefaultApprovalId());
      }
    }

    return defaultApprovalIds;
  }

  private static List<DefaultApprovals> getDefaultApprovals(EntityManager entityManager, String cmrIssuingCntry, long reqId, String requestTyp) {
    String sql = ExternalizedQuery.getSql("DEFAULT_APPROVALS.BY_REQUEST_TYP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQUEST_TYP", requestTyp);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("COUNTRY", "%" + (StringUtils.isEmpty(cmrIssuingCntry) ? "" : cmrIssuingCntry) + "%");

    List<DefaultApprovals> defaultApprovals = query.getResults(DefaultApprovals.class);
    return defaultApprovals;
  }

  private static List<DefaultApprovalConditions> getDefaultApprovalConditions(EntityManager entityManager, long defaultApprovalId, boolean addrInd) {
    String sql = ExternalizedQuery.getSql("DEFAULT_APPROVAL_CONDITIONS.BY_DEFAULT_APPROVAL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEFAULT_APPROVAL_ID", defaultApprovalId);

    if (addrInd)
      query.append("and ADDR_INDC = 'Y'");
    else
      query.append("and (ADDR_INDC is null or ADDR_INDC <> 'Y')");

    query.append("order by SEQUENCE_NO, CONDITION_LEVEL");

    List<DefaultApprovalConditions> conditions = query.getResults(DefaultApprovalConditions.class);
    return conditions;
  }

  private static String getMainSql(String requestTyp, boolean addrInd) {
    String mainSql = null;

    if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestTyp) && addrInd) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_CREATE.ADDR_IND");
    } else if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestTyp)) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_CREATE");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(requestTyp) && addrInd) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_UPDATE.ADDR_IND");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(requestTyp)) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_UPDATE");
    } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(requestTyp) && addrInd) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_MASS_CREATE.ADDR_IND");
    } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(requestTyp)) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_MASS_CREATE");
    } else if (CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(requestTyp) && addrInd) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_MASS_UPDATE.ADDR_IND");
    } else if (CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(requestTyp)) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_MASS_UPDATE");
    } else if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(requestTyp)) {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_MASS_UPDATE.BYENT");
    } else {
      mainSql = ExternalizedQuery.getSql("APPROVAL.DEFAULT_DELETE_REACTIVATE");
    }

    return mainSql;
  }

  private static String generateWhereClause(List<DefaultApprovalConditions> conditions, String requestTyp) {
    StringBuilder whereClauseSb = new StringBuilder();
    StringBuilder orClauseSb = new StringBuilder();
    int prevLevel = 0;
    boolean processingOR = false;

    // conditions - order by sequence and condition level!

    boolean hasProlifConditions = false;
    for (DefaultApprovalConditions condition : conditions) {
      if ("PROLIF_CHECKLIST.ANY".equals(condition.getDatabaseFieldName())) {
        hasProlifConditions = true;
      } else {
        String conditionSql = getConditionSql(condition, requestTyp);

        if (!StringUtils.isEmpty(conditionSql)) {
          if (prevLevel == 0 || condition.getConditionLevel() == prevLevel) {
            if (processingOR) {
              orClauseSb.append(" or ");
              orClauseSb.append(conditionSql);
            } else {
              orClauseSb.append(conditionSql);
            }
            processingOR = true;
          } else {
            if (processingOR) {
              whereClauseSb.append(" and ");
              whereClauseSb.append("(" + orClauseSb + ")");

              orClauseSb = new StringBuilder();
              orClauseSb.append(conditionSql);
            } else {
              whereClauseSb.append(" and ");
              whereClauseSb.append(conditionSql);
              processingOR = false;
            }
          }
        }
        prevLevel = condition.getConditionLevel();
      }
    }
    if (processingOR) {
      whereClauseSb.append(" and ");
      whereClauseSb.append("(" + orClauseSb + ")");
    }
    if (hasProlifConditions) {
      LOG.debug("Appending Prolif Checklist where clause...");
      String prolifWhere = ExternalizedQuery.getSql("APPROVAL.CHECKLIST.WHERE");
      whereClauseSb.append(prolifWhere);
    }

    return whereClauseSb.toString().trim();
  }

  private static String getConditionSql(DefaultApprovalConditions condition, String requestTyp) {
    StringBuilder sql = new StringBuilder();
    String fieldName = condition.getDatabaseFieldName().toUpperCase().trim();
    String operator = condition.getOperator().toUpperCase().trim();
    String value = !StringUtils.isEmpty(condition.getValue()) ? condition.getValue().trim() : "";
    boolean checkPrev = !StringUtils.isEmpty(condition.getPreviousValueIndc())
        && CmrConstants.YES_NO.Y.toString().equalsIgnoreCase(condition.getPreviousValueIndc()) ? true : false;

    // matching for D, R requests
    if (CmrConstants.REQ_TYPE_DELETE.equalsIgnoreCase(requestTyp.trim()) || CmrConstants.REQ_TYPE_REACTIVATE.equalsIgnoreCase(requestTyp.trim())) {
      if ("EQ".equals(operator) && fieldName.contains("CMR_ISSUING_CNTRY")) {
        sql = sql.append("data.CMR_ISSUING_CNTRY" + " = '" + value + "'");
      }
      return sql.toString().trim();
    }

    // filter cntry for M/E, admin fields, and check if use previous values
    if ((CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(requestTyp.trim())
        || CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(requestTyp.trim())) && fieldName.contains("CMR_ISSUING_CNTRY")) {
      fieldName = "data.CMR_ISSUING_CNTRY";
    } else if (!fieldName.contains("ADMIN.") && !"CHG".equalsIgnoreCase(operator)) {
      if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(requestTyp.trim()) && checkPrev)
        fieldName = "old." + fieldName;
      else {
        // speical rquirement:JP MassUpdate CREATCMR-9438
        if (CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(requestTyp.trim()) && "Y".equals(value) && "TAX_CD3".equals(fieldName))
          fieldName = "data." + fieldName;
        else {
          fieldName = "curr." + fieldName;
        }
      }
    }

    switch (operator) {
    case "EQ":
      sql = sql.append(fieldName + " = '" + value + "'");
      break;
    case "LT":
      sql = sql.append("cast(" + fieldName + " as integer) < " + value);
      break;
    case "LTE":
      sql = sql.append("cast(" + fieldName + " as integer) <= " + value);
      break;
    case "GT":
      sql = sql.append("cast(" + fieldName + " as integer) > " + value);
      break;
    case "GTE":
      sql = sql.append("cast(" + fieldName + " as integer) >= " + value);
      break;
    case "NE":
      if (fieldName.equals("ADMIN.REQUESTER_ID")) {
        sql = sql.append(fieldName + " <> LOWER('" + value + "')");
      } else {
        sql = sql.append(fieldName + " <> '" + value + "'");
      }
      break;
    case "*":
      sql = sql.append(fieldName + " is not null");
      break;
    case "$":
      sql = sql.append("trim(nvl(" + fieldName + ", '')) = ''");
      break;
    case "IN":
      sql = sql.append(fieldName + " in " + getClauseForContains(value));
      break;
    case "NIN":
      sql = sql.append(fieldName + " not in " + getClauseForContains(value));
      break;
    case "CON":
      sql = sql.append("trim(upper(" + fieldName + ")) like '%" + value.toUpperCase() + "%'");
      break;
    case "NCO":
      sql = sql.append("trim(upper(" + fieldName + ")) not like '%" + value.toUpperCase() + "%'");
      break;
    case "STA":
      sql = sql.append("trim(upper(" + fieldName + ")) like '" + value.toUpperCase() + "%'");
      break;
    case "CHG":
      if (fieldName.equals("ADMIN.MAIN_CUST_NM1")) {
        sql = sql.append("nvl(" + fieldName + ",'') <> nvl(ADMIN.OLD_CUST_NM1,'')");
      } else if (fieldName.equals("ADMIN.MAIN_CUST_NM2")) {
        sql = sql.append("nvl(" + fieldName + ",'') <> nvl(ADMIN.OLD_CUST_NM2,'')");
      } else {
        sql = sql.append("nvl(curr." + fieldName + ",'') <> nvl(old." + fieldName + ",'')");
      }
      break;
    }

    return sql.toString().trim();
  }

  private static String getClauseForContains(String value) {
    StringBuilder result = new StringBuilder();
    List<String> values = new ArrayList<String>();

    if (!StringUtils.isEmpty(value)) {
      for (String val : value.split(",")) {
        values.add("'" + val + "'");
      }
      result.append("(");
      result.append(StringUtils.join(values, ","));
      result.append(")");
    }

    return result.toString();
  }

  private static List<Integer> getSequenceNos(EntityManager entityManager, long reqId, String requestTyp) {
    String sql = null;
    if (CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(requestTyp)) {
      sql = ExternalizedQuery.getSql("MASS_CREATE.GET_SEQNO");
    } else {
      sql = ExternalizedQuery.getSql("MASS_UPDT.GET_SEQNO");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    return query.getResults(Integer.class);
  }

}
