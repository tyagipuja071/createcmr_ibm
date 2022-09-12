/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.create.batch.service.FillSapNoService;
import com.ibm.cmr.create.batch.service.TransConnMultiService;
import com.ibm.cmr.create.batch.service.TransConnMultiService.Mode;

/**
 * @author 136786PH1
 *
 */
public class TransConnMultiEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    boolean delete = false;
    if (args != null && args.length > 0 && "DELETE".equalsIgnoreCase(args[0].trim())) {
      delete = true;
    }
    BatchEntryPoint.initContext("TransConnMulti", true);

    FillSapNoService fill = new FillSapNoService();
    fill.setSkipExit(true);
    fill.execute();

    TransConnMultiService service = new TransConnMultiService();
    service.setSkipExit(true);
    service.setDeleteMode(delete);

    service.setMode(Mode.Aborted);
    service.execute();

    service.setMode(Mode.Pending);
    service.execute();

    service.setMode(Mode.MQ);
    service.execute();

    if (!"Y".equals(SystemParameters.getString("POOL.CMR.STATUS"))) {
      service.setSkipExit(false);
    }
    service.setMode(Mode.Manual);
    service.execute();

    if ("Y".equals(SystemParameters.getString("POOL.CMR.STATUS"))) {
      service.setSkipExit(false);
      service.setMode(Mode.Pool);
      service.execute();
    }

    service.setMode(Mode.LAReprocess);
    service.execute();

  }

}
