/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.NPSService;

/**
 * Entry point for the {@link NPSService} batch application
 * 
 * @author JeffZAMORA
 * 
 */
public class NPSEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("NPSSurvey");

    NPSService service = new NPSService();
    service.execute();
  }

}
