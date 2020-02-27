/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.MassCreateProcessService;

/**
 * @author Rochelle Salazar
 * 
 */
public class MassCreateProcessEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("MassCreateProcess");

    MassCreateProcessService service = new MassCreateProcessService();
    service.execute();
  }
}
