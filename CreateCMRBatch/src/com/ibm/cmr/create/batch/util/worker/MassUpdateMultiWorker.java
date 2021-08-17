/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.MassUpdt;
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

}
