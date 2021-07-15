package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.ATMassProcessMultiLegacyService;
import com.ibm.cmr.create.batch.service.ATService;
import com.ibm.cmr.create.batch.service.BaseBatchService;

/**
 * Entry point for the {@link ATMassProcessMultiLegacyService} process
 * 
 * @author Shivani Chauhan
 * 
 */
public class ATMassProcessEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("ATMassMultiProcess");
    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new ATMassProcessMultiLegacyService();
    } else {
      ATService atService = new ATService();
      atService.setMassServiceMode(true);
      service = atService;
    }
    service.execute();
  }

}
