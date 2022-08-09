/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.ATMultiService;
import com.ibm.cmr.create.batch.service.ATMultiService.Mode;

public class ATServiceMultiEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("ATServiceMulti", true);

    ATMultiService service = new ATMultiService();
    service.setSkipExit(true);
    service.setMode(Mode.Aborted);
    service.execute();

    service.setMode(Mode.Normal);
    service.execute();

    service.setMode(Mode.Mass);
    service.execute();

  }
}
