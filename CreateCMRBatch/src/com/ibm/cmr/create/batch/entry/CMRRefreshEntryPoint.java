/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.CMRRefreshService;

/**
 * Entry point for {@link CMRRefreshService}
 * 
 * @author JeffZAMORA
 *
 */
public class CMRRefreshEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("CMRRefresh");

    CMRRefreshService service = new CMRRefreshService();
    if (args != null) {
      for (int i = 0; i < args.length - 1; i++) {
        if ("-hours".equals(args[i])) {
          String hours = args[i + 1];
          if (StringUtils.isNumeric(hours)) {
            int iHours = Integer.parseInt(hours);
            if (iHours > 0 && iHours <= 12) {
              service.setHours(iHours);
            }
          }
        }
        if ("-country".equals(args[i])) {
          String country = args[i + 1];
          if (StringUtils.isNumeric(country) && country.length() == 3) {
            service.setCountry(country);
          }
        }
      }
    }
    service.execute();
  }
}
