/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.ApprovalBatchService;

/**
 * @author Jeffrey Zamora
 * 
 */
public class ApprovalEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("Approval", true);
    ApprovalBatchService service = new ApprovalBatchService();
    service.execute();
  }
}
