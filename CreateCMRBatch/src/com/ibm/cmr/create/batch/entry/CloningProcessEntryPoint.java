/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.CloningProcessService;

/**
 * Entry point for the {@link CloningProcessEntryPoint} process
 * 
 * @author PriyRanjan
 * 
 */
public class CloningProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {

    BatchEntryPoint.initContext("CloningProcess");

    CloningProcessService service = new CloningProcessService();
    service.setDevMode(args != null && args.length > 0 && "DEV".equals(args[0]));
    service.execute();

  }

}
