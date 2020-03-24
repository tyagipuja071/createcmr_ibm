/**
 *
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.util.updt.USUpdateChecker;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * Checks the updates being done on a request and is called from the
 * {@link UpdateSwitchElement}
 *
 * @author JeffZAMORA
 *
 */
public abstract class UpdateChecker {

  private static Map<String, Class<? extends UpdateChecker>> checkerMap = new HashMap<>();

  static {
    registerChecker(SystemLocation.UNITED_STATES, USUpdateChecker.class);
  }

  /**
   * Registers a checker class
   *
   * @param sysLoc
   * @param checkerClass
   */
  private static void registerChecker(String sysLoc, Class<? extends UpdateChecker> checkerClass) {
    checkerMap.put(sysLoc, checkerClass);
  }

}
