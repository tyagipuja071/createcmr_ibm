/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.RequestMaintService;

/**
 * Entry point for {@link RequestMaintService}
 * 
 * @author 136786PH1
 *
 */
public class RequestMaintEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("ReqMaint", false);
    RequestMaintService service = new RequestMaintService();
    service.execute();
  }

}
