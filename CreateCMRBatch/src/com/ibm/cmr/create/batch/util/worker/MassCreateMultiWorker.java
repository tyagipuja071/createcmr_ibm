/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;
import com.ibm.cmr.create.batch.util.MultiThreadedWorker;

/**
 * {@link MultiThreadedWorker} for {@link MassCreate}
 * 
 * @author 136786PH1
 *
 */
public abstract class MassCreateMultiWorker extends MultiThreadedWorker<MassCreate> {

  protected static final String MASS_CREATE_FAIL = "FAIL";
  protected static final String MASS_CREATE_DONE = "DONE";

  public MassCreateMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassCreate parentEntity) {
    super(parentService, parentAdmin, parentEntity);

  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

  protected boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
  }

}
