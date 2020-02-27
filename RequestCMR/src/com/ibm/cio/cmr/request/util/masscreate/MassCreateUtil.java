/**
 * 
 */
package com.ibm.cio.cmr.request.util.masscreate;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.entity.MassCreatePK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Utility functions for mass create
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateUtil {

  private static final Logger LOG = Logger.getLogger(MassCreateUtil.class);

  /**
   * CReates MASS_CREATE, MASS_CREATE_DATA, and MASS_CREATE_ADDR records based
   * on the file container
   * 
   * @param file
   * @param entityManager
   */
  public static synchronized void createMassCreateRecords(MassCreateFile file, EntityManager entityManager) {
    LOG.info("Creating Mass Create records for parsed file..");
    for (MassCreateFileRow row : file.getRows()) {
      LOG.debug("Mass Create: ID " + file.getReqId() + " Iteration: " + file.getIterationId() + " Sequence: " + row.getSeqNo());
      MassCreate mc = new MassCreate();
      MassCreatePK mcPk = new MassCreatePK();
      mcPk.setSeqNo(row.getSeqNo());
      mcPk.setIterationId(file.getIterationId());
      mcPk.setParReqId(file.getReqId());
      mc.setId(mcPk);

      mc.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_READY);

      MassCreateData data = row.getData();
      if (!StringUtils.isBlank(data.getEnterprise()) || !StringUtils.isBlank(data.getAffiliate()) || !StringUtils.isBlank(data.getIccTaxClass())
          || !StringUtils.isBlank(data.getIccTaxExemptStatus()) || !StringUtils.isBlank(data.getNonIbmCompanyInd())) {
        mc.setAutoUpdt(CmrConstants.YES_NO.Y.toString());
      } else {
        mc.setAutoUpdt(CmrConstants.YES_NO.N.toString());
      }
      entityManager.persist(mc);
      entityManager.persist(data);
      for (MassCreateAddr addr : row.getAddresses()) {
        if (addr.isVirtual()) {
          LOG.debug("Address " + addr.getId().getAddrType() + " is virtual. Will not create.");
        } else {
          entityManager.persist(addr);
        }
      }

      entityManager.flush();
    }
  }

  /**
   * Queries the database and converts the current file into error log form. The
   * records without errors will not be included.
   * 
   * @param entityManager
   * @param reqId
   * @throws Exception
   */
  public static void convertToErrorLog(EntityManager entityManager, OutputStream targetStream, long reqId, int iterationId) throws Exception {
    String sql = ExternalizedQuery.getSql("MC.GET_MC_METADATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITER_ID", iterationId);

    List<CompoundEntity> massCreateList = query.getCompundResults(MassCreate.class, MassCreate.MASS_CREATE_META_MAPPING);
    if (massCreateList != null && !massCreateList.isEmpty()) {
      // get the list, then track the errors
      MassCreate record = null;
      String fileName = null;
      Map<Integer, String> colMsgMap = new HashMap<Integer, String>();
      for (CompoundEntity result : massCreateList) {
        record = result.getEntity(MassCreate.class);
        fileName = (String) result.getValue("FILE_NAME");
        if (CmrConstants.MASS_CREATE_ROW_STATUS_FAIL.equals(record.getRowStatusCd())) {
          colMsgMap.put(record.getId().getSeqNo() - 1, record.getErrorTxt());
        }
      }
      MassCreateFileParser parser = new MassCreateFileParser();
      parser.copyErrorRows(fileName, colMsgMap, targetStream, reqId, iterationId);
    } else {
      throw new CmrException(MessageUtil.INFO_ERROR_LOG_EMPTY);
    }

  }
}
