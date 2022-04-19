/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.create.batch.service.TransConnMultiService;
import com.ibm.cmr.create.batch.service.TransConnMultiService.Mode;

/**
 * @author 136786PH1
 *
 */
public class TransConnMultiEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("TransConnMulti", true);

    TransConnMultiService service = new TransConnMultiService();
    service.setMode(Mode.Aborted);
    service.execute();

    service.setMode(Mode.Pending);
    service.execute();

    service.setMode(Mode.MQ);
    service.execute();

    service.setMode(Mode.Manual);
    service.execute();

    if ("Y".equals(SystemParameters.getString("POOL.CMR.STATUS"))) {
      service.setMode(Mode.Pool);
      service.execute();
    }

  }

}
