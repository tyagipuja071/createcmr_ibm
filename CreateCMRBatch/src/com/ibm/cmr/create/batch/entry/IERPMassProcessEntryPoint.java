/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.IERPMassProcessService;

/**
 * Entry point for the {@link IERPMassProcessService} process
 * 
 * @author Dennis NATAD
 * 
 */
public class IERPMassProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("IERPMassProcess");

    IERPMassProcessService service = new IERPMassProcessService();
    service.setDevMode(args != null && args.length > 0 && "DEV".equals(args[0]));
    service.execute();
  }

}
