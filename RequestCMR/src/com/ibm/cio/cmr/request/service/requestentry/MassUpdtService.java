package com.ibm.cio.cmr.request.service.requestentry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class MassUpdtService extends BaseService<MassUpdateModel, MassUpdt> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MassUpdtService.class);
  }

  @Override
  protected void performTransaction(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException, Exception {
    String cmrList = model.getCmrList();

    if ("REMOVE_CMR".equals(model.getAction())) {
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.REACT_DEL_REMOVE_CMR");
      PreparedQuery q = new PreparedQuery(entityManager, sql);
      List<KeyContainer> keys = extractKeys(model);
      for (KeyContainer key : keys) {
        model.setCmrNo(key.getKey("cmrNo"));
        log.debug("Removing CMR " + key.getKey("cmrNo") + " for parent req id : " + model.getParReqId());
        removeCMRRecords(model, entityManager, request);
        /*
         * q.setParameter("REQ_ID", model.getParReqId());
         * q.setParameter("CMR_NO", key.getKey("cmrNo")); q.executeSql();
         */
      }
    } else if ("ADD_CMR".equals(model.getAction())) {

      String[] cmrArray = cmrList.split(",");
      List<String> cmractualList = new ArrayList<>();
      for (String singleCmr : cmrArray) {
        if (singleCmr.trim().length() > 0) {
          cmractualList.add(singleCmr.trim());
        }
      }
      String maxRows = "0";
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.REACTIVATE.GET_CMR_LIST");

      PreparedQuery query = new PreparedQuery(entityManager, sql);

      query.setParameter("REQ_ID", model.getParReqId());

      List<String> rs = query.getResults(String.class);
      if ("R".equalsIgnoreCase(model.getParReqType())) {
        maxRows = SystemConfiguration.getValue("REACTIVE_MAX_ROWS");
      } else if ("D".equalsIgnoreCase(model.getParReqType())) {
        maxRows = SystemConfiguration.getValue("DELETE_MAX_ROWS");
      }

      List<String> uniquecmrGUIList = new ArrayList<>();
      List<String> uniquecmrActualList = new ArrayList<>();
      // check for fresh duplicate values
      for (int i = 0; i < cmractualList.size(); i++) {
        if (uniquecmrGUIList.isEmpty() || (!uniquecmrGUIList.isEmpty() && !uniquecmrGUIList.contains(cmractualList.get(i))))
          uniquecmrGUIList.add(cmractualList.get(i));
      }

      // check for duplicate values from DB
      for (int i = 0; i < uniquecmrGUIList.size(); i++) {
        if ((rs == null || rs.isEmpty()) || (!rs.isEmpty() && !rs.contains(uniquecmrGUIList.get(i)))) {
          uniquecmrActualList.add(uniquecmrGUIList.get(i));
          // cmractualList.remove(uniquecmrList.get(i));
        }
      }

      // make cmractualList contain the same values as uniquecmrActualList
      cmractualList = uniquecmrActualList;
      // free up memory by clearing uniquecmrList
      // uniquecmrList.clear();
      for (int i = 0; i < cmractualList.size(); i++) {
        if (cmractualList != null && cmractualList.size() != 0 && cmractualList.get(i).length() > 7) {
          throw new CmrException(MessageUtil.ERROR_CMR_LENGTH, cmractualList.get(i));
        }

      }

      if (cmractualList != null && rs != null && ((cmractualList.size() + rs.size()) > (Integer.parseInt(maxRows)))) {
        throw new CmrException(MessageUtil.ERROR_INVALID_CMR_COUNT, maxRows);
      }

      // Defect 1751169-Temp Fix
      if ("R".equalsIgnoreCase(model.getParReqType())) {
        String cmrIssuingCntry = "";
        String issuingCntry = "";
        boolean cntryFlag = false;
        sql = ExternalizedQuery.getSql("DATA_ISSUING_CNTRY");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", model.getParReqId());

        List<Object[]> resultsCntry = query.getResults();
        if (resultsCntry != null && resultsCntry.size() > 0) {
          Object[] resultCntry = resultsCntry.get(0);
          cmrIssuingCntry = resultCntry[0] != null ? (String) resultCntry[0] : "";
          if ("838".equals(cmrIssuingCntry) || "866".equals(cmrIssuingCntry) || "754".equals(cmrIssuingCntry) || "758".equals(cmrIssuingCntry))
            cntryFlag = true;
        }

        if (cntryFlag) {
          sql = ExternalizedQuery.getSql("REACTIVATE_KNA1_COUNT");
          String sqlLegacy = ExternalizedQuery.getSql("REACTIVATE_LEGACY_COUNT");
          String sqlReservedCMR = ExternalizedQuery.getSql("LD.REACDEL_RESERVED_CMR_CHECK");
          int legacyCount = 0;
          String katr10 = "";
          boolean rdcFlag = false;

          List<Object[]> countListLegacy;
          Object[] countObjLegacy;

          query = new PreparedQuery(entityManager, sql);
          PreparedQuery queryLegacy = new PreparedQuery(entityManager, sqlLegacy);
          PreparedQuery queryReservedCMR = new PreparedQuery(entityManager, sqlReservedCMR);

          for (int i = 0; i < cmractualList.size(); i++) {
            rdcFlag = false;
            issuingCntry = cmrIssuingCntry;
            query.setParameter("CMR_NO", cmractualList.get(i));
            query.setParameter("CNTRY", issuingCntry);
            query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            List<Object[]> results = query.getResults();

            if (results != null && results.size() > 0) {
              Object[] result = results.get(0);
              katr10 = result[0] != null ? (String) result[0] : "";
              if (!"".equals(katr10)) {
                rdcFlag = true;
                throw new CmrException(MessageUtil.ERROR_CMR_REACTIVATE_RDC, cmractualList.get(i));
              }
            } else {
              rdcFlag = true;
              throw new CmrException(MessageUtil.ERROR_CMR_REACTIVATE_RDC, cmractualList.get(i));
            }

            if (rdcFlag)
              continue;

            if ("754".equals(issuingCntry))
              issuingCntry = "866";

            queryLegacy.setParameter("CMR_NO", cmractualList.get(i));
            queryLegacy.setParameter("COUNTRY", issuingCntry);
            countListLegacy = queryLegacy.getResults();
            countObjLegacy = countListLegacy.get(0);
            legacyCount = (Integer) countObjLegacy[0];
            if (legacyCount == 0)
              throw new CmrException(MessageUtil.ERROR_CMR_REACTIVATE_RDC, cmractualList.get(i));

            // reserved cmr check
            queryReservedCMR.setParameter("CMR_NO", cmractualList.get(i));
            queryReservedCMR.setParameter("COUNTRY", issuingCntry);
            queryReservedCMR.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            if (queryReservedCMR.exists())
              throw new CmrException(MessageUtil.ERROR_CMR_REACTIVATE_RDC, cmractualList.get(i));
          }
        }
      }

      for (int i = 0; i < cmractualList.size(); i++) {
        createMassUpdt(this, entityManager, model, cmractualList.get(i), model.getParReqId());
      }

      if (cmractualList != null && cmractualList.size() > 0) {
        model.setDisplayMsg(true);
      } else {
        model.setDisplayMsg(false);
      }

    }
  }

  /**
   * Create the MASS UPDATE entry for the request in MassUpdt
   * 
   * @param service
   * @param entityManager
   * @param model
   * @param cmr
   * @param parReqId
   * @throws CmrException
   * @throws SQLException
   */
  public void createMassUpdt(BaseService<?, ?> service, EntityManager entityManager, MassUpdateModel model, String cmr, long parReqId)
      throws CmrException, SQLException {
    this.log.info("Creating Mass Update record for  Parent Req Id" + parReqId);
    MassUpdt massUpdt = new MassUpdt();
    MassUpdtPK massUpdtpk = new MassUpdtPK();
    int maxSeqNo = 0;

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.REACTIVATE.GET_MAX_SEQ_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", parReqId);
    List<Integer> rs = query.getResults(Integer.class);
    if (rs != null) {
      for (Integer maxSNo : rs) {
        maxSeqNo = (maxSNo != null) ? maxSNo : 0;
      }
    } else {
      maxSeqNo = 0;
    }

    massUpdtpk.setParReqId(parReqId);
    massUpdtpk.setSeqNo(maxSeqNo + 1);
    massUpdtpk.setIterationId(1);

    massUpdt.setRowStatusCd("READY");
    massUpdt.setCmrNo(cmr);
    massUpdt.setId(massUpdtpk);

    service.createEntity(massUpdt, entityManager);
  }

  public void removeCMRRecords(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.REACTIVATE.GET_REMOVE_CMR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getParReqId());
    query.setParameter("CMR_NO", model.getCmrNo());
    List<MassUpdt> cmrList = query.getResults(MassUpdt.class);
    for (MassUpdt cmrRec : cmrList) {
      deleteEntity(cmrRec, entityManager);
    }
  }

  @Override
  protected List<MassUpdateModel> doSearch(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<MassUpdateModel> results = new ArrayList<MassUpdateModel>();
    long parReqId = model.getParReqId();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.REACTIVATE.GET_CMR_LIST");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getParReqId());

    List<String> rs = query.getResults(String.class);

    MassUpdateModel massUpdtModel = null;
    for (String massUpdtRec : rs) {
      massUpdtModel = new MassUpdateModel();
      massUpdtModel.setCmrNo(massUpdtRec);
      massUpdtModel.setParReqId(parReqId);
      // copyValuesFromEntity(massUpdtRec, massUpdtModel);
      results.add(massUpdtModel);
    }
    return results;

  }

  @Override
  protected MassUpdt createFromModel(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    MassUpdt admin = new MassUpdt();
    MassUpdtPK pk = new MassUpdtPK();
    admin.setId(pk);
    copyValuesToEntity(model, admin);
    return admin;
  }

  @Override
  protected MassUpdt getCurrentRecord(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  protected List<MassUpdateModel> doSearchFailed(long parReqId, int iterationId, EntityManager entityManager) throws Exception {
    List<MassUpdateModel> results = new ArrayList<MassUpdateModel>();

    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.FAIL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", parReqId);
    query.setParameter("ITERATION_ID", iterationId);
    List<MassUpdt> rs = query.getResults(MassUpdt.class);

    MassUpdateModel massModel = null;
    for (MassUpdt massUpdt : rs) {
      massModel = new MassUpdateModel();
      copyValuesFromEntity(massUpdt, massModel);

      results.add(massModel);
    }

    return results;

  }

}
