/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.MassCreateValidatorService;

/**
 * Entry Point for the Mass Create validator batch program
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateValidatorEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {

    BatchEntryPoint.initContext("MassCreateValidator");

    MassCreateValidatorService service = new MassCreateValidatorService();
    service.execute();

  }
}
