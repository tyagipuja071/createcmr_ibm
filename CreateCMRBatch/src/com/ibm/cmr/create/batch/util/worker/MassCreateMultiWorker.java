/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker;

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

  public MassCreateMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassCreate parentEntity) {
    super(parentService, parentAdmin, parentEntity);

  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

}
