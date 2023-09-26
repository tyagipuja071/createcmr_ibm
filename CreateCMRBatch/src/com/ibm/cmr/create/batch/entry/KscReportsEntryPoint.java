/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.KscReportsService;

/**
 * {@link BatchEntryPoint} for {@link KscReportsService}
 * 
 * @author 136786PH1
 *
 */
public class KscReportsEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("KSC", false);
    KscReportsService service = new KscReportsService();
    if (args != null && args.length > 0) {
      service.setMode(args[0]);
    }
    if (args != null && args.length > 1) {
      try {
        String refDate = args[1];
        if (refDate.length() == 8) {
          SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
          Date parsed = format.parse(refDate);
          service.setReferenceDate(parsed);
        }
      } catch (Exception e) {
        // noop, skip ref date setting
      }
    }
    service.execute();
  }
}
