/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.TransConnMultiService;
import com.ibm.cmr.create.batch.service.TransConnMultiService.Mode;

/**
 * @author 136786PH1
 *
 */
public class PoolEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("Pool", true);

    TransConnMultiService service = new TransConnMultiService();
    service.setMode(Mode.Pool);
    service.execute();
  }

}
