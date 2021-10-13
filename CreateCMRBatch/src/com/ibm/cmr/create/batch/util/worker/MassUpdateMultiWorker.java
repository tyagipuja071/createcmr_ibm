/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;
import com.ibm.cmr.create.batch.util.MultiThreadedWorker;

/**
 * MultiThreadedWorker base class for {@link MassUpdt}
 * 
 * @author 136786PH1
 *
 */
public abstract class MassUpdateMultiWorker extends MultiThreadedWorker<MassUpdt> {

  protected static final String MASS_UPDATE_FAIL = "FAIL";
  protected static final String MASS_UPDATE_DONE = "DONE";

  public MassUpdateMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassUpdt parentEntity) {
    super(parentService, parentAdmin, parentEntity);
  }

  protected boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

  /**
   * 
   * @param entityManager
   * @param cmrNo
   * @param cmrIssuingCntry
   * @return
   */
  protected boolean isOwnerCorrect(EntityManager entityManager, String cmrNo, String cmrIssuingCntry) {
    String sql = "select KATR10 from SAPR3.KNA1 where MANDT = :MANDT and KATR6 = :COUNTRY and ZZKV_CUSNO = :CMR_NO and KTOKD = 'ZS01'";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    String katr10 = query.getSingleResult(String.class);
    if (katr10 == null) {
      // non existent, return true;
      return true;
    } else {
      return "".equals(katr10.trim());
    }
  }

}
