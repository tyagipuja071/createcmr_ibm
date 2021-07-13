/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cmr.create.batch.util.MultiThreadedWorker;

/**
 * MultiThreadedWorker base class for {@link MassUpdt}
 * 
 * @author 136786PH1
 *
 */
public abstract class MassUpdateMultiWorker extends MultiThreadedWorker<MassUpdt> {

  public MassUpdateMultiWorker(Admin parentAdmin, MassUpdt parentEntity) {
    super(parentAdmin, parentEntity);
  }

}
