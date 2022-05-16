package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.USMassProcessMultiService;

public class USMassProcessServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("USMassProcess");

    // BaseBatchService service = null;
    USMassProcessMultiService usService = new USMassProcessMultiService();
    // usService.setMassServiceMode(true);
    // service = usService;
    usService.execute();
  }
}