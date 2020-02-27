/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.CheckCertificatesService;

/**
 * Entry point for the services that checks whether Batch URLs
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CheckCertificatesEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initPlainContext("CheckCert");
    // this comment is a prod fix
    CheckCertificatesService service = new CheckCertificatesService();
    service.execute();
  }

}
